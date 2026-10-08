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

  /// Máximo de celulares por consulta múltiple, igual que en Nayra-Back (aprobado el 2026-10-08).
  static const maxPorConsulta = 500;

  /// Consulta múltiple para la agenda (D-042): `POST /api/v1/destinatarios/busqueda-multiple {celulares}` con
  /// sesión → `{destinatarios: [{celular, nombreVisible}]}`, solo con los números registrados y habilitados.
  /// Una sola llamada: quien la usa ya recortó la lista a [maxPorConsulta] (no hay paginación en este MVP).
  Future<List<Destinatario>> buscarVarios(List<String> celulares) async {
    if (celulares.length > maxPorConsulta) throw ArgumentError('Más de $maxPorConsulta celulares');
    final json = await _http.postJson(_http.uriApi('/destinatarios/busqueda-multiple'), {'celulares': celulares}, true);
    return [
      for (final d in campo<List<dynamic>>(json, 'destinatarios'))
        if (d is Map<String, dynamic>)
          Destinatario(campo<String>(d, 'celular'), campo<String>(d, 'nombreVisible'))
        else
          throw const ErrorRespuesta(),
    ];
  }
}
