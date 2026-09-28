import 'dart:typed_data';

import '../modelos/autenticacion.dart';
import 'cliente_http.dart';

/// Inicio de sesión (D-037 modificada por D-061): AutenticacionVozController.java, perfil `prototipo`.
/// RUTAS PROVISIONALES (`/prototipo/autenticacion`, D-014). Sin sesión: las protege la firma del dispositivo.
class AutenticacionApi {
  AutenticacionApi(this._http);
  final ClienteHttp _http;

  Uri _uri(String ruta) => _http.uriPrototipo('/autenticacion$ruta');

  /// POST /nonces {dispositivoId} → {nonce, proposito}.
  Future<String> nonce(String dispositivoId) async =>
      campo<String>(await _http.postJson(_uri('/nonces'), {'dispositivoId': dispositivoId}), 'nonce');

  /// POST /transacciones {dispositivoId, nonce, firma} → {transaccionId}. 401 DISPOSITIVO_NO_VERIFICADO si la firma
  /// no es válida; 409 CUENTA_BLOQUEADA o CUENTA_INACTIVA.
  Future<String> abrirTransaccion(String dispositivoId, String nonce, String firma) async => campo<String>(
      await _http.postJson(_uri('/transacciones'), {'dispositivoId': dispositivoId, 'nonce': nonce, 'firma': firma}),
      'transaccionId');

  /// POST /transacciones/{id}/pin {pin} → ResultadoPaso. El PIN viaja una sola vez y no se guarda.
  Future<ResultadoPaso> verificarPin(String transaccionId, String pin) async =>
      ResultadoPaso.desdeJson(await _http.postJson(_uri('/transacciones/${Uri.encodeComponent(transaccionId)}/pin'), {'pin': pin}));

  /// POST multipart /transacciones/{id}/pin-dictado (parte `audio`) → ResultadoPaso.
  Future<ResultadoPaso> verificarPinDictado(String transaccionId, Uint8List wav) async => ResultadoPaso.desdeJson(
      await _http.postAudio(_uri('/transacciones/${Uri.encodeComponent(transaccionId)}/pin-dictado'), wav));

  /// POST multipart /transacciones/{id}/voz (parte `audio`) → ResultadoPaso; con AUTENTICADO trae el JWT.
  Future<ResultadoPaso> verificarVoz(String transaccionId, Uint8List wav) async => ResultadoPaso.desdeJson(
      await _http.postAudio(_uri('/transacciones/${Uri.encodeComponent(transaccionId)}/voz'), wav));
}
