import 'package:flutter/material.dart';

import '../tema/colores.dart';

/// Texto de instrucción grande (26 px o más).
class TextoNayra extends StatelessWidget {
  const TextoNayra(this.texto, {super.key, this.centrado = false, this.secundario = false});
  final String texto;
  final bool centrado;
  final bool secundario;

  @override
  Widget build(BuildContext context) => Text(
        texto,
        textAlign: centrado ? TextAlign.center : TextAlign.start,
        style: TextStyle(
          fontSize: secundario ? 22 : 27,
          height: 1.25,
          color: secundario ? ColoresNayra.textoSecundarioSobreOscuro : ColoresNayra.blanco,
        ),
      );
}

/// Mensaje del estado actual como región viva: el lector lo lee cuando cambia (errores, reintentos).
class MensajeEstado extends StatelessWidget {
  const MensajeEstado(this.texto, {super.key, this.tipo = TipoMensaje.informacion});
  final String texto;
  final TipoMensaje tipo;

  @override
  Widget build(BuildContext context) {
    if (texto.isEmpty) return const SizedBox.shrink();
    final (fondo, icono, prefijo) = switch (tipo) {
      TipoMensaje.informacion => (ColoresNayra.superficie, Icons.info_outline, ''),
      TipoMensaje.exito => (ColoresNayra.exito, Icons.check_circle_outline, 'Correcto. '),
      TipoMensaje.advertencia => (ColoresNayra.advertencia, Icons.warning_amber_rounded, 'Atención. '),
      TipoMensaje.error => (ColoresNayra.error, Icons.error_outline, 'Error. '),
    };
    return Semantics(
      liveRegion: true,
      label: '$prefijo$texto',
      excludeSemantics: true,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: fondo,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: ColoresNayra.blanco, width: 2),
        ),
        child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Icon(icono, size: 34, color: ColoresNayra.blanco),
          const SizedBox(width: 12),
          Expanded(child: Text(texto, style: const TextStyle(fontSize: 24, height: 1.25, color: ColoresNayra.blanco))),
        ]),
      ),
    );
  }
}

enum TipoMensaje { informacion, exito, advertencia, error }

/// Tarjeta con un dato principal grande y una línea secundaria (datos a confirmar, destinatario...).
class TarjetaInfo extends StatelessWidget {
  const TarjetaInfo({super.key, required this.principal, this.secundario, this.destacado, this.etiqueta});
  final String principal;
  final String? secundario;

  /// Importe grande en amarillo encima del texto principal.
  final String? destacado;

  /// Lectura para el lector de pantalla (p. ej. el monto hablado).
  final String? etiqueta;

  @override
  Widget build(BuildContext context) => Semantics(
        container: true,
        label: etiqueta ?? [destacado, principal, secundario].whereType<String>().join('. '),
        excludeSemantics: true,
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            color: ColoresNayra.superficie,
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: ColoresNayra.azulSecundario, width: 2),
          ),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            if (destacado != null)
              Text(destacado!, style: Theme.of(context).textTheme.displaySmall),
            Text(principal, style: const TextStyle(fontSize: 30, fontWeight: FontWeight.w700, height: 1.15)),
            if (secundario != null) ...[
              const SizedBox(height: 6),
              Text(secundario!,
                  style: const TextStyle(fontSize: 24, color: ColoresNayra.textoSecundarioSobreOscuro)),
            ],
          ]),
        ),
      );
}

/// Lista de pares etiqueta / valor (Mis datos). Cada par se lee como un elemento.
class TarjetaDatos extends StatelessWidget {
  const TarjetaDatos(this.datos, {super.key});
  final List<(String, String)> datos;

  @override
  Widget build(BuildContext context) => Container(
        width: double.infinity,
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: ColoresNayra.superficie,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: ColoresNayra.azulSecundario, width: 2),
        ),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          for (final (etiqueta, valor) in datos)
            Semantics(
              container: true,
              label: '$etiqueta: $valor',
              excludeSemantics: true,
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 7),
                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Text(etiqueta,
                      style: const TextStyle(fontSize: 20, color: ColoresNayra.textoSecundarioSobreOscuro)),
                  Text(valor, style: const TextStyle(fontSize: 27, fontWeight: FontWeight.w700, height: 1.15)),
                ]),
              ),
            ),
        ]),
      );
}

/// Etiqueta amarilla de progreso ("Muestra 1 de 3", "Movimiento 2 de 4").
class EtiquetaProgreso extends StatelessWidget {
  const EtiquetaProgreso(this.texto, {super.key});
  final String texto;

  @override
  Widget build(BuildContext context) => Align(
        alignment: Alignment.centerLeft,
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          decoration: BoxDecoration(color: ColoresNayra.foco, borderRadius: BorderRadius.circular(14)),
          child: Text(texto,
              style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w700, color: ColoresNayra.azulMarino)),
        ),
      );
}

enum TipoResultado { exito, advertencia, error, informacion, espera }

/// Resultado grande con círculo e icono (registro completado, identidad verificada, sesión cerrada...).
/// El estado se comunica con icono y texto, no solo con color.
class IconoResultado extends StatelessWidget {
  const IconoResultado(this.tipo, {super.key, this.icono});
  final TipoResultado tipo;
  final IconData? icono;

  @override
  Widget build(BuildContext context) {
    final (fondo, color, iconoBase) = switch (tipo) {
      TipoResultado.exito => (ColoresNayra.exito, ColoresNayra.blanco, Icons.check_rounded),
      TipoResultado.advertencia => (ColoresNayra.advertencia, ColoresNayra.blanco, Icons.schedule),
      TipoResultado.error => (ColoresNayra.error, ColoresNayra.blanco, Icons.close_rounded),
      TipoResultado.informacion => (ColoresNayra.superficie, ColoresNayra.blanco, Icons.info_outline),
      TipoResultado.espera => (ColoresNayra.superficie, ColoresNayra.blanco, Icons.phone_android),
    };
    return ExcludeSemantics(
      child: Center(
        child: Container(
          width: 170,
          height: 170,
          decoration: BoxDecoration(
            color: fondo,
            shape: BoxShape.circle,
            border: Border.all(color: tipo == TipoResultado.espera ? ColoresNayra.foco : ColoresNayra.blanco, width: 5),
          ),
          child: Icon(icono ?? iconoBase, size: 96, color: color),
        ),
      ),
    );
  }
}

/// Carga: indicador y texto que el lector anuncia. Nunca se deja la pantalla vacía durante una petición.
class IndicadorCarga extends StatelessWidget {
  const IndicadorCarga(this.texto, {super.key});
  final String texto;

  @override
  Widget build(BuildContext context) => Semantics(
        liveRegion: true,
        label: texto,
        excludeSemantics: true,
        child: Column(children: [
          const SizedBox(height: 24),
          const SizedBox(
            width: 150,
            height: 150,
            child: CircularProgressIndicator(strokeWidth: 16, backgroundColor: ColoresNayra.superficie),
          ),
          const SizedBox(height: 24),
          TextoNayra(texto, centrado: true),
        ]),
      );
}

/// Frase de desafío (D-054): palabras en blanco y dígitos en amarillo, letra grande. El lector la lee completa.
class FraseDesafio extends StatelessWidget {
  const FraseDesafio(this.texto, {super.key});
  final String texto;

  @override
  Widget build(BuildContext context) {
    final partes = texto.split(RegExp(r'[,\s]+')).where((p) => p.isNotEmpty);
    return Semantics(
      container: true,
      label: 'Frase a decir: $texto',
      excludeSemantics: true,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 18, horizontal: 10),
        decoration: BoxDecoration(
          color: ColoresNayra.superficie,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: ColoresNayra.blanco, width: 3),
        ),
        child: Wrap(
          alignment: WrapAlignment.center,
          spacing: 10,
          runSpacing: 10,
          children: [
            for (final p in partes)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                decoration: BoxDecoration(
                  color: _esNumero(p) ? ColoresNayra.foco : ColoresNayra.blanco,
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Text(p,
                    style: const TextStyle(fontSize: 34, fontWeight: FontWeight.w700, color: ColoresNayra.azulMarino)),
              ),
          ],
        ),
      ),
    );
  }

  static const _numeros = {'cero', 'uno', 'dos', 'tres', 'cuatro', 'cinco', 'seis', 'siete', 'ocho', 'nueve'};
  static bool _esNumero(String p) => RegExp(r'^\d+$').hasMatch(p) || _numeros.contains(p.toLowerCase());
}

/// Indica quién hace el paso (mockups A2 y A3: «Con ayuda del representante», «Lo hace el representante»).
class EtiquetaQuien extends StatelessWidget {
  const EtiquetaQuien(this.texto, {super.key});
  final String texto;

  @override
  Widget build(BuildContext context) => Align(
        alignment: Alignment.centerLeft,
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
          decoration: BoxDecoration(color: ColoresNayra.blanco, borderRadius: BorderRadius.circular(14)),
          child: Row(mainAxisSize: MainAxisSize.min, children: [
            const Icon(Icons.groups, size: 26, color: ColoresNayra.azulMarino),
            const SizedBox(width: 8),
            Flexible(
              child: Text(texto,
                  style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w700, color: ColoresNayra.azulMarino)),
            ),
          ]),
        ),
      );
}
