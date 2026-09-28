import '../modelos/registro.dart';
import 'cliente_http.dart';

/// Pasos del registro asistido que hace el representante (D-052, pasos 3–6): AdministracionController.java.
/// Requieren una sesión con rol ADMIN (PROVISIONAL: por ahora solo un ADMIN actúa como representante; D-052 admite
/// también a otra persona autorizada, pendiente). Rutas PROVISIONALES (D-014).
class RepresentanteApi {
  RepresentanteApi(this._http);
  final ClienteHttp _http;

  /// POST /api/v1/admin/registros {tipoDocumentoIdentidad, numeroDocumento} → {codigoRegistro, nombres, apellidos}.
  /// 404 DOCUMENTO_NO_ENCONTRADO; 409 DOCUMENTO_REGISTRADO o CUENTA_FINANCIERA_NO_ENCONTRADA.
  Future<RegistroIniciado> iniciarRegistro(TipoDocumento tipo, String numeroDocumento) async =>
      RegistroIniciado.desdeJson(await _http.postJson(_http.uriApi('/admin/registros'),
          {'tipoDocumentoIdentidad': tipo.codigo, 'numeroDocumento': numeroDocumento}, true));

  /// POST /api/v1/admin/registros/{codigo}/validacion-identidad → 204.
  Future<void> validarIdentidad(String codigo) => _http.postJson(
      _http.uriApi('/admin/registros/${Uri.encodeComponent(codigo)}/validacion-identidad'), null, true);
}
