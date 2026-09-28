import 'package:flutter/material.dart';

import '../../app/dependencias.dart';
import '../../componentes/botones.dart';
import '../../componentes/contenido.dart';
import '../../componentes/pantalla.dart';
import '../../flujo/flujo_billetera.dart';
import '../../modelos/billetera.dart';
import 'estados_consulta.dart';

/// C2 — Saldo de la cuenta financiera simulada (HU-66). Endpoint PENDIENTE en Nayra-Back: mientras no exista,
/// la pantalla informa que la función no está disponible (sin datos ficticios).
class PantallaSaldo extends StatefulWidget {
  const PantallaSaldo({super.key});

  @override
  State<PantallaSaldo> createState() => _PantallaSaldoState();
}

class _PantallaSaldoState extends State<PantallaSaldo> {
  Consulta<Saldo>? _consulta;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _consulta ??= consultaSaldo(ProveedorNayra.de(context).billetera)..cargar();
  }

  @override
  void dispose() {
    _consulta?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => ListenableBuilder(
        listenable: _consulta!,
        builder: (context, _) {
          final c = _consulta!;
          final saldo = c.valor?.disponible;
          return PantallaNayra(
            titulo: 'Su saldo',
            textoVoz: textoVozConsulta(c,
                cargando: 'Consultando su saldo.', vacio: '', listo: saldo == null ? '' : 'Su saldo es ${saldo.hablado}.'),
            hijos: [
              ...?estadosConsulta(c, cargando: 'Consultando su saldo.', vacio: ''),
              if (saldo != null)
                TarjetaInfo(destacado: saldo.texto, principal: 'soles disponibles', etiqueta: 'Su saldo es ${saldo.hablado}.'),
              BotonNayra(
                texto: 'Volver al menú',
                icono: Icons.home,
                tipo: TipoBoton.secundario,
                alPulsar: () => Navigator.of(context).maybePop(),
              ),
            ],
          );
        },
      );
}
