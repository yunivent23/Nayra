import 'package:flutter/material.dart';

import '../../app/dependencias.dart';
import '../../componentes/botones.dart';
import '../../componentes/contenido.dart';
import '../../componentes/pantalla.dart';
import '../../flujo/flujo_billetera.dart';
import '../../modelos/billetera.dart';
import 'estados_consulta.dart';

/// D2 — Avisos de operaciones recibidas (HU-77, secundaria; `nayra.notificaciones`, tipo OPERACION), de uno en
/// uno. Endpoint PENDIENTE en Nayra-Back. Marcar como leído también requiere un endpoint que no existe.
class PantallaAvisos extends StatefulWidget {
  const PantallaAvisos({super.key});

  @override
  State<PantallaAvisos> createState() => _PantallaAvisosState();
}

class _PantallaAvisosState extends State<PantallaAvisos> {
  Consulta<List<Aviso>>? _consulta;
  ListaUnoEnUno<Aviso>? _lista;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _consulta ??= consultaAvisos(ProveedorNayra.de(context).billetera)
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

  static String _lectura(Aviso a, int n, int total) =>
      'Aviso $n de $total${a.leida ? '' : ', nuevo'}. ${a.contenido}';

  @override
  Widget build(BuildContext context) => ListenableBuilder(
        listenable: Listenable.merge([_consulta!, ?_lista]),
        builder: (context, _) {
          final c = _consulta!;
          final l = _lista;
          final a = l?.actual;
          final total = l?.elementos.length ?? 0;
          return PantallaNayra(
            titulo: 'Avisos',
            textoVoz: textoVozConsulta(c,
                cargando: 'Consultando sus avisos.',
                vacio: 'No tiene avisos.',
                listo: a == null ? '' : _lectura(a, l!.indice + 1, total)),
            hijos: [
              ...?estadosConsulta(c, cargando: 'Consultando sus avisos.', vacio: 'No tiene avisos.'),
              if (a != null) ...[
                EtiquetaProgreso('Aviso ${l!.indice + 1} de $total${a.leida ? '' : ' · nuevo'}'),
                TarjetaInfo(principal: a.contenido, secundario: fechaCorta(a.fecha), etiqueta: _lectura(a, l.indice + 1, total)),
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
