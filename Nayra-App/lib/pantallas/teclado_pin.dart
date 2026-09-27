import 'package:flutter/material.dart';

import 'anuncio.dart';

/// Teclado numérico accesible para el PIN de 6 dígitos (D-061).
///
/// - Botones grandes con etiqueta para el lector de pantalla.
/// - Solo se anuncia el progreso ("3 de 6 dígitos"), nunca los dígitos pulsados.
/// - El PIN se mantiene solo en memoria y se entrega al completarse.
class TecladoPin extends StatefulWidget {
  const TecladoPin({super.key, required this.alCompletar, this.habilitado = true});

  final ValueChanged<String> alCompletar;
  final bool habilitado;

  static const longitud = 6;

  @override
  State<TecladoPin> createState() => _TecladoPinState();
}

class _TecladoPinState extends State<TecladoPin> {
  final _digitos = <String>[];

  void _pulsar(String d) {
    if (!widget.habilitado || _digitos.length >= TecladoPin.longitud) return;
    setState(() => _digitos.add(d));
    if (_digitos.length == TecladoPin.longitud) {
      final pin = _digitos.join();
      setState(_digitos.clear);
      anunciar(context, 'PIN completo. Verificando.');
      widget.alCompletar(pin);
    } else {
      anunciar(context, '${_digitos.length} de ${TecladoPin.longitud} dígitos');
    }
  }

  void _borrar() {
    if (_digitos.isEmpty) return;
    setState(_digitos.removeLast);
    anunciar(context, 'Dígito borrado. ${_digitos.length} de ${TecladoPin.longitud} dígitos');
  }

  Widget _tecla(String etiqueta, String semantica, VoidCallback accion) => Padding(
        padding: const EdgeInsets.all(6),
        child: Semantics(
          button: true,
          label: semantica,
          excludeSemantics: true,
          child: SizedBox(
            height: 72,
            child: FilledButton.tonal(
              onPressed: widget.habilitado ? accion : null,
              child: Text(etiqueta, style: const TextStyle(fontSize: 28)),
            ),
          ),
        ),
      );

  @override
  Widget build(BuildContext context) {
    final teclas = <Widget>[
      for (final d in ['1', '2', '3', '4', '5', '6', '7', '8', '9']) _tecla(d, d, () => _pulsar(d)),
      _tecla('⌫', 'Borrar último dígito', _borrar),
      _tecla('0', '0', () => _pulsar('0')),
      const SizedBox.shrink(),
    ];
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Semantics(
          label: '${_digitos.length} de ${TecladoPin.longitud} dígitos ingresados',
          excludeSemantics: true,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              for (var i = 0; i < TecladoPin.longitud; i++)
                Padding(
                  padding: const EdgeInsets.all(6),
                  child: Icon(i < _digitos.length ? Icons.circle : Icons.circle_outlined, size: 18),
                ),
            ],
          ),
        ),
        GridView.count(
          crossAxisCount: 3,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          childAspectRatio: 1.6,
          children: teclas,
        ),
      ],
    );
  }
}
