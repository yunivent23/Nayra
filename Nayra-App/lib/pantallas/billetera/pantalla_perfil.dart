import 'package:flutter/material.dart';

import '../../api/cliente_http.dart';
import '../../app/dependencias.dart';
import '../../componentes/botones.dart';
import '../../componentes/contenido.dart';
import '../../componentes/pantalla.dart';
import '../../flujo/mensajes.dart';
import '../../modelos/usuario.dart';

/// D1 — Mis datos (HU-09) y estado de la cuenta (HU-15) desde GET /api/v1/usuarios/me, y cierre de sesión (HU-13,
/// DELETE /api/v1/sesiones/actual). La actualización de datos (HU-10) no está en el alcance implementado.
class PantallaPerfil extends StatefulWidget {
  const PantallaPerfil({super.key});

  @override
  State<PantallaPerfil> createState() => _PantallaPerfilState();
}

class _PantallaPerfilState extends State<PantallaPerfil> {
  bool _cargando = true;
  bool _cerrando = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) _cargar();
    });
  }

  Future<void> _cargar() async {
    setState(() {
      _cargando = true;
      _error = null;
    });
    try {
      await ProveedorNayra.de(context).sesion.actualizarUsuario();
    } on FalloNayra catch (e) {
      // Un 401 lo resuelve el gestor de sesión (vuelve a la pantalla de sesión cerrada).
      _error = mensajeFallo(e);
    }
    if (mounted) setState(() => _cargando = false);
  }

  Future<void> _cerrarSesion() async {
    setState(() => _cerrando = true);
    await ProveedorNayra.de(context).sesion.cerrar();
  }

  @override
  Widget build(BuildContext context) {
    final u = ProveedorNayra.de(context).sesion.usuario;
    final datos = u == null
        ? null
        : [
            ('Nombre', u.nombreCompleto),
            if (u.celular != null) ('Celular', u.celular!),
            ('Cuenta', textoEstadoCuenta(u.estado)),
          ];
    final String voz;
    if (_cerrando) {
      voz = 'Cerrando sesión.';
    } else if (_cargando) {
      voz = 'Cargando sus datos.';
    } else if (_error != null) {
      voz = _error!;
    } else {
      voz = '${datos!.map((d) => '${d.$1}: ${d.$2}').join('. ')}.';
    }
    return PantallaNayra(
      titulo: 'Mis datos',
      textoVoz: voz,
      hijos: [
        if (_cerrando || (_cargando && datos == null)) IndicadorCarga(voz),
        if (!_cerrando && _error != null) ...[
          MensajeEstado(_error!, tipo: TipoMensaje.error),
          BotonNayra(texto: 'Intentar de nuevo', icono: Icons.replay, alPulsar: _cargar),
        ],
        if (!_cerrando && datos != null) TarjetaDatos(datos),
        if (!_cerrando)
          BotonNayra(texto: 'Cerrar sesión', icono: Icons.logout, tipo: TipoBoton.secundario, alPulsar: _cerrarSesion),
      ],
    );
  }
}
