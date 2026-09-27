import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:nayra_app/flujo/flujo_cuenta.dart';
import 'package:nayra_app/flujo/flujo_inicio_sesion.dart';
import 'package:nayra_app/flujo/flujo_registro.dart';
import 'package:nayra_app/servicios/api_prototipo.dart';

import 'falsos.dart';

typedef Respuesta = (int, Map<String, Object?>);

/// Backend falso: responde por ruta y registra lo que recibe.
MockClient backend(Map<String, List<Respuesta>> rutas, List<String> recibidas) => MockClient((r) async {
      final ruta = r.url.path.replaceFirst('/prototipo', '');
      final sesion = r.headers['Authorization'] == null ? '' : ' [${r.headers['Authorization']}]';
      recibidas.add('${r.method} $ruta${r.body.contains('audio') ? ' [audio]' : r.body.isEmpty ? '' : ' ${r.body}'}$sesion');
      final cola = rutas.entries.firstWhere((e) => RegExp('^${e.key}\$').hasMatch(ruta)).value;
      final (codigo, cuerpo) = cola.length > 1 ? cola.removeAt(0) : cola.first;
      return http.Response(cuerpo.isEmpty ? '' : jsonEncode(cuerpo), codigo, headers: {'content-type': 'application/json'});
    });

Map<String, Object?> paso(String estado, {String? motivo, int? restantes, String? desafio, String? sesion}) => {
      'estado': estado,
      'motivo': motivo,
      'intentosRestantes': restantes,
      'desafio': desafio == null ? null : {'desafioId': 'd-$desafio', 'texto': desafio},
      'sesion': sesion,
    };

void main() {
  late DispositivoFalso dispositivo;
  late List<String> recibidas;

  setUp(() {
    dispositivo = DispositivoFalso()..ids = {'cuentaId': 'c1', 'dispositivoId': 'disp1'};
    recibidas = [];
  });

  test('inicio de sesión: dispositivo → PIN → desafío → voz → autenticado', () async {
    final api = ApiPrototipo('http://x', cliente: backend({
      '/autenticacion/nonces': [(200, {'nonce': 'N1', 'proposito': 'INICIO_SESION'})],
      '/autenticacion/transacciones': [(200, {'transaccionId': 'T1'})],
      '/autenticacion/transacciones/T1/pin': [(200, paso('CONTINUAR', restantes: 3, desafio: 'llave, uno, dos, tres, mesa'))],
      '/autenticacion/transacciones/T1/voz': [(200, paso('AUTENTICADO', sesion: 'TOKEN'))],
    }, recibidas));
    final f = FlujoInicioSesion(api, dispositivo);
    await f.iniciar();
    expect(dispositivo.firmados.single, 'N1|disp1|INICIO_SESION');
    expect(f.paso, PasoInicioSesion.pin);
    await f.enviarPin('482913');
    expect(f.paso, PasoInicioSesion.voz);
    expect(f.mensaje, contains('llave, uno, dos, tres, mesa'));
    expect(f.mensaje, isNot(contains('482913')));
    await f.enviarVoz(await GrabadorFalso().detener());
    expect(f.paso, PasoInicioSesion.autenticado);
    expect(f.sesion, 'TOKEN');
  });

  test('reintento de voz presenta el nuevo desafío e intentos restantes', () async {
    final api = ApiPrototipo('http://x', cliente: backend({
      '/autenticacion/nonces': [(200, {'nonce': 'N1'})],
      '/autenticacion/transacciones': [(200, {'transaccionId': 'T1'})],
      '/autenticacion/transacciones/T1/pin': [(200, paso('CONTINUAR', desafio: 'llave, uno, dos, tres, mesa'))],
      '/autenticacion/transacciones/T1/voz': [
        (200, paso('REINTENTAR', motivo: 'NO_COINCIDE', restantes: 2, desafio: 'luna, cuatro, cinco, seis, pera')),
        (200, paso('BLOQUEADA', motivo: 'NO_COINCIDE', restantes: 0)),
      ],
    }, recibidas));
    final f = FlujoInicioSesion(api, dispositivo);
    await f.iniciar();
    await f.enviarPin('482913');
    await f.enviarVoz(await GrabadorFalso().detener());
    expect(f.paso, PasoInicioSesion.voz);
    expect(f.desafio!.texto, 'luna, cuatro, cinco, seis, pera');
    expect(f.mensaje, contains('Le quedan 2 intentos'));
    await f.enviarVoz(await GrabadorFalso().detener());
    expect(f.paso, PasoInicioSesion.bloqueada);
  });

  test('PIN incorrecto se queda en el paso de PIN; cuenta bloqueada se informa', () async {
    final api = ApiPrototipo('http://x', cliente: backend({
      '/autenticacion/nonces': [(200, {'nonce': 'N1'})],
      '/autenticacion/transacciones': [(200, {'transaccionId': 'T1'}), (409, {'error': 'CUENTA_BLOQUEADA'})],
      '/autenticacion/transacciones/T1/pin': [(200, paso('REINTENTAR', motivo: 'PIN_INCORRECTO', restantes: 2))],
    }, recibidas));
    final f = FlujoInicioSesion(api, dispositivo);
    await f.iniciar();
    await f.enviarPin('000000');
    expect(f.paso, PasoInicioSesion.pin);
    expect(f.mensaje, 'El PIN no es correcto. Le quedan 2 intentos.');
    await f.iniciar();
    expect(f.paso, PasoInicioSesion.bloqueada);
  });

  test('sin dispositivo vinculado no se contacta al backend', () async {
    dispositivo.ids = {};
    final f = FlujoInicioSesion(ApiPrototipo('http://x', cliente: backend({}, recibidas)), dispositivo);
    await f.iniciar();
    expect(f.paso, PasoInicioSesion.terminado);
    expect(recibidas, isEmpty);
  });

  test('registro asistido en el celular: código, datos, celular, PIN, 3 muestras y finalización', () async {
    dispositivo.ids = {};
    const c = 'COD1';
    final api = ApiPrototipo('http://x', cliente: backend({
      '/api/v1/registros/$c': [(200, {'nombres': 'Persona Dos', 'apellidos': 'Ficticia Simulada', 'paso': 'IDENTIDAD_VALIDADA'})],
      '/api/v1/registros/$c/datos': [(200, {'paso': 'DATOS_COMPLETOS'})],
      '/registro/$c/enrolamiento/desafios': [(200, {'desafioId': 'e1', 'texto': 'luna, uno, dos, tres, mapa'})],
      '/registro/$c/enrolamiento/muestras': [
        (200, {'aceptada': true, 'motivo': null, 'muestrasValidas': 1, 'muestrasRequeridas': 3}),
        (200, {'aceptada': false, 'motivo': 'CALIDAD_INSUFICIENTE', 'muestrasValidas': 1, 'muestrasRequeridas': 3}),
        (200, {'aceptada': true, 'motivo': null, 'muestrasValidas': 2, 'muestrasRequeridas': 3}),
        (200, {'aceptada': true, 'motivo': null, 'muestrasValidas': 3, 'muestrasRequeridas': 3}),
      ],
      '/registro/$c/enrolamiento/finalizacion': [(200, {'correcto': true, 'motivo': null, 'muestrasValidas': 3})],
      '/api/v1/registros/$c/finalizacion': [(200, {'usuarioId': 'u9', 'dispositivoId': 'd9'})],
    }, recibidas));
    final f = FlujoRegistro(api, dispositivo);
    await f.ingresarCodigo(' $c ');
    expect(f.paso, PasoRegistro.confirmarDatos);
    expect(f.mensaje, contains('Persona Dos Ficticia Simulada'));
    await f.confirmarDatos(true);
    f.ingresarCelular('12');
    expect(f.paso, PasoRegistro.celular);
    f.ingresarCelular('987 654 321');
    expect(f.paso, PasoRegistro.pin);
    f.ingresarPin('482913');
    await f.confirmarPin('111111');
    expect(f.paso, PasoRegistro.pin);
    f.ingresarPin('482913');
    await f.confirmarPin('482913');
    expect(f.paso, PasoRegistro.muestra);
    expect(recibidas.any((r) => r.contains('"celular":"987654321"') && r.contains('CLAVE_PUBLICA')), isTrue);
    final audio = await GrabadorFalso().detener();
    await f.enviarMuestra(audio);
    await f.enviarMuestra(audio);
    expect(f.mensaje, contains('No se escuchó con claridad'));
    await f.enviarMuestra(audio);
    expect(dispositivo.ids, isEmpty, reason: 'Los identificadores se guardan al finalizar el registro');
    await f.enviarMuestra(audio);
    expect(f.paso, PasoRegistro.terminado);
    expect(dispositivo.ids, {'cuentaId': 'u9', 'dispositivoId': 'd9'});
  });

  test('registro: código no válido, identidad sin validar y datos rechazados', () async {
    final api = ApiPrototipo('http://x', cliente: backend({
      '/api/v1/registros/MALO': [(404, {'error': 'REGISTRO_NO_VALIDO'})],
      '/api/v1/registros/PEND': [(409, {'error': 'IDENTIDAD_NO_VALIDADA'})],
      '/api/v1/registros/OK': [(200, {'nombres': 'A', 'apellidos': 'B', 'paso': 'IDENTIDAD_VALIDADA'})],
      '/api/v1/registros/OK/datos': [(200, {'paso': 'CANCELADO'})],
    }, recibidas));
    final f = FlujoRegistro(api, dispositivo);
    await f.ingresarCodigo('MALO');
    expect(f.paso, PasoRegistro.codigo);
    expect(f.mensaje, contains('no es válido'));
    await f.ingresarCodigo('PEND');
    expect(f.mensaje, contains('todavía no validó'));
    await f.ingresarCodigo('OK');
    await f.confirmarDatos(false);
    expect(f.paso, PasoRegistro.error);
    expect(recibidas.last, contains('"confirmaDatos":false'));
  });

  test('mis datos con la sesión y cierre de sesión', () async {
    final api = ApiPrototipo('http://x', cliente: backend({
      '/api/v1/usuarios/me': [
        (200, {'nombres': 'Persona Dos', 'apellidos': 'Ficticia Simulada', 'celular': '987654321', 'rol': 'USER',
          'estado': 'ACTIVA', 'dispositivoActivo': true}),
        (401, {'error': 'NO_AUTENTICADO'}),
      ],
      '/api/v1/sesiones/actual': [(204, {})],
    }, recibidas));
    final f = FlujoCuenta(api, 'TOKEN');
    await f.cargar();
    expect(f.paso, PasoCuenta.datos);
    expect(f.mensaje, contains('Estado de la cuenta: activa'));
    expect(recibidas.single, 'GET /api/v1/usuarios/me [Bearer TOKEN]');
    await f.cargar();
    expect(f.paso, PasoCuenta.cerrada, reason: '401: la sesión venció por inactividad o fue revocada');
    await f.cerrarSesion();
    expect(recibidas.last, 'DELETE /api/v1/sesiones/actual [Bearer TOKEN]');
  });
}
