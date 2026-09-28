import '../modelos/usuario.dart';
import 'cliente_http.dart';

/// Cuenta de acceso con sesión (UsuarioController.java, SesionController.java). Rutas PROVISIONALES (D-014).
class UsuarioApi {
  UsuarioApi(this._http);
  final ClienteHttp _http;

  /// GET /api/v1/usuarios/me → DatosPropios (HU-09, HU-15). Requiere sesión.
  Future<DatosPropios> datosPropios() async =>
      DatosPropios.desdeJson(await _http.get(_http.uriApi('/usuarios/me'), conSesion: true));

  /// DELETE /api/v1/sesiones/actual → 204 (HU-13). Requiere sesión.
  Future<void> cerrarSesion() => _http.delete(_http.uriApi('/sesiones/actual'), conSesion: true);
}
