import 'package:flutter/material.dart';

import '../../app/dependencias.dart';
import '../../componentes/botones.dart';
import '../../componentes/contenido.dart';
import '../../componentes/pantalla.dart';
import '../../flujo/flujo_billetera.dart';
import '../../modelos/billetera.dart';
import '../../tema/colores.dart';
import 'estados_consulta.dart';

/// C3 — Movimientos de uno en uno con «Anterior» y «Siguiente» (HU-67; estado de la operación HU-87 y código de
/// referencia HU-88, secundarias). Endpoint PENDIENTE en Nayra-Back.
class PantallaMovimientos extends StatefulWidget {
  const PantallaMovimientos({super.key});

  @override
  State<PantallaMovimientos> createState() => _PantallaMovimientosState();
}

class _PantallaMovimientosState extends State<PantallaMovimientos> {
  Consulta<List<Movimiento>>? _consulta;
  ListaUnoEnUno<Movimiento>? _lista;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _consulta ??= consultaMovimientos(ProveedorNayra.de(context).billetera)
        ..addListener(() {
          final v = _consulta!.valor;
          _lista = v == null || v.isEmpty ? null : ListaUnoEnUno(v);
        })
        ..cargar();
  }

  @override
  void dispose() {
    _consulta?.dispose();
    _lista?.dispose();
    super.dispose();
  }

  static String _lectura(Movimiento m, int n, int total) =>
      'Movimiento $n de $total. ${m.enviado ? 'Enviado a' : 'Recibido de'} ${m.contraparte ?? 'otra cuenta'}, '
      '${m.monto.hablado}, ${m.estado.texto.toLowerCase()}. Código ${m.codigoReferencia.split('').join(' ')}.';

  @override
  Widget build(BuildContext context) => ListenableBuilder(
        listenable: Listenable.merge([_consulta!, ?_lista]),
        builder: (context, _) {
          final c = _consulta!;
          final l = _lista;
          final m = l?.actual;
          final total = l?.elementos.length ?? 0;
          return PantallaNayra(
            titulo: 'Movimientos',
            textoVoz: textoVozConsulta(c,
                cargando: 'Consultando sus movimientos.',
                vacio: 'Todavía no tiene movimientos.',
                listo: m == null ? '' : _lectura(m, l!.indice + 1, total)),
            hijos: [
              ...?estadosConsulta(c, cargando: 'Consultando sus movimientos.', vacio: 'Todavía no tiene movimientos.'),
              if (m != null) ...[
                EtiquetaProgreso('Movimiento ${l!.indice + 1} de $total'),
                Semantics(
                  container: true,
                  label: _lectura(m, l.indice + 1, total),
                  excludeSemantics: true,
                  child: _TarjetaMovimiento(m),
                ),
                ParBotones(
                  izquierdo: BotonNayra(
                    texto: 'Anterior',
                    icono: Icons.arrow_back,
                    tipo: TipoBoton.secundario,
                    vertical: true,
                    tamanoTexto: 26,
                    tamanoIcono: 36,
                    alPulsar: l.hayAnterior ? l.anterior : null,
                  ),
                  derecho: BotonNayra(
                    texto: 'Siguiente',
                    icono: Icons.arrow_forward,
                    vertical: true,
                    tamanoTexto: 26,
                    tamanoIcono: 36,
                    alPulsar: l.haySiguiente ? l.siguiente : null,
                  ),
                ),
              ],
            ],
          );
        },
      );
}

class _TarjetaMovimiento extends StatelessWidget {
  const _TarjetaMovimiento(this.m);
  final Movimiento m;

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: ColoresNayra.superficie,
          borderRadius: BorderRadius.circular(26),
          border: Border.all(color: ColoresNayra.azulSecundario, width: 2),
        ),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(m.enviado ? 'Enviado a' : 'Recibido de',
              style: const TextStyle(fontSize: 22, color: ColoresNayra.textoSecundarioSobreOscuro)),
          Text(m.contraparte ?? 'Otra cuenta', style: const TextStyle(fontSize: 32, fontWeight: FontWeight.w700)),
          Text(m.monto.texto, style: Theme.of(context).textTheme.displaySmall),
          const SizedBox(height: 6),
          EstadoOperacionEtiqueta(m.estado),
          const SizedBox(height: 10),
          Text('${fechaCorta(m.fecha)} · Código ${m.codigoReferencia}',
              style: const TextStyle(fontSize: 21, color: ColoresNayra.textoSecundarioSobreOscuro)),
        ]),
      );
}

/// Estado de una operación con icono y texto (no solo color).
class EstadoOperacionEtiqueta extends StatelessWidget {
  const EstadoOperacionEtiqueta(this.estado, {super.key});
  final EstadoOperacion estado;

  @override
  Widget build(BuildContext context) {
    final (fondo, icono) = switch (estado) {
      EstadoOperacion.exitoso => (ColoresNayra.exito, Icons.check),
      EstadoOperacion.fallido => (ColoresNayra.error, Icons.close),
      EstadoOperacion.cancelado => (ColoresNayra.advertencia, Icons.block),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
      decoration: BoxDecoration(color: fondo, borderRadius: BorderRadius.circular(12)),
      child: Row(mainAxisSize: MainAxisSize.min, children: [
        Icon(icono, size: 22, color: ColoresNayra.blanco),
        const SizedBox(width: 6),
        Text(estado.codigo, style: const TextStyle(fontSize: 23, fontWeight: FontWeight.w700, color: ColoresNayra.blanco)),
      ]),
    );
  }
}
