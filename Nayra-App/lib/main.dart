import 'package:flutter/material.dart';

import 'config.dart';
import 'flujo/flujo_inicio_sesion.dart';
import 'flujo/flujo_registro.dart';
import 'pantallas/pantalla_inicio_sesion.dart';
import 'pantallas/pantalla_registro.dart';
import 'servicios/api_prototipo.dart';
import 'servicios/dispositivo.dart';
import 'servicios/grabador.dart';

void main() {
  runApp(NayraApp(
    api: ApiPrototipo(ConfiguracionApp.urlBackend),
    dispositivo: CanalClaveDispositivo(),
    grabador: GrabadorMicrofono(),
  ));
}

class NayraApp extends StatelessWidget {
  const NayraApp({super.key, required this.api, required this.dispositivo, required this.grabador});

  final ApiPrototipo api;
  final ClaveDispositivo dispositivo;
  final GrabadorVoz grabador;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Nayra',
      theme: ThemeData(colorSchemeSeed: const Color(0xFF1B4F72), useMaterial3: true),
      home: PantallaPrincipal(api: api, dispositivo: dispositivo, grabador: grabador),
    );
  }
}

class PantallaPrincipal extends StatelessWidget {
  const PantallaPrincipal({super.key, required this.api, required this.dispositivo, required this.grabador});

  final ApiPrototipo api;
  final ClaveDispositivo dispositivo;
  final GrabadorVoz grabador;

  Widget _boton(BuildContext context, String texto, IconData icono, WidgetBuilder destino) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: SizedBox(
          height: 96,
          child: FilledButton.icon(
            onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: destino)),
            icon: Icon(icono, size: 36),
            label: Text(texto, style: const TextStyle(fontSize: 22)),
          ),
        ),
      );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Nayra')),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _boton(context, 'Iniciar sesión Nayra', Icons.login,
                (_) => PantallaInicioSesion(api: api, flujo: FlujoInicioSesion(api, dispositivo), grabador: grabador)),
            _boton(context, 'Registrarme con un código', Icons.person_add,
                (_) => PantallaRegistro(flujo: FlujoRegistro(api, dispositivo), grabador: grabador)),
          ],
        ),
      ),
    );
  }
}
