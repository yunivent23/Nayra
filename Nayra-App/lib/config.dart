/// Configuración de la app del prototipo.
///
/// La URL del backend se define al compilar:
///   flutter run --dart-define=NAYRA_BACKEND_URL=http://10.0.2.2:8080
/// 10.0.2.2 es el equipo anfitrión visto desde el emulador de Android. Sin TLS solo para el
/// prototipo local; el transporte seguro de producción sigue pendiente (06_SEGURIDAD).
class ConfiguracionApp {
  static const urlBackend = String.fromEnvironment(
    'NAYRA_BACKEND_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );

  /// PROVISIONAL — PENDIENTE DE VALIDACIÓN: igual que duracion_maxima_s del servicio de voz.
  static const duracionMaximaGrabacion = Duration(seconds: 20);

  /// Formato que espera el servicio de voz (05 §27): WAV PCM 16 bits, 16 kHz, mono.
  static const frecuenciaMuestreo = 16000;
}
