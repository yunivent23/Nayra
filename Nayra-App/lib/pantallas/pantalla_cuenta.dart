import 'package:flutter/material.dart';

import '../flujo/flujo_cuenta.dart';
import 'texto_estado.dart';

/// "Mis datos" y cierre de sesión (HU-09, HU-13, HU-15).
class PantallaCuenta extends StatefulWidget {
  const PantallaCuenta({super.key, required this.flujo});

  final FlujoCuenta flujo;

  @override
  State<PantallaCuenta> createState() => _PantallaCuentaState();
}

class _PantallaCuentaState extends State<PantallaCuenta> {
  @override
  void initState() {
    super.initState();
    widget.flujo.cargar();
  }

  Widget _boton(String texto, IconData icono, VoidCallback accion) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: SizedBox(
          height: 72,
          child: FilledButton.icon(
            onPressed: accion,
            icon: Icon(icono, size: 32),
            label: Text(texto, style: const TextStyle(fontSize: 20)),
          ),
        ),
      );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mis datos')),
      body: SafeArea(
        child: ListenableBuilder(
          listenable: widget.flujo,
          builder: (context, _) {
            final f = widget.flujo;
            return ListView(
              padding: const EdgeInsets.all(16),
              children: [
                TextoEstado(f.mensaje),
                if (f.paso == PasoCuenta.cargando) const Center(child: CircularProgressIndicator()),
                if (f.paso == PasoCuenta.datos) ...[
                  _boton('Actualizar', Icons.refresh, f.cargar),
                  _boton('Cerrar sesión', Icons.logout, f.cerrarSesion),
                ],
                if (f.paso == PasoCuenta.cerrada)
                  _boton('Volver al inicio', Icons.home, () => Navigator.of(context).popUntil((r) => r.isFirst)),
              ],
            );
          },
        ),
      ),
    );
  }
}
