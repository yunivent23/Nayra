import 'package:flutter/foundation.dart';

import '../servicios/api_prototipo.dart';

enum PasoCuenta { cargando, datos, cerrada }

/// Cuenta de acceso con sesión iniciada: "Mis datos" (HU-09, HU-15) y cierre de sesión (HU-13).
/// Si el backend responde 401 la sesión terminó, p. ej. por 5 minutos de inactividad (D-018).
class FlujoCuenta extends ChangeNotifier {
  FlujoCuenta(this._api, this._sesion);

  final ApiPrototipo _api;
  final String _sesion;

  PasoCuenta paso = PasoCuenta.cargando;
  String mensaje = 'Cargando sus datos.';
  DatosPropios? datos;

  void _ir(PasoCuenta nuevo, String texto) {
    paso = nuevo;
    mensaje = texto;
    notifyListeners();
  }

  Future<void> cargar() async {
    _ir(PasoCuenta.cargando, 'Cargando sus datos.');
    try {
      datos = await _api.datosPropios(_sesion);
      final d = datos!;
      _ir(PasoCuenta.datos, 'Nombre: ${d.nombres} ${d.apellidos}. '
          '${d.celular == null ? '' : 'Celular: ${d.celular}. '}'
          'Estado de la cuenta: ${d.estado == 'ACTIVO' ? 'activa' : 'bloqueada'}.');
    } on ErrorApi catch (e) {
      datos = null;
      _ir(PasoCuenta.cerrada, e.estadoHttp == 401
          ? 'Su sesión se cerró. Vuelva a iniciar sesión.'
          : 'No se pudieron cargar sus datos.');
    } catch (_) {
      _ir(PasoCuenta.datos, 'No se pudo conectar con Nayra. Inténtelo otra vez.');
    }
  }

  Future<void> cerrarSesion() async {
    try {
      await _api.cerrarSesion(_sesion);
    } catch (_) {
      // Aunque falle el aviso, la sesión vence sola por inactividad.
    }
    datos = null;
    _ir(PasoCuenta.cerrada, 'Sesión cerrada.');
  }
}
