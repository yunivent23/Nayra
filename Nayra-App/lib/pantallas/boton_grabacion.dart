import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../servicios/grabador.dart';
import 'anuncio.dart';

/// Botón único de grabación manual: primera pulsación graba, segunda detiene y envía.
class BotonGrabacion extends StatefulWidget {
  const BotonGrabacion({
    super.key,
    required this.grabador,
    required this.alTerminar,
    this.etiqueta = 'Grabar',
    this.habilitado = true,
  });

  final GrabadorVoz grabador;
  final ValueChanged<Uint8List> alTerminar;
  final String etiqueta;
  final bool habilitado;

  @override
  State<BotonGrabacion> createState() => _BotonGrabacionState();
}

class _BotonGrabacionState extends State<BotonGrabacion> {
  bool _grabando = false;

  Future<void> _alternar() async {
    if (_grabando) {
      final wav = await widget.grabador.detener();
      setState(() => _grabando = false);
      if (mounted) anunciar(context, 'Grabación terminada. Enviando.');
      widget.alTerminar(wav);
      return;
    }
    if (!await widget.grabador.tienePermiso()) {
      if (mounted) anunciar(context, 'Nayra necesita permiso para usar el micrófono.');
      return;
    }
    await widget.grabador.iniciar();
    setState(() => _grabando = true);
    if (mounted) anunciar(context, 'Grabando. Hable ahora y pulse de nuevo al terminar.');
  }

  @override
  void dispose() {
    if (_grabando) widget.grabador.cancelar();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final texto = _grabando ? 'Detener y enviar' : widget.etiqueta;
    return Semantics(
      button: true,
      label: texto,
      excludeSemantics: true,
      child: SizedBox(
        width: double.infinity,
        height: 88,
        child: FilledButton.icon(
          onPressed: widget.habilitado ? _alternar : null,
          icon: Icon(_grabando ? Icons.stop : Icons.mic, size: 36),
          label: Text(texto, style: const TextStyle(fontSize: 22)),
        ),
      ),
    );
  }
}
