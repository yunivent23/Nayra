import '../api/cliente_http.dart';

/// Mensajes que Nayra dice con su voz y muestra en pantalla (HU-47, HU-64), con trato de «tú» (D-075).
/// Nunca incluyen el PIN, puntajes biométricos ni detalles técnicos.
String mensajeMotivo(String? motivo, {int? intentosRestantes}) {
  final base = switch (motivo) {
    'PIN_INCORRECTO' => 'El PIN no es correcto.',
    'PIN_NO_RECONOCIDO' => 'No entendí el PIN dictado. Dilo de nuevo, dígito por dígito.',
    'CALIDAD_INSUFICIENTE' => 'No te escuché con claridad. Busca un lugar tranquilo y repite la frase.',
    'FORMATO_INVALIDO' => 'La grabación no es válida. Inténtalo otra vez.',
    'CONTENIDO_INCORRECTO' => 'La frase no coincide con la solicitada.',
    'POSIBLE_SPOOFING' || 'NO_COINCIDE' => 'No pude verificar tu identidad.',
    'DESAFIO_VENCIDO' => 'La frase anterior venció. Se generó una nueva.',
    'SERVICIO_NO_DISPONIBLE' => 'No puedo verificar tu voz en este momento. Inténtalo en unos minutos.',
    'SIN_REFERENCIA' => 'Debes volver a registrar tu voz.',
    'LIMITE_MUESTRAS' => 'Se alcanzó el máximo de muestras. Vuelve a empezar el registro de voz.',
    _ => 'No se pudo completar el paso.',
  };
  if (intentosRestantes != null && intentosRestantes > 0 &&
      const {'PIN_INCORRECTO', 'CONTENIDO_INCORRECTO', 'POSIBLE_SPOOFING', 'NO_COINCIDE'}.contains(motivo)) {
    return intentosRestantes == 1 ? '$base Te queda 1 intento.' : '$base Te quedan $intentosRestantes intentos.';
  }
  return base;
}

/// Texto del desafío para leerlo en voz alta: "Repite: llave, cuatro, siete, dos, mesa".
String instruccionDesafio(String texto) => 'Repite: $texto';

/// Mensaje para cualquier fallo de comunicación. [porCodigo] permite textos propios de cada pantalla para los
/// códigos de error del backend; el resto recibe un texto genérico.
String mensajeFallo(Object error, {Map<String, String> porCodigo = const {}}) => switch (error) {
      ErrorApi(:final codigo) when porCodigo.containsKey(codigo) => porCodigo[codigo]!,
      ErrorApi(estadoHttp: 401) => 'Tu sesión se cerró. Vuelve a iniciar sesión.',
      ErrorApi(estadoHttp: 403) => 'No tienes permiso para esta acción.',
      ErrorApi(codigo: 'CUENTA_BLOQUEADA') => 'Tu cuenta está bloqueada.',
      ErrorApi(codigo: 'CUENTA_INACTIVA') => 'Tu cuenta no está activa.',
      ErrorApi(estadoHttp: >= 500) => 'Nayra tuvo un problema. Inténtalo más tarde.',
      ErrorApi() => 'No se pudo completar la operación.',
      ErrorConexion() => 'No pude conectarme con Nayra. Revisa tu conexión a internet e inténtalo otra vez.',
      ErrorTiempoAgotado() => 'Nayra tardó demasiado en responder. Inténtalo otra vez.',
      ErrorRespuesta() => 'Nayra respondió de una forma inesperada. Inténtalo más tarde.',
      FuncionNoDisponible() => 'Esta función todavía no está disponible en Nayra.',
      _ => 'Ocurrió un problema inesperado. Inténtalo otra vez.',
    };
