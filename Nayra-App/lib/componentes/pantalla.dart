import 'package:flutter/material.dart';

import '../tema/colores.dart';
import '../tema/tema.dart';
import 'anuncio.dart';
import 'boton_voz.dart';

/// Estructura común de las pantallas de los mockups v3:
/// - título grande y, en el registro, "Paso N de 8";
/// - contenido desplazable (se adapta al tamaño de letra del sistema);
/// - barra fija inferior con "Atrás" y "Repetir", siempre en el mismo lugar, o el botón de voz fijo (D-075).
///
/// [textoVoz] es lo que "Nayra dice" con su propia voz (D-075): se dice al mostrarse la pantalla y cada vez que se toca "Repetir"
/// (HU-54, HU-58, HU-59). Nunca debe contener el PIN.
class PantallaNayra extends StatefulWidget {
  const PantallaNayra({
    super.key,
    required this.textoVoz,
    required this.hijos,
    this.titulo,
    this.paso,
    this.mostrarAtras = true,
    this.alVolver,
    this.cabecera,
    this.botonVoz,
    this.hablarAlMostrar = true,
  });

  final String textoVoz;
  final List<Widget> hijos;
  final String? titulo;
  final String? paso;
  final bool mostrarAtras;

  /// Acción de "Atrás"; por defecto cierra la pantalla.
  final VoidCallback? alVolver;

  /// Contenido fijo encima del título (logo de las pantallas de inicio).
  final Widget? cabecera;

  /// Botón de voz fijo (D-075). Si se indica, reemplaza la barra inferior: el dock incluye «Repetir» y «Atrás».
  final BotonVoz? botonVoz;

  /// false si la pantalla dice [textoVoz] por su cuenta (por ejemplo, después de cargar un dato); «Repetir» lo
  /// sigue diciendo.
  final bool hablarAlMostrar;

  @override
  State<PantallaNayra> createState() => _PantallaNayraState();
}

class _PantallaNayraState extends State<PantallaNayra> {
  @override
  void initState() {
    super.initState();
    if (!widget.hablarAlMostrar) return;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) anunciar(context, widget.textoVoz);
    });
  }

  @override
  void didUpdateWidget(PantallaNayra anterior) {
    super.didUpdateWidget(anterior);
    // La misma pantalla cambia de estado (p. ej. PIN incorrecto): se lee el nuevo mensaje.
    if (widget.hablarAlMostrar && anterior.textoVoz != widget.textoVoz) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) anunciar(context, widget.textoVoz);
      });
    }
  }

  /// «Repetir» del botón de voz: si hay una captura abierta, primero se cierra y recién después habla Nayra.
  Future<void> _repetirConBoton() async {
    await widget.botonVoz?.antesDeRepetir?.call();
    if (mounted) anunciar(context, widget.textoVoz);
  }

  void _volver() {
    if (widget.alVolver != null) {
      widget.alVolver!();
    } else {
      Navigator.of(context).maybePop();
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: widget.mostrarAtras && widget.alVolver == null,
      onPopInvokedWithResult: (hecho, _) {
        if (!hecho && widget.mostrarAtras && widget.alVolver != null) widget.alVolver!();
      },
      child: Scaffold(
        body: SafeArea(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (widget.cabecera != null) widget.cabecera!,
              if (widget.titulo != null || widget.paso != null)
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 12, 20, 8),
                  child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    if (widget.paso != null)
                      Text(widget.paso!,
                          style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w700, color: ColoresNayra.foco)),
                    if (widget.titulo != null)
                      Semantics(
                        header: true,
                        child: Text(widget.titulo!, style: Theme.of(context).textTheme.headlineSmall),
                      ),
                  ]),
                ),
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.fromLTRB(MedidasNayra.margen, 6, MedidasNayra.margen, 14),
                  children: [for (final h in widget.hijos) Padding(padding: const EdgeInsets.only(bottom: 12), child: h)],
                ),
              ),
              if (widget.botonVoz != null)
                DockVoz(
                  boton: widget.botonVoz!,
                  atras: widget.mostrarAtras ? _volver : null,
                  repetir: _repetirConBoton,
                )
              else
                _BarraInferior(
                  atras: widget.mostrarAtras ? _volver : null,
                  repetir: () => anunciar(context, widget.textoVoz),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _BarraInferior extends StatelessWidget {
  const _BarraInferior({required this.atras, required this.repetir});
  final VoidCallback? atras;
  final VoidCallback repetir;

  Widget _boton(String texto, IconData icono, Color borde, Color color, VoidCallback accion, String etiqueta) => Expanded(
        child: Semantics(
          container: true,
          button: true,
          label: etiqueta,
          onTap: accion,
          excludeSemantics: true,
          child: Material(
            color: ColoresNayra.superficie,
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(22), side: BorderSide(color: borde, width: 3)),
            child: InkWell(
              borderRadius: BorderRadius.circular(22),
              onTap: accion,
              child: ConstrainedBox(
                constraints: const BoxConstraints(minHeight: MedidasNayra.altoBarra),
                child: Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                  Icon(icono, size: 34, color: color),
                  const SizedBox(width: 10),
                  Flexible(
                      child: Text(texto, style: TextStyle(fontSize: 25, fontWeight: FontWeight.w700, color: color))),
                ]),
              ),
            ),
          ),
        ),
      );

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(12, 0, 12, 10),
        child: Row(children: [
          if (atras != null) ...[
            _boton('Atrás', Icons.arrow_back, ColoresNayra.azulSecundario, ColoresNayra.blanco, atras!, 'Atrás'),
            const SizedBox(width: 10),
          ],
          _boton('Repetir', Icons.replay, ColoresNayra.foco, ColoresNayra.foco, repetir, 'Repetir instrucción'),
        ]),
      );
}
