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

  /// Interrumpe lo que Nayra está diciendo (por ejemplo, al tocar el botón de voz).
  Future<void> callar();
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
    if (texto.isEmpty) return;
    await _preparar();
    await callar();
    final c = _actual = Completer<void>();
    _esperandoInicio = true;
    _hablando.value = true;
    await _tts.speak(texto);
    // Respaldo por si el motor no avisa el final: tiempo estimado de lectura más un margen.
    return c.future.timeout(Duration(milliseconds: 3000 + texto.length * 90), onTimeout: _terminar);
  }

  @override
  Future<void> callar() async {
    if (!_hablando.value && _actual == null) return;
    await _tts.stop();
    _terminar();
  }
}
