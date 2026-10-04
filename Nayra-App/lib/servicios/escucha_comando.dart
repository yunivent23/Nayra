import 'dart:async';
import 'dart:convert';

import 'package:vosk_flutter_service/vosk_flutter_service.dart';

/// Escucha del comando «Iniciar sesión Nayra» (D-081, MVP del Paso 3). Solo reconoce el comando: no es una
/// captura del flujo, no se envía al servidor y nunca entra al pipeline biométrico (captura ≠ comando, D-075).
abstract class EscuchaComando {
  /// Abre el micrófono y empieza a escuchar. Cada texto reconocido llega a [alTexto]; un fallo durante la escucha
  /// llega a [alFallar]. Lanza si no puede empezar (modelo ausente, sin permiso, micrófono ocupado).
  Future<void> iniciar(void Function(String texto) alTexto, void Function(Object error) alFallar);

  /// Deja de escuchar y cierra el micrófono. Al terminar, el micrófono quedó libre.
  Future<void> detener();
}

/// true si [texto] es exactamente el comando «Iniciar sesión Nayra». Ignora mayúsculas, tildes, signos y espacios
/// de más; no hace coincidencia aproximada. «Neyra» es como el modelo pequeño de español escribe «Nayra», que no
/// está en su vocabulario.
bool esComandoIniciarSesion(String texto) {
  const tildes = {'á': 'a', 'é': 'e', 'í': 'i', 'ó': 'o', 'ú': 'u', 'ü': 'u'};
  final normal = texto
      .toLowerCase()
      .split('')
      .map((c) => tildes[c] ?? c)
      .join()
      .replaceAll(RegExp(r'[^a-zñ ]'), ' ')
      .trim()
      .split(RegExp(r'\s+'))
      .join(' ');
  return normal == 'iniciar sesion nayra' || normal == 'iniciar sesion neyra';
}

/// [EscuchaComando] con Vosk en el celular y una gramática cerrada (D-081).
///
/// El modelo `vosk-model-small-es-0.42` no está en el repositorio: se copia a [rutaModelo] antes de compilar
/// (ver README). Sin el modelo, [iniciar] falla y el botón de voz sigue funcionando.
class EscuchaVosk implements EscuchaComando {
  static const rutaModelo = 'assets/modelos/vosk-model-small-es-0.42.zip';

  /// Gramática cerrada: el comando o «[unk]» (cualquier otra cosa). Probada con el modelo en el servidor de
  /// desarrollo con voz sintética, no en un teléfono.
  static const gramatica = ['iniciar sesión neyra', '[unk]'];

  Future<Recognizer>? _reconocedor;
  SpeechService? _servicio;
  StreamSubscription<String>? _resultados;

  /// El modelo se carga una vez, la primera vez que se escucha. Si falla, se reintenta la próxima vez.
  Future<Recognizer> _preparar() => _reconocedor ??= () async {
        final vosk = VoskFlutterPlugin.instance();
        final ruta = await ModelLoader().loadFromAssets(rutaModelo);
        final modelo = await vosk.createModel(ruta);
        return vosk.createRecognizer(model: modelo, sampleRate: 16000, grammar: gramatica);
      }()
          .catchError((Object e) {
        _reconocedor = null;
        throw e;
      });

  @override
  Future<void> iniciar(void Function(String texto) alTexto, void Function(Object error) alFallar) async {
    final reconocedor = await _preparar();
    // El servicio se crea en cada escucha y se destruye al detener: así el micrófono de Vosk queda liberado antes
    // de que el inicio de sesión abra el suyo.
    final servicio = _servicio = await VoskFlutterPlugin.instance().initSpeechService(reconocedor);
    _resultados = servicio.onResult().listen((r) => alTexto(_texto(r)), onError: alFallar);
    if (await servicio.start(onRecognitionError: alFallar) != true) {
      await detener();
      throw StateError('Vosk no pudo abrir el micrófono');
    }
  }

  static String _texto(String resultado) {
    final datos = jsonDecode(resultado);
    return datos is Map && datos['text'] is String ? datos['text'] as String : '';
  }

  @override
  Future<void> detener() async {
    final servicio = _servicio;
    _servicio = null;
    await _resultados?.cancel();
    _resultados = null;
    if (servicio == null) return;
    try {
      await servicio.cancel();
    } finally {
      // Libera el micrófono (AudioRecord) en Android.
      await servicio.dispose();
    }
  }
}
