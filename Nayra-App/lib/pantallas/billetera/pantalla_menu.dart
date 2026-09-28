import 'package:flutter/material.dart';

import '../../app/dependencias.dart';
import '../../app/navegacion.dart';
import '../../componentes/botones.dart';
import '../../componentes/pantalla.dart';
import '../representante/pantalla_representante.dart';
import 'pantalla_avisos.dart';
import 'pantalla_movimientos.dart';
import 'pantalla_perfil.dart';
import 'pantalla_saldo.dart';
import 'pantalla_transferencia.dart';

/// C1 — Menú principal de la billetera (sesión iniciada). Opciones del primer entregable (01 §12.4):
/// saldo (HU-66), movimientos (HU-67), transferir (HU-69), avisos (HU-77, secundaria) y mis datos (HU-09, HU-15).
/// Si la cuenta es ADMIN se agrega el registro asistido (HU-99), que por ahora solo puede hacer un ADMIN.
class PantallaMenu extends StatelessWidget {
  const PantallaMenu({super.key});

  @override
  Widget build(BuildContext context) {
    final sesion = ProveedorNayra.de(context).sesion;
    return ListenableBuilder(
      listenable: sesion,
      builder: (context, _) {
        final admin = sesion.usuario?.esAdministrador ?? false;
        final opciones = <(String, IconData, TipoBoton, Widget Function())>[
          ('Saldo', Icons.account_balance_wallet, TipoBoton.secundario, () => const PantallaSaldo()),
          ('Movimientos', Icons.list_alt, TipoBoton.secundario, () => const PantallaMovimientos()),
          ('Transferir', Icons.send, TipoBoton.principal, () => const PantallaTransferencia()),
          ('Avisos', Icons.notifications_none, TipoBoton.secundario, () => const PantallaAvisos()),
          ('Mis datos', Icons.person_outline, TipoBoton.secundario, () => const PantallaPerfil()),
          if (admin) ('Registro asistido', Icons.how_to_reg, TipoBoton.secundario, () => const PantallaRepresentante()),
        ];
        final total = opciones.length;
        return PantallaNayra(
          titulo: 'Menú',
          textoVoz: 'Menú. ${[for (var i = 0; i < total; i++) 'Opción ${i + 1} de $total: ${opciones[i].$1}'].join('. ')}.',
          mostrarAtras: false,
          hijos: [
            for (var i = 0; i < total; i++)
              BotonNayra(
                texto: opciones[i].$1,
                etiqueta: '${opciones[i].$1}. Opción ${i + 1} de $total',
                icono: opciones[i].$2,
                tipo: opciones[i].$3,
                alto: 100,
                tamanoIcono: 40,
                alPulsar: () => Navegacion.ir(context, (_) => Protegida(child: opciones[i].$4())),
              ),
          ],
        );
      },
    );
  }
}
