import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../tema/colores.dart';
import 'botones.dart';

/// Campo de texto grande con etiqueta accesible y botón de envío de ancho completo.
class CampoTexto extends StatefulWidget {
  const CampoTexto({
    super.key,
    required this.etiqueta,
    required this.textoBoton,
    required this.alEnviar,
    this.teclado = TextInputType.text,
    this.iconoBoton = Icons.chevron_right,
    this.formatos,
    this.pista,
  });

  final String etiqueta;
  final String textoBoton;
  final ValueChanged<String> alEnviar;
  final TextInputType teclado;
  final IconData iconoBoton;
  final List<TextInputFormatter>? formatos;
  final String? pista;

  @override
  State<CampoTexto> createState() => _CampoTextoState();
}

class _CampoTextoState extends State<CampoTexto> {
  final _controlador = TextEditingController();

  @override
  void dispose() {
    _controlador.dispose();
    super.dispose();
  }

  void _enviar() => widget.alEnviar(_controlador.text);

  @override
  Widget build(BuildContext context) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          TextField(
            controller: _controlador,
            keyboardType: widget.teclado,
            inputFormatters: widget.formatos,
            autocorrect: false,
            enableSuggestions: false,
            style: const TextStyle(fontSize: 34, fontWeight: FontWeight.w700, color: ColoresNayra.textoPrincipal),
            cursorColor: ColoresNayra.azulMarino,
            cursorWidth: 4,
            decoration: InputDecoration(labelText: widget.etiqueta, helperText: widget.pista,
                helperStyle: const TextStyle(fontSize: 20, color: ColoresNayra.textoSecundarioSobreOscuro),
                helperMaxLines: 3),
            onSubmitted: (_) => _enviar(),
          ),
          const SizedBox(height: 16),
          BotonNayra(texto: widget.textoBoton, icono: widget.iconoBoton, alPulsar: _enviar),
        ],
      );
}
