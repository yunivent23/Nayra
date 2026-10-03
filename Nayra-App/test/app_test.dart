import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:nayra_app/api/cliente_http.dart';
import 'package:nayra_app/app/dependencias.dart';
import 'package:nayra_app/app/nayra_app.dart';

import 'falsos.dart';

/// Backend falso por ruta (método + ruta → cola de respuestas). Registra método, ruta y sesión recibida.
class BackendFalso {
  BackendFalso(this.rutas);
  final Map<String, List<(int, Object?)>> rutas;
  final recibidas = <String>[];

  http.Client get cliente => MockClient((r) async {
        final clave = '${r.method} ${r.url.path}';
        final sesion = r.headers['Authorization'];
        recibidas.add(sesion == null ? clave : '$clave [$sesion]');
        final cola = rutas[clave];
        if (cola == null) return http.Response('{"error":"NO_ENCONTRADO"}', 404);
        final (codigo, cuerpo) = cola.length > 1 ? cola.removeAt(0) : cola.first;
        return http.Response(cuerpo == null ? '' : jsonEncode(cuerpo), codigo,
            headers: {'content-type': 'application/json'});
      });
}

Map<String, Object?> yo({String rol = 'USER'}) => {
      'nombres': 'Ana María',
      'apellidos': 'Torres Díaz',
      'celular': '987654321',
      'rol': rol,
      'estado': 'ACTIVO',
      'dispositivoActivo': true,
    };

Map<String, List<(int, Object?)>> loginCorrecto({String rol = 'USER'}) => {
      'POST /prototipo/autenticacion/nonces': [(200, {'nonce': 'N1', 'proposito': 'INICIO_SESION'})],
      'POST /prototipo/autenticacion/transacciones': [(200, {'transaccionId': 'T1'})],
      'POST /prototipo/autenticacion/transacciones/T1/pin': [
        (200, {
          'estado': 'CONTINUAR',
          'motivo': null,
          'intentosRestantes': 3,
          'desafio': {'desafioId': 'D1', 'texto': 'barco, cinco, uno, ocho, luna'},
          'sesion': null,
        })
      ],
      'POST /prototipo/autenticacion/transacciones/T1/voz': [
        (200, {'estado': 'AUTENTICADO', 'motivo': null, 'intentosRestantes': null, 'desafio': null, 'sesion': 'JWT'})
      ],
      'GET /api/v1/usuarios/me': [(200, yo(rol: rol))],
      'DELETE /api/v1/sesiones/actual': [(204, null)],
    };

late VozFalsa voz;
final botonVoz = find.byKey(const Key('botonVoz'));

Future<void> abrir(WidgetTester tester, BackendFalso backend) async {
  tester.view.physicalSize = const Size(1236, 2745); // 412 × 915 dp
  tester.view.devicePixelRatio = 3;
  addTearDown(tester.view.reset);
  final dispositivo = DispositivoFalso()..ids = {'cuentaId': 'u1', 'dispositivoId': 'disp1'};
  await tester.pumpWidget(NayraApp(
    dependencias: Dependencias(
      http: ClienteHttp('http://x', cliente: backend.cliente),
      dispositivo: dispositivo,
      grabador: GrabadorFalso(),
      voz: voz = VozFalsa(),
    ),
  ));
  await tester.pumpAndSettle();
}

/// Deja vencer la sesión para que no quede el temporizador de inactividad pendiente al terminar la prueba.
Future<void> vencerSesion(WidgetTester tester) async {
  await tester.pump(const Duration(minutes: 6));
  await tester.pumpAndSettle();
}

Future<void> tocar(WidgetTester tester, Finder f) async {
  await tester.ensureVisible(f.first);
  await tester.pumpAndSettle();
  await tester.tap(f.first);
  await tester.pumpAndSettle();
}

/// Bienvenida → botón de voz («Iniciar sesión Nayra») → PIN → frase → voz → saludo → billetera (D-075).
Future<void> iniciarSesion(WidgetTester tester) async {
  await tocar(tester, find.text('Iniciar sesión'));
  await tocar(tester, botonVoz);
  expect(find.text('Ingresa tu PIN'), findsOneWidget);
  for (final d in ['4', '8', '2', '9', '1', '3']) {
    await tocar(tester, find.bySemanticsLabel(d));
  }
  expect(find.text('Verifiquemos tu voz'), findsOneWidget);
  expect(find.bySemanticsLabel('Frase a decir: barco, cinco, uno, ocho, luna'), findsOneWidget);
  expect(voz.dichos.last,
      'PIN correcto. Ahora verificaré tu voz. Toca el botón y repite: barco, cinco, uno, ocho, luna');
  await tocar(tester, botonVoz);
  expect(find.text('Te escucho'), findsOneWidget);
  await tocar(tester, botonVoz);
  expect(voz.dichos, contains('Identidad verificada. ¡Hola, Ana!'));
  expect(find.text('Menú'), findsOneWidget);
}

void main() {
  testWidgets('inicio de sesión completo → billetera → saldo sin endpoint → mis datos → cerrar sesión',
      (tester) async {
    final semantica = tester.ensureSemantics();
    final backend = BackendFalso(loginCorrecto());
    await abrir(tester, backend);
    expect(find.text('Registrarme'), findsOneWidget);

    await iniciarSesion(tester);
    // El PIN viaja una sola vez al backend y nunca aparece en la interfaz ni en lo que dice Nayra.
    expect(find.textContaining('482913'), findsNothing);
    expect(voz.dichos.where((d) => d.contains('482913')), isEmpty);
    // Semidúplex: «Te escucho» se dice una vez, antes de abrir el micrófono de la verificación de voz.
    expect(voz.dichos.where((d) => d == 'Te escucho.'), hasLength(1));
    expect(find.text('Registro asistido'), findsNothing, reason: 'solo para ADMIN');

    await tocar(tester, find.text('Saldo'));
    expect(find.text('Esta función todavía no está disponible en Nayra.'), findsOneWidget);
    expect(find.textContaining('S/'), findsNothing, reason: 'sin endpoint no se muestran datos ficticios');
    await tocar(tester, find.text('Volver al menú'));

    await tocar(tester, find.text('Mis datos'));
    expect(find.bySemanticsLabel('Nombre: Ana María Torres Díaz'), findsOneWidget);
    expect(find.bySemanticsLabel('Cuenta: Activa'), findsOneWidget);
    await tocar(tester, find.text('Cerrar sesión'));
    expect(find.text('Registrarme'), findsOneWidget, reason: 'HU-13: vuelve a la bienvenida');
    expect(backend.recibidas, contains('DELETE /api/v1/sesiones/actual [Bearer JWT]'));
    semantica.dispose();
  });

  testWidgets('5 minutos sin actividad cierran la sesión y no se puede volver atrás (D-018)', (tester) async {
    final backend = BackendFalso(loginCorrecto());
    await abrir(tester, backend);
    await iniciarSesion(tester);
    expect(find.text('Menú'), findsOneWidget);
    // El plazo se cuenta desde la última petición con sesión (unidad: cliente_http_test.dart).
    await tester.pump(const Duration(minutes: 5, seconds: 1));
    await tester.pumpAndSettle();
    expect(find.text('Su sesión se cerró'), findsOneWidget);
    expect(find.text('Pasaron 5 minutos sin actividad.'), findsOneWidget);
    expect(find.text('Menú'), findsNothing);
    // «Atrás» del sistema no vuelve a una pantalla protegida: la pila se vació al cerrar la sesión.
    await tester.binding.handlePopRoute();
    await tester.pumpAndSettle();
    expect(find.text('Su sesión se cerró'), findsOneWidget);
    expect(find.text('Menú'), findsNothing);
    expect(backend.recibidas.where((r) => r.startsWith('DELETE')), isEmpty,
        reason: 'el cierre por inactividad lo controla el servidor');
  });

  testWidgets('401 del backend en una pantalla protegida lleva a «Su sesión se cerró»', (tester) async {
    final rutas = loginCorrecto();
    rutas['GET /api/v1/usuarios/me'] = [(200, yo()), (401, {'error': 'NO_AUTENTICADO'})];
    await abrir(tester, BackendFalso(rutas));
    await iniciarSesion(tester);
    await tocar(tester, find.text('Mis datos'));
    expect(find.text('Su sesión se cerró'), findsOneWidget);
    expect(find.text('Su sesión ya no es válida.'), findsOneWidget);
  });

  testWidgets('PIN incorrecto: mensaje con intentos restantes; tercer fallo: cuenta bloqueada', (tester) async {
    final rutas = loginCorrecto();
    rutas['POST /prototipo/autenticacion/transacciones/T1/pin'] = [
      (200, {'estado': 'REINTENTAR', 'motivo': 'PIN_INCORRECTO', 'intentosRestantes': 1, 'desafio': null, 'sesion': null}),
      (200, {'estado': 'BLOQUEADA', 'motivo': 'PIN_INCORRECTO', 'intentosRestantes': 0, 'desafio': null, 'sesion': null}),
    ];
    await abrir(tester, BackendFalso(rutas));
    await tocar(tester, find.text('Iniciar sesión'));
    await tocar(tester, botonVoz);
    for (var i = 0; i < 6; i++) {
      await tocar(tester, find.bySemanticsLabel('0'));
    }
    expect(find.textContaining('Te queda 1 intento'), findsWidgets);
    expect(find.text('Ingresa tu PIN'), findsOneWidget);
    for (var i = 0; i < 6; i++) {
      await tocar(tester, find.bySemanticsLabel('0'));
    }
    expect(find.text('Cuenta bloqueada'), findsWidgets);
    expect(find.text('Se agotaron los intentos. Por seguridad, tu cuenta está bloqueada.'), findsOneWidget);
  });

  testWidgets('sin conexión al iniciar sesión: mensaje comprensible y opción de reintentar', (tester) async {
    tester.view.physicalSize = const Size(1236, 2745);
    tester.view.devicePixelRatio = 3;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(NayraApp(
      dependencias: Dependencias(
        http: ClienteHttp('http://x', cliente: MockClient((r) async => throw http.ClientException('sin red'))),
        dispositivo: DispositivoFalso()..ids = {'dispositivoId': 'disp1'},
        grabador: GrabadorFalso(),
        voz: VozFalsa(),
      ),
    ));
    await tocar(tester, find.text('Iniciar sesión'));
    await tocar(tester, botonVoz);
    expect(find.textContaining('No pude conectarme con Nayra'), findsWidgets);
    expect(find.text('Intentar de nuevo'), findsOneWidget);
  });

  testWidgets('cuenta ADMIN: el menú agrega el registro asistido del representante', (tester) async {
    final rutas = loginCorrecto(rol: 'ADMIN')
      ..['POST /api/v1/admin/registros'] = [(200, {'codigoRegistro': 'K7P2Q9', 'nombres': 'Luis', 'apellidos': 'Ramos'})]
      ..['POST /api/v1/admin/registros/K7P2Q9/validacion-identidad'] = [(204, null)];
    final backend = BackendFalso(rutas);
    await abrir(tester, backend);
    await iniciarSesion(tester);
    await tocar(tester, find.text('Registro asistido'));
    expect(find.text('Paso 1 de 8'), findsOneWidget);
    await tester.enterText(find.byType(TextField), '45781236');
    await tocar(tester, find.text('Consultar'));
    expect(find.text('Luis Ramos'), findsOneWidget);
    await tocar(tester, find.text('Identidad validada'));
    expect(find.text('K7P2Q9'), findsOneWidget);
    expect(backend.recibidas, contains('POST /api/v1/admin/registros [Bearer JWT]'));
    await vencerSesion(tester);
  });

  testWidgets('transferir: celular → destinatario encontrado → No / Sí, sin ID interno (G-1)', (tester) async {
    final semantica = tester.ensureSemantics();
    final rutas = loginCorrecto()
      ..['POST /api/v1/destinatarios/busqueda'] = [
        (404, {'error': 'DESTINATARIO_NO_ENCONTRADO'}),
        (200, {'nombreVisible': 'María De la...'}),
        (200, {'nombreVisible': 'María De la...'}),
      ];
    final backend = BackendFalso(rutas);
    await abrir(tester, backend);
    await iniciarSesion(tester);
    await tocar(tester, find.text('Transferir'));

    expect(find.text('¿A quién transfiere?'), findsOneWidget);
    expect(find.textContaining('Identificador'), findsNothing, reason: 'el ID interno de la cuenta nunca se pide');
    expect(find.bySemanticsLabel('Número de celular: vacío'), findsOneWidget);
    Future<void> escribir(String digitos) async {
      for (final d in digitos.split('')) {
        await tocar(tester, find.bySemanticsLabel(d));
      }
    }

    await escribir('987654321');
    expect(find.text('987 654 321'), findsOneWidget);
    await tocar(tester, find.text('Buscar'));
    expect(find.text('No hay una cuenta Nayra asociada a ese número. Revíselo y toque Buscar.'), findsOneWidget);
    expect(find.text('987 654 321'), findsOneWidget, reason: 'el número escrito se conserva');

    for (var i = 0; i < 9; i++) {
      await tocar(tester, find.bySemanticsLabel('Borrar último dígito'));
    }
    await escribir('912345678');
    await tocar(tester, find.text('Buscar'));
    expect(find.text('Destinatario encontrado'), findsOneWidget);
    expect(find.text('¿Desea transferir a María De la...?'), findsOneWidget);
    expect(find.textContaining('912'), findsNothing, reason: 'se confirma por el nombre, no por el número');
    await expectLater(tester, meetsGuideline(androidTapTargetGuideline));
    await expectLater(tester, meetsGuideline(labeledTapTargetGuideline));
    await expectLater(tester, meetsGuideline(textContrastGuideline));

    await tocar(tester, find.text('No, buscar otro número'));
    expect(find.text('¿A quién transfiere?'), findsOneWidget);
    expect(find.bySemanticsLabel('Número de celular: vacío'), findsOneWidget, reason: 'se reinicia el ingreso');

    await escribir('912345678');
    await tocar(tester, find.text('Buscar'));
    await tocar(tester, find.text('Sí'));
    expect(find.text('¿Cuánto envía?'), findsOneWidget);
    expect(backend.recibidas.where((r) => r.startsWith('POST /api/v1/destinatarios/busqueda')),
        everyElement(endsWith('[Bearer JWT]')));
    await vencerSesion(tester);
    semantica.dispose();
  });

  group('Accesibilidad (guías de Flutter para Android)', () {
    Future<void> revisar(WidgetTester tester) async {
      await expectLater(tester, meetsGuideline(androidTapTargetGuideline));
      await expectLater(tester, meetsGuideline(labeledTapTargetGuideline));
      await expectLater(tester, meetsGuideline(textContrastGuideline));
    }

    testWidgets('bienvenida, PIN, frase de desafío y menú', (tester) async {
      final semantica = tester.ensureSemantics();
      await abrir(tester, BackendFalso(loginCorrecto()));
      await revisar(tester);
      await tocar(tester, find.text('Iniciar sesión'));
      await revisar(tester);
      await tocar(tester, botonVoz);
      await revisar(tester);
      for (final d in ['4', '8', '2', '9', '1', '3']) {
        await tocar(tester, find.bySemanticsLabel(d));
      }
      await revisar(tester);
      await tocar(tester, botonVoz);
      await revisar(tester);
      await tocar(tester, botonVoz);
      await revisar(tester);
      await tocar(tester, find.text('Transferir'));
      await revisar(tester);
      await vencerSesion(tester);
      semantica.dispose();
    });

    testWidgets('el texto crece con el tamaño de letra del sistema sin desbordes', (tester) async {
      tester.platformDispatcher.textScaleFactorTestValue = 2.0;
      addTearDown(tester.platformDispatcher.clearTextScaleFactorTestValue);
      await abrir(tester, BackendFalso(loginCorrecto()));
      await tocar(tester, find.text('Iniciar sesión'));
      expect(tester.takeException(), isNull);
      await tocar(tester, botonVoz);
      expect(tester.takeException(), isNull);
      for (final d in ['4', '8', '2', '9', '1', '3']) {
        await tocar(tester, find.bySemanticsLabel(d));
      }
      expect(tester.takeException(), isNull);
      await tocar(tester, botonVoz);
      expect(tester.takeException(), isNull);
      await tocar(tester, botonVoz);
      await vencerSesion(tester);
    });

    testWidgets('la búsqueda del destinatario no se desborda con la letra al 200 %', (tester) async {
      final rutas = loginCorrecto()..['POST /api/v1/destinatarios/busqueda'] = [(200, {'nombreVisible': 'María De la...'})];
      await abrir(tester, BackendFalso(rutas));
      await iniciarSesion(tester);
      // La letra se agranda desde aquí: esta prueba cubre solo las pantallas del destinatario.
      tester.platformDispatcher.textScaleFactorTestValue = 2.0;
      addTearDown(tester.platformDispatcher.clearTextScaleFactorTestValue);
      await tocar(tester, find.text('Transferir'));
      for (final d in '912345678'.split('')) {
        await tocar(tester, find.bySemanticsLabel(d));
      }
      expect(tester.takeException(), isNull);
      await tocar(tester, find.text('Buscar'));
      expect(find.text('No, buscar otro número'), findsOneWidget);
      expect(tester.takeException(), isNull);
      await vencerSesion(tester);
    });
  });
}
