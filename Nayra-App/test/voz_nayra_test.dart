import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart' show SizedBox;
import 'package:flutter_test/flutter_test.dart';
import 'package:nayra_app/api/cliente_http.dart';
import 'package:nayra_app/app/dependencias.dart';
import 'package:nayra_app/componentes/boton_voz.dart';
import 'package:nayra_app/app/nayra_app.dart';
import 'package:nayra_app/servicios/voz_nayra.dart';

import 'app_test.dart' show BackendFalso, botonVoz, loginCorrecto, tocar;
import 'falsos.dart';

const _canal = MethodChannel('flutter_tts');

/// Motor de voz falso en el canal de `flutter_tts`. Anota cada llamada en [registro]; puede fallar o retener
/// `stop` y, si [avisaFin], avisa el inicio y el fin de cada frase como lo hace Android.
class MotorFalso {
  MotorFalso(this.tester, this.registro) {
    tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(_canal, _atender);
    addTearDown(() => tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(_canal, null));
  }

  final WidgetTester tester;
  final List<String> registro;
  bool avisaFin = false;
  bool fallaStop = false;
  bool fallaSpeak = false;

  /// Si se indica, el próximo `stop` no responde hasta que se complete.
  Completer<void>? retenerStop;

  /// Valor de `hablando` cada vez que se llamó a `stop`.
  final hablandoAlDetener = <bool>[];
  VozTts? voz;

  Future<Object?> _atender(MethodCall llamada) async {
    switch (llamada.method) {
      case 'speak':
        final a = llamada.arguments;
        registro.add('habla: ${a is Map ? a['text'] : a}');
        if (fallaSpeak) throw PlatformException(code: 'TTS', message: 'no se pudo enviar la frase');
        if (avisaFin) {
          Timer.run(() async {
            await _avisar('speak.onStart');
            await _avisar('speak.onComplete');
          });
        }
      case 'stop':
        registro.add('stop');
        if (voz != null) hablandoAlDetener.add(voz!.hablando.value);
        final retenido = retenerStop;
        retenerStop = null;
        if (retenido != null) await retenido.future;
        if (fallaStop) throw PlatformException(code: 'TTS', message: 'el motor no respondió');
    }
    return 1;
  }

  Future<void> _avisar(String metodo) => tester.binding.defaultBinaryMessenger
      .handlePlatformMessage(_canal.name, _canal.codec.encodeMethodCall(MethodCall(metodo)), (_) {});

  int get frases => registro.where((r) => r.startsWith('habla: ')).length;
}

/// Grabador que anota en el mismo registro cuándo se abre y se cierra el micrófono.
class GrabadorEspia extends GrabadorFalso {
  GrabadorEspia(this.registro);
  final List<String> registro;

  @override
  Future<void> iniciar() async {
    registro.add('micrófono abierto');
    grabando = true;
  }

  @override
  Future<Uint8List> detener() async {
    registro.add('micrófono cerrado');
    return super.detener();
  }

  @override
  Future<void> cancelar() async {
    if (grabando) registro.add('micrófono cerrado');
    grabando = false;
  }
}

/// Semidúplex (D-075): ninguna frase empieza mientras el micrófono está abierto.
void comprobarSemiduplex(List<String> registro) {
  var abierto = false;
  for (final r in registro) {
    if (r == 'micrófono abierto') abierto = true;
    if (r == 'micrófono cerrado') abierto = false;
    expect(abierto && r.startsWith('habla: '), isFalse, reason: 'Nayra habló con el micrófono abierto: $registro');
  }
}

void main() {
  testWidgets('si el motor no avisa el final, el respaldo detiene la voz antes de marcar que Nayra calló (D-075)',
      (tester) async {
    final motor = MotorFalso(tester, []);
    final voz = motor.voz = VozTts();
    var termino = false;
    voz.decir('Te escucho.').then((_) => termino = true);
    await tester.pump();
    expect(voz.hablando.value, isTrue);
    expect(motor.registro, isNot(contains('stop')));

    // Antes del respaldo (3000 ms + 90 ms por letra) sigue hablando y quien espera la frase no continúa.
    await tester.pump(const Duration(milliseconds: 3000));
    expect(voz.hablando.value, isTrue);
    expect(termino, isFalse);

    await tester.pump(const Duration(seconds: 2));
    expect(motor.registro.last, 'stop');
    expect(motor.hablandoAlDetener, [true], reason: 'se detiene el motor antes de marcar que Nayra dejó de hablar');
    expect(voz.hablando.value, isFalse);
    expect(termino, isTrue);
  });

  testWidgets('si stop falla al vencer el respaldo: error explícito, Nayra sigue «hablando» y el micrófono no se '
      'reserva hasta confirmar el silencio', (tester) async {
    final motor = MotorFalso(tester, [])..fallaStop = true;
    final voz = motor.voz = VozTts();
    Object? error;
    voz.decir('Te escucho.').catchError((Object e) => error = e);
    await tester.pump(const Duration(seconds: 5));
    expect(error, isA<FalloVoz>(), reason: 'quien espera la frase recibe el fallo; no queda esperando');
    expect(voz.hablando.value, isTrue, reason: 'no se sabe si el teléfono calló');

    // Con stop fallando, reservar el micrófono también falla y no queda reservado.
    await expectLater(voz.reservarMicrofono(), throwsA(isA<FalloVoz>()));
    expect(voz.hablando.value, isTrue);

    // Cuando stop vuelve a responder, se confirma el silencio y se puede reservar.
    motor.fallaStop = false;
    await voz.reservarMicrofono();
    expect(voz.hablando.value, isFalse);
    final frases = motor.frases;
    await voz.decir('No debe sonar');
    expect(motor.frases, frases, reason: 'con el micrófono reservado, Nayra no habla');
    voz.liberarMicrofono();
  });

  testWidgets('tras un fallo de stop, el aviso de fin del motor confirma el silencio', (tester) async {
    final motor = MotorFalso(tester, [])..fallaStop = true;
    final voz = motor.voz = VozTts();
    voz.decir('Te escucho.').catchError((_) {});
    await tester.pump(const Duration(seconds: 5));
    expect(voz.hablando.value, isTrue);
    await motor._avisar('speak.onComplete');
    expect(voz.hablando.value, isFalse);
  });

  testWidgets('un respaldo antiguo no termina una frase nueva', (tester) async {
    final motor = MotorFalso(tester, []);
    final voz = motor.voz = VozTts();
    final retenido = motor.retenerStop = Completer<void>();
    voz.decir('uno').catchError((_) {});
    // Vence el respaldo de «uno» (3000 + 3 × 90 ms); su stop queda sin responder.
    await tester.pump(const Duration(milliseconds: 3300));
    expect(motor.registro.last, 'stop');

    var dosTermino = false;
    voz.decir('dos, frase nueva').then((_) => dosTermino = true);
    await tester.pump();
    expect(motor.registro.last, 'habla: dos, frase nueva');

    // Responde por fin el stop antiguo: «dos» sigue en curso.
    retenido.complete();
    await tester.pump();
    expect(voz.hablando.value, isTrue);
    expect(dosTermino, isFalse);

    await tester.pump(const Duration(seconds: 5));
    expect(dosTermino, isTrue);
  });

  testWidgets('con el micrófono reservado ninguna frase empieza, aunque ya estuviera en preparación', (tester) async {
    final motor = MotorFalso(tester, []);
    final voz = motor.voz = VozTts();
    // La frase empieza a prepararse (espera al motor) y en ese intervalo se reserva el micrófono.
    final frase = voz.decir('Repite la instrucción');
    final reserva = voz.reservarMicrofono();
    await tester.pump();
    await frase;
    await reserva;
    expect(motor.frases, 0);
    expect(voz.hablando.value, isFalse);

    await voz.decir('Tampoco esta');
    expect(motor.frases, 0);

    voz.liberarMicrofono();
    voz.decir('Ahora sí').then((_) {});
    await tester.pump();
    expect(motor.frases, 1);
    await tester.pump(const Duration(seconds: 5));
  });

  testWidgets('«Repetir» durante la captura cierra el micrófono y recién después Nayra repite (D-075)',
      (tester) async {
    final registro = <String>[];
    final motor = MotorFalso(tester, registro)..avisaFin = true;
    final grabador = GrabadorEspia(registro);
    tester.view.physicalSize = const Size(1236, 2745);
    tester.view.devicePixelRatio = 3;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(NayraApp(
      dependencias: Dependencias(
        http: ClienteHttp('http://x', cliente: BackendFalso(loginCorrecto()).cliente),
        dispositivo: DispositivoFalso()..ids = {'cuentaId': 'u1', 'dispositivoId': 'disp1'},
        grabador: grabador,
        voz: motor.voz = VozTts(),
      ),
    ));
    await tester.pumpAndSettle();
    await tocar(tester, find.text('Iniciar sesión'));
    await tocar(tester, botonVoz);
    for (final d in ['4', '8', '2', '9', '1', '3']) {
      await tocar(tester, find.bySemanticsLabel(d));
    }
    expect(find.text('Verifiquemos tu voz'), findsOneWidget);

    await tocar(tester, botonVoz);
    expect(grabador.grabando, isTrue);
    expect(find.text('Te escucho'), findsOneWidget);

    final desde = registro.length;
    await tocar(tester, find.bySemanticsLabel('Repetir instrucción'));
    expect(grabador.grabando, isFalse);
    expect(registro.sublist(desde), [
      'micrófono cerrado',
      'habla: PIN correcto. Ahora verificaré tu voz. Toca el botón y repite: barco, cinco, uno, ocho, luna',
    ]);
    expect(find.text('Te escucho'), findsNothing, reason: 'el botón vuelve a esperar');
    expect(find.text('Toca para hablar'), findsOneWidget);
    comprobarSemiduplex(registro);
  });

  testWidgets('mientras el micrófono captura, un mensaje de la pantalla no hace hablar a Nayra', (tester) async {
    final registro = <String>[];
    final voz = VozFalsa();
    final grabador = GrabadorEspia(registro);
    final ciclo = CicloCaptura(voz, grabador);
    addTearDown(ciclo.dispose);
    await ciclo.alternar(ModoCaptura.verificacionVoz, (_) {});
    expect(grabador.grabando, isTrue);
    await voz.decir('Mensaje que llega durante la captura');
    expect(voz.bloqueadas, ['Mensaje que llega durante la captura']);
    await ciclo.cerrar();
    expect(voz.microfono, isFalse);
    await voz.decir('Después de cerrar');
    expect(voz.dichos.last, 'Después de cerrar');
  });

  testWidgets('si speak falla: se confirma el silencio con stop, la frase termina con FalloVoz y no queda bloqueada',
      (tester) async {
    final motor = MotorFalso(tester, [])..fallaSpeak = true;
    final voz = motor.voz = VozTts();
    await expectLater(voz.decir('Te escucho.'), throwsA(isA<FalloVoz>()));
    expect(motor.registro.last, 'stop', reason: 'antes de marcar que calló se detiene el motor');
    expect(motor.hablandoAlDetener.last, isTrue);
    expect(voz.hablando.value, isFalse, reason: 'stop confirmó el silencio: no queda «hablando» para siempre');

    // Sin respaldo pendiente ni frase colgada: se puede reservar el micrófono y luego volver a hablar.
    await voz.reservarMicrofono();
    voz.liberarMicrofono();
    motor.fallaSpeak = false;
    var termino = false;
    voz.decir('Ahora sí').then((_) => termino = true);
    await tester.pump(const Duration(seconds: 5));
    expect(termino, isTrue);
  });

  testWidgets('si speak y stop fallan, sigue «hablando», la frase termina con FalloVoz y el micrófono no se reserva',
      (tester) async {
    final motor = MotorFalso(tester, [])
      ..fallaSpeak = true
      ..fallaStop = true;
    final voz = motor.voz = VozTts();
    await expectLater(voz.decir('Te escucho.'), throwsA(isA<FalloVoz>()));
    expect(voz.hablando.value, isTrue, reason: 'no se pudo confirmar el silencio');
    await expectLater(voz.reservarMicrofono(), throwsA(isA<FalloVoz>()));
    // El aviso de fin del motor confirma el silencio.
    await motor._avisar('speak.onComplete');
    expect(voz.hablando.value, isFalse);
  });

  testWidgets('si la pantalla se cierra mientras se abre el micrófono, el micrófono queda cerrado', (tester) async {
    final registro = <String>[];
    final grabador = GrabadorLento(registro);
    final voz = VozFalsa();
    tester.view.physicalSize = const Size(1236, 2745);
    tester.view.devicePixelRatio = 3;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(NayraApp(
      dependencias: Dependencias(
        http: ClienteHttp('http://x', cliente: BackendFalso(loginCorrecto()).cliente),
        dispositivo: DispositivoFalso()..ids = {'cuentaId': 'u1', 'dispositivoId': 'disp1'},
        grabador: grabador,
        voz: voz,
      ),
    ));
    await tester.pumpAndSettle();
    await tocar(tester, find.text('Iniciar sesión'));
    await tocar(tester, botonVoz);
    for (final d in ['4', '8', '2', '9', '1', '3']) {
      await tocar(tester, find.bySemanticsLabel(d));
    }
    // Se toca el botón y el micrófono empieza a abrirse (el sistema todavía no responde).
    await tester.tap(botonVoz);
    await tester.pump();
    expect(grabador.abriendo, isNotNull);
    expect(voz.microfono, isTrue);

    // La pantalla se destruye antes de que el micrófono termine de abrirse.
    await tester.pumpWidget(const SizedBox());
    grabador.abriendo!.complete();
    await tester.pump();

    expect(registro, ['micrófono abierto', 'micrófono cerrado']);
    expect(grabador.grabando, isFalse);
    expect(voz.microfono, isFalse, reason: 'la voz queda libre después de cerrar el micrófono');
    expect(tester.takeException(), isNull, reason: 'no se actualiza un widget desmontado');
  });
}

/// Grabador cuyo micrófono tarda en abrirse hasta que la prueba completa [abriendo].
class GrabadorLento extends GrabadorEspia {
  GrabadorLento(super.registro);
  Completer<void>? abriendo;

  @override
  Future<void> iniciar() async {
    final c = abriendo = Completer<void>();
    await c.future;
    await super.iniciar();
  }
}
