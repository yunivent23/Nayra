import 'package:flutter/material.dart';

/// Mensaje del paso actual como región viva: el lector de pantalla lo lee cuando cambia.
class TextoEstado extends StatelessWidget {
  const TextoEstado(this.texto, {super.key});
  final String texto;

  @override
  Widget build(BuildContext context) => Semantics(
        liveRegion: true,
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: Text(texto, style: Theme.of(context).textTheme.titleLarge, textAlign: TextAlign.center),
        ),
      );
}

/// Desafío en letra grande; el lector lo lee como frase a repetir.
class TextoDesafio extends StatelessWidget {
  const TextoDesafio(this.texto, {super.key});
  final String texto;

  @override
  Widget build(BuildContext context) => Semantics(
        label: 'Frase a decir: $texto',
        excludeSemantics: true,
        child: Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Text(texto, style: Theme.of(context).textTheme.headlineMedium, textAlign: TextAlign.center),
          ),
        ),
      );
}
