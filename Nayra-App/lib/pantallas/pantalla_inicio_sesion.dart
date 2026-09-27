import 'package:flutter/material.dart';

import '../flujo/flujo_cuenta.dart';
import '../flujo/flujo_inicio_sesion.dart';
import '../servicios/api_prototipo.dart';
import '../servicios/grabador.dart';
import 'boton_grabacion.dart';
import 'pantalla_cuenta.dart';
import 'teclado_pin.dart';
import 'texto_estado.dart';

class PantallaInicioSesion extends StatefulWidget {
  const PantallaInicioSesion({super.key, required this.api, required this.flujo, required this.grabador});

  final ApiPrototipo api;
  final FlujoInicioSesion flujo;
  final GrabadorVoz grabador;

  @override
  State<PantallaInicioSesion> createState() => _PantallaInicioSesionState();
}

class _PantallaInicioSesionState extends State<PantallaInicioSesion> {
  @override
  void initState() {
    super.initState();
    widget.flujo.iniciar();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Iniciar sesión Nayra')),
      body: SafeArea(
        child: ListenableBuilder(
          listenable: widget.flujo,
          builder: (context, _) {
            final f = widget.flujo;
            return ListView(
              padding: const EdgeInsets.all(16),
              children: [
                TextoEstado(f.mensaje),
                if (f.paso == PasoInicioSesion.verificando) const Center(child: CircularProgressIndicator()),
                if (f.paso == PasoInicioSesion.pin) ...[
                  TecladoPin(alCompletar: f.enviarPin),
                  const SizedBox(height: 16),
                  BotonGrabacion(
                    grabador: widget.grabador,
                    etiqueta: 'Dictar PIN',
                    alTerminar: f.enviarPinDictado,
                  ),
                ],
                if (f.paso == PasoInicioSesion.voz && f.desafio != null) ...[
                  TextoDesafio(f.desafio!.texto),
                  const SizedBox(height: 16),
                  BotonGrabacion(
                    key: ValueKey(f.desafio!.id),
                    grabador: widget.grabador,
                    etiqueta: 'Grabar frase',
                    alTerminar: f.enviarVoz,
                  ),
                ],
                if (f.paso == PasoInicioSesion.autenticado && f.sesion != null)
                  FilledButton(
                    onPressed: () => Navigator.of(context).pushReplacement(MaterialPageRoute(
                        builder: (_) => PantallaCuenta(flujo: FlujoCuenta(widget.api, f.sesion!)))),
                    child: const Text('Ver mis datos'),
                  ),
                if (const {PasoInicioSesion.bloqueada, PasoInicioSesion.terminado}.contains(f.paso))
                  FilledButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Volver')),
              ],
            );
          },
        ),
      ),
    );
  }
}
