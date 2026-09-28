import '../modelos/billetera.dart';
import '../modelos/celular.dart';
import 'cliente_http.dart';

/// Búsqueda del destinatario de una transferencia por su celular (G-1, HU-69).
/// Contrato real de Nayra-Back: `POST /api/v1/destinatarios/busqueda {celular}` con sesión →
/// `{nombreVisible}`. Errores: 400 CELULAR_INVALIDO, 404 DESTINATARIO_NO_ENCONTRADO, 409 CUENTAS_IGUALES.
class DestinatarioApi {
  DestinatarioApi(this._http);
  final ClienteHttp _http;

  /// [celular] ya normalizado con [Celular.normalizar]. La respuesta solo dice que existe una cuenta Nayra
  /// asociada a ese número; no prueba quién es titular de la línea.
  Future<Destinatario> buscar(String celular) async {
    final json = await _http.postJson(_http.uriApi('/destinatarios/busqueda'), {'celular': celular}, true);
    return Destinatario(celular, campo<String>(json, 'nombreVisible'));
  }
}
