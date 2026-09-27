import 'dart:async';
import 'dart:typed_data';

import 'package:record/record.dart';

import '../config.dart';
import 'wav.dart';

/// Grabación manual (iniciar/detener) de una muestra de voz.
abstract class GrabadorVoz {
  Future<bool> tienePermiso();
  Future<void> iniciar();

  /// Detiene y devuelve el WAV en memoria (no se guarda en disco, D-013).
  Future<Uint8List> detener();
  Future<void> cancelar();
}

class GrabadorMicrofono implements GrabadorVoz {
  final _grabadora = AudioRecorder();
  final _pcm = BytesBuilder(copy: false);
  StreamSubscription<Uint8List>? _suscripcion;
  Timer? _limite;

  @override
  Future<bool> tienePermiso() => _grabadora.hasPermission();

  @override
  Future<void> iniciar() async {
    _pcm.clear();
    final flujo = await _grabadora.startStream(const RecordConfig(
      encoder: AudioEncoder.pcm16bits,
      sampleRate: ConfiguracionApp.frecuenciaMuestreo,
      numChannels: 1,
    ));
    _suscripcion = flujo.listen(_pcm.add);
    // Corte de seguridad a la duración máxima PROVISIONAL.
    _limite = Timer(ConfiguracionApp.duracionMaximaGrabacion, () => _grabadora.stop());
  }

  @override
  Future<Uint8List> detener() async {
    _limite?.cancel();
    await _grabadora.stop();
    await _suscripcion?.cancel();
    final wav = envolverWav(_pcm.takeBytes(), frecuencia: ConfiguracionApp.frecuenciaMuestreo);
    return wav;
  }

  @override
  Future<void> cancelar() async {
    _limite?.cancel();
    await _grabadora.cancel();
    await _suscripcion?.cancel();
    _pcm.clear();
  }
}
