import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nayra_app/api/cliente_http.dart';
import 'package:nayra_app/app/dependencias.dart';
import 'package:nayra_app/app/nayra_app.dart';
import 'package:nayra_app/componentes/comando_voz.dart';
import 'package:nayra_app/servicios/escucha_comando.dart';

import 'app_test.dart' show BackendFalso, botonVoz, loginCorrecto, tocar;
import 'falsos.dart';

/// Vosk falso: anota cuándo abre y cierra su micrófono y deja simular lo que «oye».
class EscuchaFalsa implements EscuchaComando {
  EscuchaFalsa(this.registro);
  final List<String> registro;
  bool fallaAlIniciar = false;
  bool escuchando = false;
  void Function(String)? _alTexto;

  @override
  Future<void> iniciar(void Function(String texto) alTexto, void Function(Object error) alFallar) async {
    if (fallaAlIniciar) throw StateError('Vosk: modelo no encontrado');
    registro.add('vosk abierto');
    escuchando = true;
    _alTexto = alTexto;
  }

  @override
  Future<void> detener() async {
    if (escuchando) registro.add('vosk cerrado');
    escuchando = false;
    _alTexto = null;
  }

  /// La persona dice [texto] mientras Vosk escucha.
  void oir(String texto) => _alTexto?.call(texto);
}

/// Dispositivo que anota cuándo empieza el inicio de sesión del Paso 2 (lectura de la clave del celular).
class DispositivoEspia extends DispositivoFalso {
  DispositivoEspia(this.registro);
  final List<String> registro;

  @override
  Future<Map<String, String>> leerIdentificadores() {
    registro.add('login iniciado');
    return super.leerIdentificadores();
  }
}

/// Voz falsa cuyo estado «hablando» controla la prueba.
class VozQueHabla extends VozFalsa {
  final hablandoAhora = ValueNotifier(false);

  @override
  ValueListenable<bool> get hablando => hablandoAhora;
}

void main() {
  group('Comparación del comando (D-081)', () {
    test('acepta «Iniciar sesión Nayra» sin importar mayúsculas, tildes ni espacios', () {
      for (final t in [
        'Iniciar sesión Nayra',
        'iniciar sesión nayra',
        'INICIAR SESIÓN NAYRA',
        '  iniciar   sesión  Nayra. ',
        'iniciar sesión neyra', // como escribe «Nayra» el modelo pequeño de español
      ]) {
        expect(esComandoIniciarSesion(t), isTrue, reason: t);
      }
    });

    test('tolera la ausencia de tilde', () {
      expect(esComandoIniciarSesion('iniciar sesion Nayra'), isTrue);
      expect(esComandoIniciarSesion('iniciar sesion nayra'), isTrue);
    });

    test('una frase distinta no es el comando', () {
      for (final t in [
        'iniciar sesión',
        'abre Nayra',
        'quiero entrar',
        'cerrar sesión Nayra',
        'iniciar sesión Nayra ahora',
        '[unk]',
        '',
      ]) {
        expect(esComandoIniciarSesion(t), isFalse, reason: t);
      }
    });
  });

  group('Inicio de sesión por comando de voz (D-081)', () {
    late List<String> registro;
    late EscuchaFalsa vosk;
    late VozFalsa voz;

    Future<void> abrirConVosk(WidgetTester tester, {bool falla = false}) async {
      registro = [];
      vosk = EscuchaFalsa(registro)..fallaAlIniciar = falla;
      voz = VozFalsa();
      tester.view.physicalSize = const Size(1236, 2745);
      tester.view.devicePixelRatio = 3;
      addTearDown(tester.view.reset);
      await tester.pumpWidget(NayraApp(
        dependencias: Dependencias(
          http: ClienteHttp('http://x', cliente: BackendFalso(loginCorrecto()).cliente),
          dispositivo: DispositivoEspia(registro)..ids = {'cuentaId': 'u1', 'dispositivoId': 'disp1'},
          grabador: GrabadorFalso(),
          voz: voz,
          comando: vosk,
        ),
      ));
      await tester.pumpAndSettle();
      await tocar(tester, find.text('Iniciar sesión'));
    }

    testWidgets('«Iniciar sesión Nayra» detiene Vosk y luego sigue el inicio de sesión del Paso 2', (tester) async {
      await abrirConVosk(tester);
      expect(voz.dichos.last, 'Hola de nuevo. Di «Iniciar sesión Nayra» o toca el botón de voz.');
      expect(vosk.escuchando, isTrue, reason: 'Vosk escucha cuando Nayra termina de hablar');
      expect(voz.microfono, isTrue, reason: 'mientras Vosk escucha, Nayra no habla');

      vosk.oir('iniciar sesion nayra');
      await tester.pumpAndSettle();
      expect(registro, ['vosk abierto', 'vosk cerrado', 'login iniciado'],
          reason: 'Vosk se detiene antes de empezar el inicio de sesión');
      expect(voz.microfono, isFalse);
      expect(find.text('Ingresa tu PIN'), findsOneWidget);
    });

    testWidgets('una frase distinta no inicia sesión y Vosk sigue escuchando', (tester) async {
      await abrirConVosk(tester);
      vosk.oir('iniciar sesión');
      vosk.oir('quiero entrar');
      await tester.pumpAndSettle();
      expect(registro, ['vosk abierto']);
      expect(find.text('Hola de nuevo'), findsOneWidget);
      // Al salir de la pantalla se detiene.
      await tester.pumpWidget(const SizedBox());
      expect(vosk.escuchando, isFalse);
    });

    testWidgets('si Vosk falla, se informa y el botón de voz sigue iniciando sesión', (tester) async {
      await abrirConVosk(tester, falla: true);
      expect(tester.takeException(), isA<StateError>(), reason: 'el fallo de Vosk se informa');
      expect(vosk.escuchando, isFalse);
      expect(voz.microfono, isFalse, reason: 'Nayra puede volver a hablar');

      await tocar(tester, botonVoz);
      expect(registro, ['login iniciado']);
      expect(find.text('Ingresa tu PIN'), findsOneWidget);
    });

    testWidgets('al destruir la pantalla, Vosk cierra su micrófono y libera la voz', (tester) async {
      await abrirConVosk(tester);
      expect(vosk.escuchando, isTrue);
      await tester.pumpWidget(const SizedBox());
      await tester.pump();
      expect(registro, ['vosk abierto', 'vosk cerrado']);
      expect(voz.microfono, isFalse);
    });
  });

  test('Vosk no escucha mientras Nayra habla; empieza cuando calla', () async {
    final registro = <String>[];
    final vosk = EscuchaFalsa(registro);
    final voz = VozQueHabla()..hablandoAhora.value = true;
    final comando = ComandoInicioSesion(vosk, voz, GrabadorFalso(), alComando: () {});
    addTearDown(comando.dispose);

    await comando.escuchar();
    expect(comando.estado, EstadoComando.esperando);
    expect(registro, isEmpty, reason: 'Vosk no abre el micrófono mientras Nayra habla');

    voz.hablandoAhora.value = false;
    await comando.escuchar();
    expect(comando.estado, EstadoComando.escuchando);
    expect(registro, ['vosk abierto']);
    expect(voz.microfono, isTrue, reason: 'y mientras escucha, Nayra no habla');
    await comando.detener();
    expect(voz.microfono, isFalse);
  });
}
