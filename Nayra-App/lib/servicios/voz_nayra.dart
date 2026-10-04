import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter_tts/flutter_tts.dart';

/// Voz propia de Nayra (D-075): Nayra habla con la síntesis de voz del teléfono, sin depender de TalkBack.
///
/// Nunca debe recibir el PIN, la transcripción de un dictado de PIN ni datos biométricos.
abstract class VozNayra {
  /// true mientras Nayra habla. Semidúplex (D-075): mientras habla, el micrófono permanece cerrado.
  ValueListenable<bool> get hablando;

  /// Dice [texto] e interrumpe lo que estuviera diciendo. Termina cuando acaba de decirlo o cuando se interrumpe.
  Future<void> decir(String texto);

  /// Interrumpe lo que Nayra está diciendo (por ejemplo, al tocar el botón de voz). Lanza [FalloVoz] si no se
  /// puede confirmar que el motor calló; en ese caso [hablando] sigue en true.
  Future<void> callar();

  /// Semidúplex (D-075): se llama justo antes de abrir el micrófono. Detiene lo que Nayra esté diciendo y, desde
  /// ese momento hasta [liberarMicrofono], [decir] no habla. Si no se puede confirmar el silencio, lanza
  /// [FalloVoz], el micrófono no queda reservado y no debe abrirse.
  Future<void> reservarMicrofono();

  /// Se llama después de que la grabación se detuvo o se canceló: Nayra puede volver a hablar.
  void liberarMicrofono();
}

/// No se pudo confirmar que el motor de voz dejó de hablar. Mientras tanto, Nayra se considera hablando y el
/// micrófono no se abre.
class FalloVoz implements Exception {
  const FalloVoz(this.causa);
  final Object causa;

  @override
  String toString() => 'FalloVoz: no se pudo detener la síntesis de voz ($causa)';
}

/// [VozNayra] con el motor de síntesis de voz de Android (paquete `flutter_tts`).
class VozTts implements VozNayra {
  final _tts = FlutterTts();
  final _hablando = ValueNotifier(false);
  Completer<void>? _actual;
  Future<void>? _preparada;

  /// Tras interrumpir una frase, el motor puede avisar tarde su cancelación. Hasta que empiece la frase nueva se
  /// ignoran esos avisos para no darla por terminada antes de tiempo.
  bool _esperandoInicio = false;

  /// true mientras el micrófono está reservado o abierto: [decir] no habla.
  bool _microfono = false;

  /// Español de Perú si el motor lo tiene; si no, otra variante de español.
  static const _idiomas = ['es-PE', 'es-US', 'es-419', 'es-ES'];

  Future<void> _preparar() => _preparada ??= _configurar();

  Future<void> _configurar() async {
    for (final idioma in _idiomas) {
      if (await _tts.isLanguageAvailable(idioma) == true) {
        await _tts.setLanguage(idioma);
        break;
      }
    }
    _tts.setStartHandler(() {
      _esperandoInicio = false;
      _hablando.value = true;
    });
    _tts.setCompletionHandler(_alTerminarFrase);
    _tts.setCancelHandler(_alTerminarFrase);
    _tts.setErrorHandler((_) => _terminar());
  }

  void _alTerminarFrase() {
    if (!_esperandoInicio) _terminar();
  }

  void _terminar() {
    _esperandoInicio = false;
    _hablando.value = false;
    final c = _actual;
    _actual = null;
    if (c != null && !c.isCompleted) c.complete();
  }

  @override
  ValueListenable<bool> get hablando => _hablando;

  @override
  Future<void> decir(String texto) async {
    if (texto.isEmpty || _microfono) return;
    await _preparar();
    await callar();
    // Se vuelve a comprobar justo antes de hablar, sin esperas de por medio: el micrófono pudo reservarse
    // mientras se preparaba el motor o se callaba la frase anterior.
    if (_microfono) return;
    final c = _actual = Completer<void>();
    _esperandoInicio = true;
    _hablando.value = true;
    try {
      await _tts.speak(texto);
    } catch (e) {
      // No se sabe si parte de la frase llegó al motor: antes de marcar que Nayra calló se confirma el silencio
      // con stop (si stop también falla, se mantiene «hablando», como en el respaldo). Luego se informa el fallo.
      await _detenerYTerminar(c);
      throw FalloVoz(e);
    }
    // Respaldo por si el motor no avisa el final: tiempo estimado de lectura más un margen.
    return c.future.timeout(Duration(milliseconds: 3000 + texto.length * 90), onTimeout: () => _detenerYTerminar(c));
  }

  /// Vence el respaldo o falla `speak`: primero se detiene el motor y recién después se marca que Nayra dejó de
  /// hablar. Así el micrófono no puede abrirse mientras el teléfono sigue diciendo la frase (semidúplex, D-075).
  /// `stop` responde después de que Android detuvo la síntesis.
  ///
  /// Si `stop` falla, [hablando] sigue en true (no se sabe si el teléfono calló) y la frase termina con
  /// [FalloVoz] para que nadie quede esperándola. La liberan el aviso de fin del motor o un `stop` posterior que
  /// sí responda ([callar], [reservarMicrofono]). Un respaldo antiguo nunca toca una frase nueva.
  Future<void> _detenerYTerminar(Completer<void> c) async {
    if (!identical(_actual, c)) return;
    try {
      await _tts.stop();
    } catch (e) {
      if (identical(_actual, c)) {
        _actual = null;
        // Un aviso tardío de fin del motor confirmará el silencio.
        _esperandoInicio = false;
      }
      throw FalloVoz(e);
    }
    if (identical(_actual, c)) _terminar();
  }

  @override
  Future<void> callar() async {
    if (!_hablando.value && _actual == null) return;
    try {
      await _tts.stop();
    } catch (e) {
      // Sin confirmación de que el motor calló: se mantiene «hablando» para que el micrófono no se abra.
      _esperandoInicio = false;
      throw FalloVoz(e);
    }
    _terminar();
  }

  @override
  Future<void> reservarMicrofono() async {
    _microfono = true;
    try {
      await callar();
    } catch (_) {
      _microfono = false;
      rethrow;
    }
  }

  @override
  void liberarMicrofono() => _microfono = false;
}
