/// Configuración de la app del prototipo.
///
/// La URL del backend se define al compilar:
///   flutter run --dart-define=NAYRA_BACKEND_URL=http://10.0.2.2:8080
/// 10.0.2.2 es el equipo anfitrión visto desde el emulador de Android. Sin TLS solo para el
/// prototipo local; el transporte seguro de producción sigue pendiente (06_SEGURIDAD).
/// No hay secretos en la app: la sesión es el JWT que entrega el backend y se guarda solo en memoria.
class ConfiguracionApp {
  static const urlBackend = String.fromEnvironment(
    'NAYRA_BACKEND_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );

  /// PROVISIONAL — PENDIENTE DE VALIDACIÓN: igual que duracion_maxima_s del servicio de voz.
  static const duracionMaximaGrabacion = Duration(seconds: 20);

  /// PROVISIONAL — PENDIENTE DE VALIDACIÓN (D-075): tiempo máximo de una captura según su modo. Es solo un respaldo:
  /// la persona termina la captura con un segundo toque del botón de voz.
  static const capturaMaximaComando = Duration(seconds: 10);
  static const capturaMaximaDictado = Duration(seconds: 15);
  static const capturaMaximaVoz = duracionMaximaGrabacion;

  /// Formato que espera el servicio de voz (05 §27): WAV PCM 16 bits, 16 kHz, mono.
  static const frecuenciaMuestreo = 16000;

  /// Cierre por inactividad (D-018): 5 minutos, controlado en el servidor, sin aviso previo ni opción de continuar.
  /// La app solo lo refleja: cuenta desde la última petición con sesión, igual que el servidor, y al vencer
  /// descarta el token y muestra que la sesión se cerró. No es configurable por el usuario.
  static const inactividadSesion = Duration(minutes: 5);

  /// PROVISIONAL (valores técnicos del cliente, sin decisión documentada): espera máxima de una petición y de una
  /// petición con audio. La de audio supera el tiempo de espera del backend hacia el servicio de voz (30 s).
  static const tiempoEsperaPeticion = Duration(seconds: 20);
  static const tiempoEsperaAudio = Duration(seconds: 45);
}
