import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:nayra_app/api/billetera_api.dart';
import 'package:nayra_app/api/cliente_http.dart';
import 'package:nayra_app/api/destinatario_api.dart';
import 'package:nayra_app/flujo/flujo_billetera.dart';
import 'package:nayra_app/flujo/flujo_transferencia.dart';
import 'package:nayra_app/modelos/billetera.dart';
import 'package:nayra_app/modelos/celular.dart';

/// Doble de prueba de la billetera. SOLO PARA PRUEBAS: la app usa BilleteraApiPendiente hasta que existan los
/// endpoints en Nayra-Back; estos datos nunca llegan a la app.
class BilleteraDePrueba implements BilleteraApi {
  final transferencias = <(String, Soles)>[];
  ResultadoTransferencia resultado = const ResultadoTransferencia(EstadoOperacion.exitoso, '482915');

  @override
  Future<Saldo> saldo() async => const Saldo(Soles(125000));

  @override
  Future<List<Movimiento>> movimientos() async => [];

  @override
  Future<ResultadoTransferencia> transferir(Destinatario destinatario, Soles monto) async {
    transferencias.add((destinatario.celular, monto));
    return resultado;
  }

  @override
  Future<List<Aviso>> avisos() async => [];
}

/// Nayra-Back simulado con el contrato real de POST /api/v1/destinatarios/busqueda (DestinatarioController):
/// 912345678 → "Carlos Rodr..."; 900000000 → número propio; el resto, sin cuenta Nayra.
class BusquedaDePrueba {
  final recibidas = <http.Request>[];
  Object? fallo;

  DestinatarioApi get api => DestinatarioApi(ClienteHttp('http://x', cliente: MockClient((r) async {
        recibidas.add(r);
        if (fallo != null) throw fallo!;
        final celular = (jsonDecode(r.body) as Map)['celular'];
        final (codigo, cuerpo) = switch (celular) {
          '912345678' => (200, {'nombreVisible': 'Carlos Rodr...'}),
          '900000000' => (409, {'error': 'CUENTAS_IGUALES'}),
          _ => (404, {'error': 'DESTINATARIO_NO_ENCONTRADO'}),
        };
        return http.Response(jsonEncode(cuerpo), codigo, headers: {'content-type': 'application/json'});
      }))
        ..tokenSesion = (() => 'JWT'));
}

void escribir(FlujoTransferencia f, String digitos) {
  for (final d in digitos.split('')) {
    f.cambiarCelular(f.celularTexto + d);
  }
}

void main() {
  group('Soles y regla del monto (v4 §2.8, E-03)', () {
    test('lectura sin redondeo y formatos visibles y hablados', () {
      expect(Soles.leer('80')!.centimos, 8000);
      expect(Soles.leer('80,5')!.centimos, 8050);
      expect(Soles.leer('0.01')!.centimos, 1);
      expect(Soles.leer('1.005'), isNull, reason: 'más de 2 decimales no se redondea: se rechaza');
      expect(Soles.leer('abc'), isNull);
      expect(const Soles(125000).texto, 'S/ 1,250.00');
      expect(const Soles(8050).decimal, '80.50');
      expect(const Soles(8050).hablado, '80 soles con 50 céntimos');
      expect(const Soles(100).hablado, '1 sol');
    });

    test('mayor que 0 y menor que 500', () {
      expect(ReglaMonto.validar('0'), 'MONTO_FUERA_DE_RANGO');
      expect(ReglaMonto.validar('0,01'), isNull);
      expect(ReglaMonto.validar('499,99'), isNull);
      expect(ReglaMonto.validar('500'), 'MONTO_FUERA_DE_RANGO');
      expect(ReglaMonto.validar('10,555'), 'MONTO_ESCALA_INVALIDA');
      expect(ReglaMonto.validar(''), 'MONTO_FUERA_DE_RANGO');
    });
  });

  group('Celular del destinatario (G-1)', () {
    test('formato de Perú, normalización de +51 y lectura', () {
      expect(Celular.normalizar('912345678'), '912345678');
      expect(Celular.normalizar('+51912345678'), '912345678');
      expect(Celular.normalizar(' 912 345-678 '), '912345678');
      for (final invalido in ['', '12345', '812345678', '9123456789', '51912345678', '+52912345678']) {
        expect(Celular.normalizar(invalido), isNull, reason: invalido);
      }
      expect(Celular.agrupado('912345678'), '912 345 678');
      expect(Celular.hablado('912345678'), '9 1 2, 3 4 5, 6 7 8');
    });

    test('el lector de pantalla lee la misma porción del apellido, sin los puntos suspensivos', () {
      const casos = {
        'María Pérez': 'María Pérez',
        'María Sala...': 'María Sala',
        'María De la...': 'María De la',
        'Juan Del R...': 'Juan Del R',
        'Ana De Los...': 'Ana De Los',
      };
      casos.forEach((visible, hablado) {
        expect(Destinatario('912345678', visible).nombreHablado, hablado, reason: visible);
      });
    });

    test('la búsqueda envía el número canónico con la sesión y lee solo el nombre visible', () async {
      final b = BusquedaDePrueba();
      final d = await b.api.buscar('912345678');
      expect(d.nombreVisible, 'Carlos Rodr...');
      expect(d.nombreHablado, 'Carlos Rodr');
      final r = b.recibidas.single;
      expect(r.method, 'POST');
      expect(r.url.path, '/api/v1/destinatarios/busqueda');
      expect(jsonDecode(r.body), {'celular': '912345678'});
      expect(r.headers['Authorization'], 'Bearer JWT');
    });
  });

  group('Transferencia (HU-69 a HU-72)', () {
    test('celular → destinatario encontrado → Sí → monto → revisar → confirmar → resultado', () async {
      final api = BilleteraDePrueba();
      final b = BusquedaDePrueba();
      final f = FlujoTransferencia(api, b.api);
      escribir(f, '912345678');
      expect(f.mensaje, '9 1 2, 3 4 5, 6 7 8', reason: 'el número se anuncia completo, dígito por dígito');
      await f.buscar();
      expect(f.paso, PasoTransferencia.confirmarDestinatario);
      expect(f.preguntaDestinatario, '¿Desea transferir a Carlos Rodr...?');
      expect(f.mensaje, startsWith('¿Desea transferir a Carlos Rodr?'));
      expect(f.mensaje, isNot(contains('912')), reason: 'la confirmación es por el nombre, no por el número');
      f.confirmarDestinatario(true);
      expect(f.paso, PasoTransferencia.monto);
      f.cambiarMonto('500');
      f.continuarConMonto();
      expect(f.paso, PasoTransferencia.monto);
      expect(f.mensaje, contains('menor que 500'));
      f.cambiarMonto('80');
      expect(f.mensaje, '80 soles');
      f.continuarConMonto();
      expect(f.paso, PasoTransferencia.revisar);
      expect(f.mensaje, contains('Va a enviar 80 soles a Carlos Rodr.'));
      expect(api.transferencias, isEmpty, reason: 'nada se envía antes de confirmar (HU-70)');
      await f.confirmar();
      expect(api.transferencias.single, ('912345678', const Soles(8000)));
      expect(f.paso, PasoTransferencia.resultado);
      expect(f.mensaje, contains('Transferencia exitosa'));
      expect(f.mensaje, contains('4 8 2 9 1 5'));
    });

    test('«No, buscar otro número» reinicia el ingreso del número', () async {
      final f = FlujoTransferencia(BilleteraDePrueba(), BusquedaDePrueba().api);
      escribir(f, '912345678');
      await f.buscar();
      f.confirmarDestinatario(false);
      expect(f.paso, PasoTransferencia.destinatario);
      expect(f.celularTexto, isEmpty);
      expect(f.destinatario, isNull);
      expect(f.mensaje, 'Indique nuevamente el número de celular del destinatario.');
    });

    test('número inválido: no se consulta al backend', () async {
      final b = BusquedaDePrueba();
      final f = FlujoTransferencia(BilleteraDePrueba(), b.api);
      escribir(f, '81234');
      await f.buscar();
      expect(f.paso, PasoTransferencia.destinatario);
      expect(f.mensajeEsError, isTrue);
      expect(f.mensaje, contains('9 dígitos y empezar con 9'));
      expect(b.recibidas, isEmpty);
    });

    test('número sin cuenta Nayra, número propio y error de conexión conservan lo escrito', () async {
      final b = BusquedaDePrueba();
      final f = FlujoTransferencia(BilleteraDePrueba(), b.api);
      escribir(f, '987654321');
      await f.buscar();
      expect(f.paso, PasoTransferencia.destinatario);
      expect(f.mensaje, 'No hay una cuenta Nayra asociada a ese número. Revíselo y toque Buscar.');
      expect(f.celularTexto, '987654321');

      f.cambiarCelular('900000000');
      await f.buscar();
      expect(f.mensaje, contains('su propio número'));

      b.fallo = http.ClientException('sin red');
      f.cambiarCelular('912345678');
      await f.buscar();
      expect(f.paso, PasoTransferencia.destinatario);
      expect(f.mensajeEsError, isTrue);
      expect(f.mensaje, contains('Toque Buscar para intentarlo otra vez'));
      expect(f.celularTexto, '912345678', reason: 'no se pierde el número escrito');
      b.fallo = null;
      await f.buscar();
      expect(f.paso, PasoTransferencia.confirmarDestinatario);
    });

    test('cancelar antes de confirmar no llama al backend (HU-71)', () async {
      final api = BilleteraDePrueba();
      final f = FlujoTransferencia(api, BusquedaDePrueba().api);
      escribir(f, '912345678');
      await f.buscar();
      f.confirmarDestinatario(true);
      f.cambiarMonto('10');
      f.continuarConMonto();
      f.cancelar();
      expect(f.paso, PasoTransferencia.resultado);
      expect(f.cancelada, isTrue);
      expect(api.transferencias, isEmpty);
    });

    test('resultado FALLIDO del backend se informa como no realizado', () async {
      final api = BilleteraDePrueba()..resultado = const ResultadoTransferencia(EstadoOperacion.fallido, null);
      final f = FlujoTransferencia(api, BusquedaDePrueba().api);
      escribir(f, '912345678');
      await f.buscar();
      f.confirmarDestinatario(true);
      f.cambiarMonto('10');
      f.continuarConMonto();
      await f.confirmar();
      expect(f.mensaje, 'La transferencia no se realizó.');
      expect(f.mensajeEsError, isTrue);
    });
  });

  group('Sin endpoints en Nayra-Back (BilleteraApiPendiente)', () {
    test('no inventa datos: saldo, movimientos, avisos y transferencia informan que no están disponibles', () async {
      const api = BilleteraApiPendiente();
      for (final c in [consultaSaldo(api), consultaMovimientos(api), consultaAvisos(api)]) {
        await c.cargar();
        expect(c.estado, EstadoCarga.error);
        expect(c.valor, isNull);
        expect(c.mensaje, 'Esta función todavía no está disponible en Nayra.');
      }
      final f = FlujoTransferencia(api, BusquedaDePrueba().api);
      escribir(f, '912345678');
      await f.buscar();
      f.confirmarDestinatario(true);
      f.cambiarMonto('10');
      f.continuarConMonto();
      await f.confirmar();
      expect(f.paso, PasoTransferencia.revisar, reason: 'la transferencia todavía no existe en Nayra-Back');
      expect(f.mensaje, 'Esta función todavía no está disponible en Nayra.');
    });

    test('lista vacía y lista de uno en uno', () async {
      final c = consultaMovimientos(BilleteraDePrueba());
      await c.cargar();
      expect(c.estado, EstadoCarga.vacio);
      final l = ListaUnoEnUno([1, 2, 3]);
      l.anterior();
      expect(l.indice, 0);
      l
        ..siguiente()
        ..siguiente()
        ..siguiente();
      expect(l.actual, 3);
      expect(l.haySiguiente, isFalse);
    });
  });
}
