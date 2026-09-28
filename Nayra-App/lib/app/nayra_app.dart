import 'package:flutter/material.dart';

import '../pantallas/pantalla_bienvenida.dart';
import '../sesion/gestor_sesion.dart';
import '../tema/tema.dart';
import 'dependencias.dart';
import 'navegacion.dart';

class NayraApp extends StatefulWidget {
  const NayraApp({super.key, required this.dependencias, this.inicio});

  final Dependencias dependencias;

  /// Pantalla inicial (pruebas); por defecto la bienvenida.
  final Widget? inicio;

  @override
  State<NayraApp> createState() => _NayraAppState();
}

class _NayraAppState extends State<NayraApp> {
  late final GestorSesion _sesion = widget.dependencias.sesion;
  bool _activa = false;

  @override
  void initState() {
    super.initState();
    _sesion.addListener(_alCambiarSesion);
  }

  /// Cierre por inactividad (D-018), por el servidor (401) o manual (HU-13): se limpia la navegación.
  void _alCambiarSesion() {
    final antes = _activa;
    _activa = _sesion.activa;
    final motivo = _sesion.ultimoCierre;
    if (antes && !_activa && motivo != null) {
      if (motivo == MotivoCierre.manual) {
        Navegacion.alInicio();
      } else {
        Navegacion.aSesionCerrada(motivo);
      }
    }
  }

  @override
  void dispose() {
    _sesion.removeListener(_alCambiarSesion);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => ProveedorNayra(
        dependencias: widget.dependencias,
        child: MaterialApp(
          title: 'Nayra',
          debugShowCheckedModeBanner: false,
          navigatorKey: Navegacion.clave,
          theme: TemaNayra.crear(),
          home: widget.inicio ?? const PantallaBienvenida(),
        ),
      );
}
