import 'package:flutter/material.dart';

import 'colores.dart';

/// Tamaños mínimos de los mockups v3 (criterios de UX para discapacidad visual): texto de 26 px o más en el
/// contenido, botones de ancho completo con alto mínimo de 120 px (teclas del PIN de 84 px) y títulos de 30 px.
/// Se respeta el tamaño de letra elegido en el sistema: ningún texto fija `textScaler`.
abstract final class MedidasNayra {
  static const altoBoton = 120.0;
  static const altoBotonGrande = 160.0;
  static const altoTecla = 84.0;
  static const altoBarra = 86.0;
  static const radio = 26.0;
  static const margen = 16.0;
  static const grosorFoco = 5.0;
}

abstract final class TemaNayra {
  static const fuente = 'AtkinsonHyperlegible';

  static ThemeData crear() {
    const esquema = ColorScheme(
      brightness: Brightness.dark,
      primary: ColoresNayra.accionPrincipal,
      onPrimary: ColoresNayra.azulMarino,
      secondary: ColoresNayra.blanco,
      onSecondary: ColoresNayra.azulMarino,
      error: ColoresNayra.error,
      onError: ColoresNayra.blanco,
      surface: ColoresNayra.azulMarino,
      onSurface: ColoresNayra.blanco,
    );
    const texto = TextTheme(
      displaySmall: TextStyle(fontSize: 52, fontWeight: FontWeight.w700, color: ColoresNayra.foco),
      headlineMedium: TextStyle(fontSize: 34, fontWeight: FontWeight.w700),
      headlineSmall: TextStyle(fontSize: 30, fontWeight: FontWeight.w700, height: 1.15),
      titleLarge: TextStyle(fontSize: 27, fontWeight: FontWeight.w400, height: 1.25),
      titleMedium: TextStyle(fontSize: 24, fontWeight: FontWeight.w700),
      bodyLarge: TextStyle(fontSize: 26, height: 1.25),
      bodyMedium: TextStyle(fontSize: 22, height: 1.25),
      labelLarge: TextStyle(fontSize: 28, fontWeight: FontWeight.w700),
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: esquema,
      scaffoldBackgroundColor: ColoresNayra.azulMarino,
      fontFamily: fuente,
      textTheme: texto.apply(bodyColor: ColoresNayra.blanco, displayColor: ColoresNayra.blanco),
      // Foco visible de alto contraste para teclado externo y switch access.
      focusColor: ColoresNayra.foco.withValues(alpha: 0.35),
      materialTapTargetSize: MaterialTapTargetSize.padded,
      inputDecorationTheme: const InputDecorationTheme(
        filled: true,
        fillColor: ColoresNayra.blanco,
        labelStyle: TextStyle(fontSize: 24, color: ColoresNayra.textoSecundario),
        floatingLabelStyle: TextStyle(fontSize: 24, color: ColoresNayra.azulMarino, fontWeight: FontWeight.w700),
        border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(22))),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.all(Radius.circular(22)),
          borderSide: BorderSide(color: ColoresNayra.foco, width: MedidasNayra.grosorFoco),
        ),
        errorStyle: TextStyle(fontSize: 22, color: ColoresNayra.blanco),
        contentPadding: EdgeInsets.symmetric(horizontal: 20, vertical: 24),
      ),
      progressIndicatorTheme: const ProgressIndicatorThemeData(color: ColoresNayra.foco),
    );
  }
}
