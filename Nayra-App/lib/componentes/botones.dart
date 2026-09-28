import 'package:flutter/material.dart';

import '../tema/colores.dart';
import '../tema/tema.dart';

enum TipoBoton {
  /// Acción principal: amarilla (una por pantalla).
  principal(ColoresNayra.accionPrincipal, ColoresNayra.azulMarino),

  /// Acción secundaria: blanca.
  secundario(ColoresNayra.blanco, ColoresNayra.azulMarino),

  /// Grabando / acción que detiene.
  alerta(ColoresNayra.error, ColoresNayra.blanco),

  /// Tecla o botón sobre fondo oscuro.
  tecla(ColoresNayra.superficie, ColoresNayra.blanco);

  const TipoBoton(this.fondo, this.texto);
  final Color fondo;
  final Color texto;
}

/// Botón de ancho completo de los mockups v3: grande, con icono y texto, foco visible y una etiqueta accesible
/// que se lee como un solo elemento. Al activarlo con TalkBack (doble toque) ejecuta la misma acción.
class BotonNayra extends StatefulWidget {
  const BotonNayra({
    super.key,
    required this.texto,
    required this.alPulsar,
    this.icono,
    this.tipo = TipoBoton.principal,
    this.alto = MedidasNayra.altoBoton,
    this.subtitulo,
    this.etiqueta,
    this.vertical = false,
    this.tamanoTexto = 30,
    this.tamanoIcono = 44,
  });

  final String texto;
  final VoidCallback? alPulsar;
  final IconData? icono;
  final TipoBoton tipo;
  final double alto;
  final String? subtitulo;

  /// Texto para el lector de pantalla si debe ser distinto del visible.
  final String? etiqueta;

  /// Icono encima del texto (botones de grabación y de acción única).
  final bool vertical;
  final double tamanoTexto;
  final double tamanoIcono;

  @override
  State<BotonNayra> createState() => _BotonNayraState();
}

class _BotonNayraState extends State<BotonNayra> {
  bool _foco = false;

  @override
  Widget build(BuildContext context) {
    final habilitado = widget.alPulsar != null;
    final fondo = habilitado ? widget.tipo.fondo : widget.tipo.fondo.withValues(alpha: 0.45);
    final color = widget.tipo.texto;
    final texto = Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: widget.vertical ? CrossAxisAlignment.center : CrossAxisAlignment.start,
      children: [
        Text(widget.texto,
            textAlign: widget.vertical ? TextAlign.center : TextAlign.start,
            style: TextStyle(fontSize: widget.tamanoTexto, fontWeight: FontWeight.w700, color: color, height: 1.1)),
        if (widget.subtitulo != null)
          Text(widget.subtitulo!, style: TextStyle(fontSize: 20, color: color, height: 1.2)),
      ],
    );
    final icono = widget.icono == null ? null : Icon(widget.icono, size: widget.tamanoIcono, color: color);
    final contenido = widget.vertical
        ? Column(mainAxisAlignment: MainAxisAlignment.center, children: [
            ?icono,
            if (icono != null) const SizedBox(height: 10),
            texto,
          ])
        : Row(mainAxisAlignment: MainAxisAlignment.center, children: [
            ?icono,
            if (icono != null) const SizedBox(width: 18),
            Flexible(child: texto),
          ]);
    return Semantics(
      container: true,
      button: true,
      enabled: habilitado,
      label: widget.etiqueta ?? [widget.texto, if (widget.subtitulo != null) widget.subtitulo].join(', '),
      onTap: widget.alPulsar,
      excludeSemantics: true,
      child: Container(
        // Foco de teclado / accesorios de conmutación: anillo amarillo separado del botón.
        padding: const EdgeInsets.all(4),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(MedidasNayra.radio + 6),
          border: Border.all(color: _foco ? ColoresNayra.foco : Colors.transparent, width: MedidasNayra.grosorFoco),
        ),
        child: Material(
          color: fondo,
          borderRadius: BorderRadius.circular(MedidasNayra.radio),
          child: InkWell(
            borderRadius: BorderRadius.circular(MedidasNayra.radio),
            onTap: widget.alPulsar,
            onFocusChange: (f) => setState(() => _foco = f),
            child: ConstrainedBox(
              constraints: BoxConstraints(minHeight: widget.alto, minWidth: double.infinity),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 14),
                child: Center(child: contenido),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// Dos botones lado a lado (Anterior / Siguiente en las listas de uno en uno).
class ParBotones extends StatelessWidget {
  const ParBotones({super.key, required this.izquierdo, required this.derecho});
  final Widget izquierdo;
  final Widget derecho;

  @override
  Widget build(BuildContext context) => Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [Expanded(child: izquierdo), const SizedBox(width: 6), Expanded(child: derecho)],
      );
}
