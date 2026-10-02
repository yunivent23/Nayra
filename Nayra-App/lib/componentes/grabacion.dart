import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../servicios/grabador.dart';
import '../tema/colores.dart';
import 'anuncio.dart';
import 'botones.dart';

/// Botón único de grabación manual (HU-28, HU-42, HU-62): primera pulsación graba, segunda detiene y envía.
/// El audio queda solo en memoria (D-013); si la pantalla se cierra mientras graba, se descarta.
class BotonGrabacion extends StatefulWidget {
  const BotonGrabacion({
    super.key,
    required this.grabador,
    required this.alTerminar,
    this.etiqueta = 'Toque para grabar',
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

  /// Evita una segunda pulsación mientras el micrófono arranca o se detiene.
  bool _ocupado = false;

  Future<void> _alternar() async {
    if (_ocupado) return;
    _ocupado = true;
    try {
      if (_grabando) {
        final wav = await widget.grabador.detener();
        if (!mounted) return;
        setState(() => _grabando = false);
        anunciar(context, 'Grabación terminada. Enviando.');
        widget.alTerminar(wav);
        return;
      }
      if (!await widget.grabador.tienePermiso()) {
        if (mounted) anunciar(context, 'Nayra necesita permiso para usar el micrófono.');
        return;
      }
      await widget.grabador.iniciar();
      if (!mounted) return;
      setState(() => _grabando = true);
      Senales.aviso();
      anunciar(context, 'Grabando. Hable ahora y toque de nuevo al terminar.');
    } catch (_) {
      // Micrófono ocupado o error del sistema al grabar: la app no se cierra; se descarta lo grabado
      // y se puede volver a intentar.
      await widget.grabador.cancelar().catchError((_) {});
      if (!mounted) return;
      setState(() => _grabando = false);
      anunciar(context, 'No se pudo usar el micrófono. Inténtelo otra vez.');
    } finally {
      _ocupado = false;
    }
  }

  @override
  void dispose() {
    if (_grabando) widget.grabador.cancelar();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Column(children: [
        if (_grabando)
          const Padding(
            padding: EdgeInsets.only(bottom: 10),
            child: ExcludeSemantics(
              child: Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                Icon(Icons.graphic_eq, size: 34, color: ColoresNayra.blanco),
                SizedBox(width: 8),
                Text('Grabando', style: TextStyle(fontSize: 26, fontWeight: FontWeight.w700)),
              ]),
            ),
          ),
        BotonNayra(
          texto: _grabando ? 'Toque para detener' : widget.etiqueta,
          etiqueta: _grabando ? 'Detener y enviar grabación' : widget.etiqueta,
          icono: _grabando ? Icons.stop_rounded : Icons.mic,
          tipo: _grabando ? TipoBoton.alerta : TipoBoton.principal,
          alto: 210,
          vertical: true,
          tamanoIcono: 74,
          alPulsar: widget.habilitado ? _alternar : null,
        ),
      ]);
}
