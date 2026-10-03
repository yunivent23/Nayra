import 'dart:async';
import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:nayra_app/api/cliente_http.dart';
import 'package:nayra_app/api/representante_api.dart';
import 'package:nayra_app/api/usuario_api.dart';
import 'package:nayra_app/flujo/mensajes.dart';
import 'package:nayra_app/modelos/autenticacion.dart';
import 'package:nayra_app/modelos/registro.dart';
import 'package:nayra_app/modelos/usuario.dart';
import 'package:nayra_app/sesion/gestor_sesion.dart';

http.Response json(int codigo, Object? cuerpo) =>
    http.Response(cuerpo == null ? '' : jsonEncode(cuerpo), codigo, headers: {'content-type': 'application/json'});

void main() {
  group('ClienteHttp', () {
    test('envía el JWT solo en peticiones con sesión y avisa cada respuesta correcta', () async {
      final cabeceras = <Map<String, String>>[];
      var peticionesConSesion = 0;
      final c = ClienteHttp('http://x', cliente: MockClient((r) async {
        cabeceras.add(r.headers);
        return json(200, {'ok': true});
      }))
        ..tokenSesion = (() => 'JWT')
        ..alPeticionConSesion = (() => peticionesConSesion++);
      await c.get(c.uriApi('/usuarios/me'), conSesion: true);
      await c.postJson(c.uriApi('/registros/X/datos'), {'a': 1});
      expect(cabeceras[0]['Authorization'], 'Bearer JWT');
      expect(cabeceras[1].containsKey('Authorization'), isFalse);
      expect(cabeceras[1]['Content-Type'], startsWith('application/json'));
      expect(peticionesConSesion, 1);
    });

    test('401 con sesión avisa que la sesión terminó; 401 sin sesión no', () async {
      var invalidas = 0;
      final c = ClienteHttp('http://x', cliente: MockClient((r) async => json(401, {'error': 'NO_AUTENTICADO'})))
        ..tokenSesion = (() => 'JWT')
        ..alSesionInvalida = (() => invalidas++);
      await expectLater(c.get(c.uriApi('/usuarios/me'), conSesion: true),
          throwsA(isA<ErrorApi>().having((e) => e.estadoHttp, 'estado', 401)));
      expect(invalidas, 1);
      // El 401 del inicio de sesión (firma del dispositivo) no es una sesión vencida.
      await expectLater(c.postJson(c.uriPrototipo('/autenticacion/transacciones')), throwsA(isA<ErrorApi>()));
      expect(invalidas, 1);
    });

    test('pantalla protegida sin token: no hace la petición', () async {
      var llamadas = 0;
      final c = ClienteHttp('http://x', cliente: MockClient((r) async {
        llamadas++;
        return json(200, {});
      }));
      await expectLater(c.get(c.uriApi('/usuarios/me'), conSesion: true),
          throwsA(isA<ErrorApi>().having((e) => e.codigo, 'codigo', 'NO_AUTENTICADO')));
      expect(llamadas, 0);
    });

    test('errores del backend, sin conexión, tiempo agotado y respuesta inválida', () async {
      Future<void> espera(http.Client cliente, Matcher m, {Duration? t}) => expectLater(
          ClienteHttp('http://x', cliente: cliente, tiempoEspera: t ?? const Duration(seconds: 5))
              .get(Uri.parse('http://x/api/v1/y')),
          throwsA(m));
      await espera(MockClient((r) async => json(409, {'error': 'CUENTA_BLOQUEADA'})),
          isA<ErrorApi>().having((e) => e.codigo, 'codigo', 'CUENTA_BLOQUEADA'));
      await espera(MockClient((r) async => http.Response('<html>', 502)),
          isA<ErrorApi>().having((e) => e.codigo, 'codigo', 'ERROR'));
      await espera(MockClient((r) async => throw http.ClientException('sin red')), isA<ErrorConexion>());
      await espera(MockClient((r) async => http.Response('no-json', 200)), isA<ErrorRespuesta>());
      await espera(MockClient((r) => Completer<http.Response>().future), isA<ErrorTiempoAgotado>(),
          t: const Duration(milliseconds: 10));
    });

    test('mensajes comprensibles para cada fallo, sin detalles técnicos', () {
      expect(mensajeFallo(const ErrorConexion()), contains('No pude conectarme'));
      expect(mensajeFallo(const ErrorTiempoAgotado()), contains('tardó demasiado'));
      expect(mensajeFallo(const ErrorApi(401, 'NO_AUTENTICADO')), contains('sesión se cerró'));
      expect(mensajeFallo(const ErrorApi(403, 'ACCESO_DENEGADO')), contains('No tienes permiso'));
      expect(mensajeFallo(const ErrorApi(500, 'ERROR_INTERNO')), isNot(contains('ERROR_INTERNO')));
      expect(mensajeFallo(const FuncionNoDisponible('saldo')), contains('todavía no está disponible'));
    });
  });

  group('Serialización con los DTOs reales del backend', () {
    test('ResultadoPaso (AutenticacionVozDTOs) y contrato roto', () {
      final r = ResultadoPaso.desdeJson({
        'estado': 'CONTINUAR',
        'motivo': null,
        'intentosRestantes': 3,
        'desafio': {'desafioId': 'd1', 'texto': 'llave, uno, dos, tres, mesa'},
        'sesion': null,
      });
      expect(r.estado, EstadoPaso.continuar);
      expect(r.desafio!.texto, 'llave, uno, dos, tres, mesa');
      expect(() => ResultadoPaso.desdeJson({'motivo': 'X'}), throwsA(isA<ErrorRespuesta>()));
      expect(() => ResultadoPaso.desdeJson({'estado': 'REINTENTAR', 'intentosRestantes': '2'}),
          throwsA(isA<ErrorRespuesta>()));
    });

    test('DatosPropios (UsuarioDTOs) con estados del modelo v4', () {
      final d = DatosPropios.desdeJson({
        'nombres': 'María Elena',
        'apellidos': 'Quispe Huamán',
        'celular': null,
        'rol': 'ADMIN',
        'estado': 'BLOQUEADO',
        'dispositivoActivo': false,
      });
      expect(d.primerNombre, 'María');
      expect(d.esAdministrador, isTrue);
      expect(textoEstadoCuenta(d.estado), 'Bloqueada');
      expect(textoEstadoCuenta('ACTIVO'), 'Activa');
      expect(textoEstadoCuenta('INACTIVO'), 'Inactiva');
    });

    test('RegistroIniciado usa codigoRegistro; el representante envía tipo y número con su sesión', () async {
      final recibidas = <http.Request>[];
      final c = ClienteHttp('http://x', cliente: MockClient((r) async {
        recibidas.add(r);
        return r.url.path.endsWith('validacion-identidad')
            ? json(204, null)
            : json(200, {'codigoRegistro': 'ABC123', 'nombres': 'Ana', 'apellidos': 'Torres'});
      }))
        ..tokenSesion = (() => 'JWT-ADMIN');
      final api = RepresentanteApi(c);
      final r = await api.iniciarRegistro(TipoDocumento.ce, '001234567');
      await api.validarIdentidad(r.codigo);
      expect(r.codigo, 'ABC123');
      expect(recibidas[0].url.path, '/api/v1/admin/registros');
      expect(jsonDecode(recibidas[0].body), {'tipoDocumentoIdentidad': 'CE', 'numeroDocumento': '001234567'});
      expect(recibidas[0].headers['Authorization'], 'Bearer JWT-ADMIN');
      expect(recibidas[1].url.path, '/api/v1/admin/registros/ABC123/validacion-identidad');
    });
  });

  group('GestorSesion (D-018)', () {
    Map<String, Object?> yo() => {
          'nombres': 'Ana',
          'apellidos': 'Torres',
          'celular': '987654321',
          'rol': 'USER',
          'estado': 'ACTIVO',
          'dispositivoActivo': true,
        };

    test('sin peticiones durante el plazo se cierra sin avisar al servidor; una petición lo reinicia', () async {
      // Plazo real: ConfiguracionApp.inactividadSesion (5 minutos). Aquí se acorta para la prueba.
      const plazo = Duration(milliseconds: 400);
      final metodos = <String>[];
      final c = ClienteHttp('http://x', cliente: MockClient((r) async {
        metodos.add(r.method);
        return json(200, yo());
      }));
      final sesion = GestorSesion(c, UsuarioApi(c), inactividad: plazo);
      await sesion.iniciar('JWT');
      await Future<void>.delayed(const Duration(milliseconds: 250));
      await sesion.actualizarUsuario();
      await Future<void>.delayed(const Duration(milliseconds: 250));
      expect(sesion.activa, isTrue, reason: 'la segunda petición reinició el plazo');
      await Future<void>.delayed(const Duration(milliseconds: 300));
      expect(sesion.activa, isFalse);
      expect(sesion.ultimoCierre, MotivoCierre.inactividad);
      expect(metodos, ['GET', 'GET'], reason: 'el cierre por inactividad lo controla el servidor: sin DELETE');
      expect(sesion.usuario, isNull, reason: 'los datos se descartan con la sesión');
    });
  });
}
