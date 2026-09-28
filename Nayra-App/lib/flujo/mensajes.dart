import '../api/cliente_http.dart';

/// Mensajes comprensibles para lectores de pantalla (accesibilidad, HU-47, HU-64).
/// Nunca incluyen el PIN, puntajes biométricos ni detalles técnicos.
String mensajeMotivo(String? motivo, {int? intentosRestantes}) {
  final base = switch (motivo) {
    'PIN_INCORRECTO' => 'El PIN no es correcto.',
    'PIN_NO_RECONOCIDO' => 'No se entendió el PIN dictado. Dígalo de nuevo, dígito por dígito.',
    'CALIDAD_INSUFICIENTE' => 'No se escuchó con claridad. Busque un lugar tranquilo y repita la frase.',
    'FORMATO_INVALIDO' => 'La grabación no es válida. Inténtelo otra vez.',
    'CONTENIDO_INCORRECTO' => 'La frase no coincide con la solicitada.',
    'POSIBLE_SPOOFING' || 'NO_COINCIDE' => 'No se pudo verificar su voz.',
    'DESAFIO_VENCIDO' => 'La frase anterior venció. Se generó una nueva.',
    'SERVICIO_NO_DISPONIBLE' => 'El servicio no está disponible en este momento. Inténtelo más tarde.',
    'SIN_REFERENCIA' => 'Debe volver a registrar su voz.',
    'LIMITE_MUESTRAS' => 'Se alcanzó el máximo de muestras. Vuelva a empezar el registro de voz.',
    _ => 'No se pudo completar el paso.',
  };
  if (intentosRestantes != null && intentosRestantes > 0 &&
      const {'PIN_INCORRECTO', 'CONTENIDO_INCORRECTO', 'POSIBLE_SPOOFING', 'NO_COINCIDE'}.contains(motivo)) {
    return intentosRestantes == 1 ? '$base Le queda 1 intento.' : '$base Le quedan $intentosRestantes intentos.';
  }
  return base;
}

/// Texto del desafío para leerlo en voz alta: "llave, cuatro, siete, dos, mesa".
String instruccionDesafio(String texto) => 'Diga en voz alta: $texto';

/// Mensaje para cualquier fallo de comunicación. [porCodigo] permite textos propios de cada pantalla para los
/// códigos de error del backend; el resto recibe un texto genérico.
String mensajeFallo(Object error, {Map<String, String> porCodigo = const {}}) => switch (error) {
      ErrorApi(:final codigo) when porCodigo.containsKey(codigo) => porCodigo[codigo]!,
      ErrorApi(estadoHttp: 401) => 'Su sesión se cerró. Vuelva a iniciar sesión.',
      ErrorApi(estadoHttp: 403) => 'No tiene permiso para esta acción.',
      ErrorApi(codigo: 'CUENTA_BLOQUEADA') => 'Su cuenta está bloqueada.',
      ErrorApi(codigo: 'CUENTA_INACTIVA') => 'Su cuenta no está activa.',
      ErrorApi(estadoHttp: >= 500) => 'Nayra tuvo un problema. Inténtelo más tarde.',
      ErrorApi() => 'No se pudo completar la operación.',
      ErrorConexion() => 'No se pudo conectar con Nayra. Revise su conexión a internet e inténtelo otra vez.',
      ErrorTiempoAgotado() => 'Nayra tardó demasiado en responder. Inténtelo otra vez.',
      ErrorRespuesta() => 'Nayra respondió de una forma inesperada. Inténtelo más tarde.',
      FuncionNoDisponible() => 'Esta función todavía no está disponible en Nayra.',
      _ => 'Ocurrió un problema inesperado. Inténtelo otra vez.',
    };
