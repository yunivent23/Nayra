import '../modelos/billetera.dart';
import 'cliente_http.dart';

/// Operaciones de la billetera (EP-06; entorno bancario simulado). La búsqueda del destinatario sí existe y está en
/// DestinatarioApi.
///
/// Nayra-Back NO tiene todavía estos endpoints (08_ESTADO_PROYECTO §3.1 y §9: "saldo, movimientos, transferencias
/// y notificaciones no están implementados"; `IOperacionesService` e `INotificacionesService` están vacíos).
/// Esta interfaz deja preparada la integración: cuando existan los controllers, se implementa [BilleteraApi] con
/// [ClienteHttp] respetando su contrato real. Qué debe proveer cada uno: docs/FRONTEND_ENDPOINTS_PENDIENTES.md.
abstract interface class BilleteraApi {
  /// HU-66. Saldo de la única cuenta financiera del usuario de la sesión.
  Future<Saldo> saldo();

  /// HU-67. Movimientos de la cuenta financiera del usuario (enviados y recibidos).
  Future<List<Movimiento>> movimientos();

  /// HU-69, HU-70, HU-72. Ejecuta la transferencia ya confirmada por la persona. El destinatario ya fue localizado
  /// por su celular con DestinatarioApi (G-1); qué dato lo identifica en la solicitud lo fijará el contrato.
  Future<ResultadoTransferencia> transferir(Destinatario destinatario, Soles monto);

  /// HU-77 (secundaria). Avisos de operaciones recibidas.
  Future<List<Aviso>> avisos();
}

/// Implementación mientras faltan los endpoints: no inventa rutas ni devuelve datos ficticios; cada llamada falla
/// con [FuncionNoDisponible] y la pantalla lo informa a la persona.
class BilleteraApiPendiente implements BilleteraApi {
  const BilleteraApiPendiente();

  @override
  Future<Saldo> saldo() => Future.error(const FuncionNoDisponible('saldo'));

  @override
  Future<List<Movimiento>> movimientos() => Future.error(const FuncionNoDisponible('movimientos'));

  @override
  Future<ResultadoTransferencia> transferir(Destinatario destinatario, Soles monto) =>
      Future.error(const FuncionNoDisponible('transferencia'));

  @override
  Future<List<Aviso>> avisos() => Future.error(const FuncionNoDisponible('avisos'));
}
