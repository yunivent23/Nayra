/// Mensajes comprensibles para lectores de pantalla (accesibilidad, 01 §HU-44).
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
    return '$base Le quedan $intentosRestantes ${intentosRestantes == 1 ? 'intento' : 'intentos'}.';
  }
  return base;
}

/// Texto del desafío para leerlo en voz alta: "llave, cuatro, siete, dos, mesa".
String instruccionDesafio(String texto) => 'Diga en voz alta: $texto';
