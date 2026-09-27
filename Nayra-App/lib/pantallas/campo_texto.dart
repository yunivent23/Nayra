import 'package:flutter/material.dart';

/// Campo de texto grande con etiqueta accesible y botón de envío.
class CampoTexto extends StatefulWidget {
  const CampoTexto({
    super.key,
    required this.etiqueta,
    required this.textoBoton,
    required this.alEnviar,
    this.teclado = TextInputType.text,
  });

  final String etiqueta;
  final String textoBoton;
  final ValueChanged<String> alEnviar;
  final TextInputType teclado;

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
            autocorrect: false,
            style: const TextStyle(fontSize: 22),
            decoration: InputDecoration(labelText: widget.etiqueta, border: const OutlineInputBorder()),
            onSubmitted: (_) => _enviar(),
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 64,
            child: FilledButton(
              onPressed: _enviar,
              child: Text(widget.textoBoton, style: const TextStyle(fontSize: 20)),
            ),
          ),
        ],
      );
}
