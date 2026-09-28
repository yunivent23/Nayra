import 'package:flutter/painting.dart';

/// Paleta de NAYRA (pedido de Yuni del 2026-09-27, sobre la estructura de los mockups v3: fondo oscuro, acción
/// principal amarilla y acción secundaria blanca). Contrastes calculados (WCAG 2.x):
/// blanco/azul marino 16.5:1, amarillo/azul marino 11.7:1, blanco sobre éxito 5.0:1, advertencia 5.0:1, error 6.6:1.
abstract final class ColoresNayra {
  /// Fondo de todas las pantallas.
  static const azulMarino = Color(0xFF0B1F3A);

  /// Tarjetas y superficies sobre el fondo (blanco encima: 12.5:1).
  static const superficie = Color(0xFF15345E);

  /// Bordes de tarjetas y fondo de las teclas; con texto blanco: 5.8:1.
  static const azulSecundario = Color(0xFF1565C0);

  static const blanco = Color(0xFFFFFFFF);

  /// Texto sobre superficies blancas (campos, botones secundarios).
  static const textoPrincipal = Color(0xFF111111);
  static const textoSecundario = Color(0xFF424242);

  /// Texto secundario sobre el fondo oscuro (11.2:1). Tomado de los mockups v3: #424242 no se lee sobre azul marino.
  static const textoSecundarioSobreOscuro = Color(0xFFC9D6E3);

  static const exito = Color(0xFF087F5B);
  static const advertencia = Color(0xFFB45309);
  static const error = Color(0xFFB42318);

  /// Acción principal y foco del lector de pantalla / teclado.
  static const foco = Color(0xFFFFD600);
  static const accionPrincipal = foco;
}
