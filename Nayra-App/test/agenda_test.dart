import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:nayra_app/api/billetera_api.dart';
import 'package:nayra_app/api/cliente_http.dart';
import 'package:nayra_app/api/destinatario_api.dart';
import 'package:nayra_app/app/dependencias.dart';
import 'package:nayra_app/flujo/flujo_transferencia.dart';
import 'package:nayra_app/modelos/billetera.dart';
import 'package:nayra_app/pantallas/billetera/pantalla_transferencia.dart';
import 'package:nayra_app/servicios/agenda.dart';

import 'falsos.dart';

/// Agenda del teléfono simulada. [permiso] false equivale a que la persona no dé el permiso de contactos.
class AgendaDePrueba implements AgendaContactos {
  AgendaDePrueba(this.contactos);
  final List<ContactoAgenda> contactos;
  bool permiso = true;
  int lecturas = 0;

  @override
  Future<List<ContactoAgenda>> leer() async {
    lecturas++;
    if (!permiso) throw const AgendaSinPermiso();
    return contactos;
  }
}

/// Nayra-Back simulado con el contrato real de POST /api/v1/destinatarios/busqueda-multiple (DestinatarioController):
/// devuelve, en el orden recibido, solo los celulares de [registrados].
class BusquedaMultipleDePrueba {
  BusquedaMultipleDePrueba(this.registrados);
  final Map<String, String> registrados;
  final recibidas = <http.Request>[];
  Object? fallo;
  int? estadoHttp;

  DestinatarioApi get api => DestinatarioApi(ClienteHttp('http://x', cliente: MockClient((r) async {
        recibidas.add(r);
        if (fallo != null) throw fallo!;
        if (estadoHttp != null) return http.Response('{"error":"ERROR_INTERNO"}', estadoHttp!);
        final celulares = ((jsonDecode(r.body) as Map)['celulares'] as List).cast<String>();
        final destinatarios = [
          for (final c in celulares)
            if (registrados[c] != null) {'celular': c, 'nombreVisible': registrados[c]},
        ];
        return http.Response(jsonEncode({'destinatarios': destinatarios}), 200,
            headers: {'content-type': 'application/json'});
      }))
        ..tokenSesion = (() => 'JWT'));

  List<String> celularesEnviados(int i) =>
      ((jsonDecode(recibidas[i].body) as Map)['celulares'] as List).cast<String>();
}

class _BilleteraSinUso implements BilleteraApi {
  @override
  dynamic noSuchMethod(Invocation invocation) => throw UnimplementedError();
}

const _agendaEjemplo = [
  ContactoAgenda('María', ['999 111 111']),
  ContactoAgenda('Juan', ['+51 999-222-222']),
  ContactoAgenda('Pedro', ['999333333']),
  ContactoAgenda('Ana', ['(01) 444 5555']),
];

void main() {
  group('Cruce de la agenda con los destinatarios registrados (D-042)', () {
    test('solo se muestran los contactos confirmados por el backend', () {
      final r = CruceAgenda.cruzar(_agendaEjemplo, [const Destinatario('999333333', 'Pedro Gómez')]);
      expect(r.map((c) => c.nombreAgenda), ['Pedro']);
      expect(r.single.destinatario.celular, '999333333');
      expect(r.single.destinatario.nombreVisible, 'Pedro Gómez');
    });

    test('se excluyen los contactos no registrados y los números que no son celulares', () {
      final r = CruceAgenda.cruzar(_agendaEjemplo, [
        const Destinatario('999222222', 'Juan Del R...'),
        const Destinatario('914445555', 'No está en la agenda'),
      ]);
      expect(r.map((c) => c.nombreAgenda), ['Juan']);
    });

    test('mantiene el orden de la agenda aunque el backend responda en otro orden', () {
      final r = CruceAgenda.cruzar(_agendaEjemplo, [
        const Destinatario('999333333', 'Pedro Gómez'),
        const Destinatario('999111111', 'María Sala...'),
        const Destinatario('999222222', 'Juan Del R...'),
      ]);
      expect(r.map((c) => c.nombreAgenda), ['María', 'Juan', 'Pedro']);
    });

    test('respuesta vacía: ningún contacto', () {
      expect(CruceAgenda.cruzar(_agendaEjemplo, const []), isEmpty);
    });

    test('un contacto con varios números registrados aparece una sola vez, con el primero', () {
      const agenda = [
        ContactoAgenda('Pedro', ['999333333', '+51 988 777 666', '999 333 333']),
        ContactoAgenda('Pedro trabajo', ['988777666']),
      ];
      final r = CruceAgenda.cruzar(agenda, const [
        Destinatario('988777666', 'Pedro Gómez'),
        Destinatario('999333333', 'Pedro Gómez'),
      ]);
      expect(r.map((c) => (c.nombreAgenda, c.destinatario.celular)), [('Pedro', '999333333'), ('Pedro trabajo', '988777666')]);
    });

    test('un mismo número en dos contactos se muestra una sola vez', () {
      const agenda = [
        ContactoAgenda('Mamá', ['999333333']),
        ContactoAgenda('Rosa', ['+51999333333']),
      ];
      final r = CruceAgenda.cruzar(agenda, const [Destinatario('999333333', 'Rosa Ped...')]);
      expect(r.map((c) => c.nombreAgenda), ['Mamá']);
    });

    test('contacto sin nombre: se muestra el nombre que devuelve Nayra', () {
      final r = CruceAgenda.cruzar(const [ContactoAgenda('', ['999333333'])], const [Destinatario('999333333', 'Pedro Gómez')]);
      expect(r.single.nombreAgenda, 'Pedro Gómez');
    });

    test('celulares a enviar: normalizados, sin repetir, en el orden de la agenda y sin números inservibles', () {
      const agenda = [
        ContactoAgenda('María', ['999 111 111', '(01) 444 5555']),
        ContactoAgenda('Sin número', []),
        ContactoAgenda('Juan', ['+51 (999) 222-222', '999.111.111', 'abc']),
        ContactoAgenda('Pedro', ['999333333', '12345']),
      ];
      final r = CruceAgenda.celulares(agenda);
      expect(r.celulares, ['999111111', '999222222', '999333333']);
      expect(r.recortada, isFalse);
    });

    test('máximo 500: primero normaliza y quita repetidos, conserva el orden y recién después recorta', () {
      final agenda = [
        // 600 números repetidos en otro formato: no cuentan para el máximo.
        for (var i = 0; i < 600; i++) ContactoAgenda('Repetido $i', ['+51 999 000 001']),
        for (var i = 1; i <= 501; i++) ContactoAgenda('Contacto $i', ['9${i.toString().padLeft(8, '0')}', 'no es un número']),
      ];
      final r = CruceAgenda.celulares(agenda);
      expect(r.celulares, hasLength(500));
      expect(r.celulares.first, '999000001');
      expect(r.celulares[1], '900000001');
      expect(r.celulares.last, '900000499');
      expect(r.celulares, isNot(contains('900000500')));
      expect(r.recortada, isTrue);
    });

    test('exactamente 500 números no se recortan', () {
      final agenda = [for (var i = 0; i < 500; i++) ContactoAgenda('C$i', ['9${i.toString().padLeft(8, '0')}'])];
      final r = CruceAgenda.celulares(agenda);
      expect(r.celulares, hasLength(500));
      expect(r.recortada, isFalse);
    });
  });

  group('Consulta múltiple con DestinatarioApi', () {
    test('envía {"celulares": [...]} con sesión y lee {"destinatarios": [{celular, nombreVisible}]}', () async {
      final b = BusquedaMultipleDePrueba({'999333333': 'Pedro Gómez'});
      final r = await b.api.buscarVarios(['999111111', '999333333']);
      expect(b.recibidas.single.url.path, '/api/v1/destinatarios/busqueda-multiple');
      expect(b.recibidas.single.headers['Authorization'], 'Bearer JWT');
      expect(jsonDecode(b.recibidas.single.body), {
        'celulares': ['999111111', '999333333'],
      });
      expect(r.map((d) => (d.celular, d.nombreVisible)), [('999333333', 'Pedro Gómez')]);
    });

    test('nunca envía más de 500 celulares: sin paginación ni varias consultas', () async {
      final celulares = [for (var i = 0; i < 501; i++) '9${i.toString().padLeft(8, '0')}'];
      final b = BusquedaMultipleDePrueba({});
      expect(() => b.api.buscarVarios(celulares), throwsArgumentError);
      expect(b.recibidas, isEmpty);
    });
  });

  group('Elegir de mis contactos en la transferencia', () {
    FlujoTransferencia flujo(BusquedaMultipleDePrueba b, AgendaContactos agenda) =>
        FlujoTransferencia(_BilleteraSinUso(), b.api, agenda);

    test('sin agenda no se ofrece la opción', () {
      final f = FlujoTransferencia(_BilleteraSinUso(), BusquedaMultipleDePrueba({}).api);
      expect(f.conAgenda, isFalse);
      expect(f.mensaje, FlujoTransferencia.pedirCelular);
    });

    test('muestra solo los contactos registrados y el elegido pasa a la confirmación', () async {
      final b = BusquedaMultipleDePrueba({'999333333': 'Pedro Gómez'});
      final agenda = AgendaDePrueba(_agendaEjemplo);
      final f = flujo(b, agenda);
      expect(agenda.lecturas, 0, reason: 'el permiso se pide solo al tocar Elegir de mis contactos');

      await f.buscarEnContactos();
      expect(b.celularesEnviados(0), ['999111111', '999222222', '999333333']);
      expect(f.paso, PasoTransferencia.contactos);
      expect(f.contactos.map((c) => c.nombreAgenda), ['Pedro']);
      expect(f.mensaje, contains('Tienes 1 contacto con cuenta Nayra'));

      f.elegirContacto(f.contactos.single);
      expect(f.paso, PasoTransferencia.confirmarDestinatario);
      expect(f.destinatario!.celular, '999333333');
      expect(f.contactos, isEmpty, reason: 'la lista no se conserva al salir');
      f.confirmarDestinatario(true);
      expect(f.paso, PasoTransferencia.monto);
    });

    test('agenda con más de 500 números: una sola consulta con los primeros 500 y aviso accesible', () async {
      final agenda = [
        for (var i = 1; i <= 520; i++) ContactoAgenda('Contacto $i', ['9${i.toString().padLeft(8, '0')}']),
      ];
      // Registrados: uno dentro de los primeros 500 y otro fuera (el 510), que no se consulta ni se muestra.
      final b = BusquedaMultipleDePrueba({'900000002': 'Dos Con...', '900000510': 'Fuera Del...'});
      final f = flujo(b, AgendaDePrueba(agenda));
      await f.buscarEnContactos();
      expect(b.recibidas, hasLength(1));
      expect(b.celularesEnviados(0), [for (var i = 1; i <= 500; i++) '9${i.toString().padLeft(8, '0')}']);
      expect(f.contactos.map((c) => c.nombreAgenda), ['Contacto 2']);
      expect(f.mensaje, contains(FlujoTransferencia.hasta500));
    });

    test('hasta 500 números: sin aviso del máximo', () async {
      final f = flujo(BusquedaMultipleDePrueba({'999333333': 'Pedro Gómez'}), AgendaDePrueba(_agendaEjemplo));
      await f.buscarEnContactos();
      expect(f.mensaje, isNot(contains(FlujoTransferencia.hasta500)));
    });

    test('permiso denegado: vuelve a la búsqueda manual sin consultar al backend', () async {
      final b = BusquedaMultipleDePrueba({'999333333': 'Pedro Gómez'});
      final f = flujo(b, AgendaDePrueba(_agendaEjemplo)..permiso = false);
      await f.buscarEnContactos();
      expect(f.paso, PasoTransferencia.destinatario);
      expect(f.mensajeEsError, isTrue);
      expect(f.mensaje, contains('Puedes continuar escribiendo el número de celular.'));
      expect(f.contactos, isEmpty);
      expect(b.recibidas, isEmpty);
    });

    test('ningún contacto registrado: mensaje de Nayra y sin contactos', () async {
      final b = BusquedaMultipleDePrueba({});
      final f = flujo(b, AgendaDePrueba(_agendaEjemplo));
      await f.buscarEnContactos();
      expect(b.recibidas, hasLength(1));
      expect(f.paso, PasoTransferencia.destinatario);
      expect(f.mensaje, startsWith(FlujoTransferencia.sinContactos));
      expect(f.contactos, isEmpty);
    });

    test('agenda vacía o sin celulares utilizables: no se consulta al backend', () async {
      for (final agenda in [<ContactoAgenda>[], [const ContactoAgenda('Ana', ['(01) 444 5555']), const ContactoAgenda('Luis', [])]]) {
        final b = BusquedaMultipleDePrueba({});
        final f = flujo(b, AgendaDePrueba(agenda));
        await f.buscarEnContactos();
        expect(b.recibidas, isEmpty);
        expect(f.paso, PasoTransferencia.destinatario);
        expect(f.mensaje, startsWith(FlujoTransferencia.sinContactos));
        expect(f.contactos, isEmpty);
      }
    });

    test('error HTTP o backend sin respuesta: ningún contacto se muestra como destinatario', () async {
      for (final configurar in <void Function(BusquedaMultipleDePrueba)>[
        (b) => b.estadoHttp = 500,
        (b) => b.fallo = http.ClientException('sin red'),
      ]) {
        final b = BusquedaMultipleDePrueba({'999333333': 'Pedro Gómez'});
        configurar(b);
        final f = flujo(b, AgendaDePrueba(_agendaEjemplo));
        await f.buscarEnContactos();
        expect(f.paso, PasoTransferencia.destinatario);
        expect(f.mensajeEsError, isTrue);
        expect(f.mensaje, endsWith('Puedes continuar escribiendo el número de celular.'));
        expect(f.contactos, isEmpty);
      }
    });

    test('«Escribir el número» vuelve al teclado y descarta la lista', () async {
      final f = flujo(BusquedaMultipleDePrueba({'999333333': 'Pedro Gómez'}), AgendaDePrueba(_agendaEjemplo));
      await f.buscarEnContactos();
      f.escribirNumero();
      expect(f.paso, PasoTransferencia.destinatario);
      expect(f.contactos, isEmpty);
    });
  });

  testWidgets('la pantalla lista solo los contactos registrados con botones accesibles', (tester) async {
    tester.view.physicalSize = const Size(1236, 2745);
    tester.view.devicePixelRatio = 3;
    addTearDown(tester.view.reset);
    final b = BusquedaMultipleDePrueba({'999333333': 'Pedro Gómez'});
    final agenda = AgendaDePrueba(_agendaEjemplo);
    final f = FlujoTransferencia(_BilleteraSinUso(), b.api, agenda);
    await tester.pumpWidget(ProveedorNayra(
      dependencias: Dependencias(
        http: ClienteHttp('http://x'),
        dispositivo: DispositivoFalso(),
        grabador: GrabadorFalso(),
        voz: VozFalsa(),
        agenda: agenda,
      ),
      child: MaterialApp(home: PantallaTransferencia(flujo: f)),
    ));
    await tester.pumpAndSettle();
    expect(agenda.lecturas, 0);

    await tester.ensureVisible(find.text('Elegir de mis contactos'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Elegir de mis contactos'));
    await tester.pumpAndSettle();
    expect(find.text('Pedro'), findsOneWidget);
    for (final otro in ['María', 'Juan', 'Ana']) {
      expect(find.text(otro), findsNothing);
    }
    expect(find.bySemanticsLabel(RegExp('Pedro. En Nayra: Pedro Gómez.')), findsOneWidget);
    expect(find.text('Escribir el número'), findsOneWidget);
  });
}
