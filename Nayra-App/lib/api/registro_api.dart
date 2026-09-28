import 'dart:typed_data';

import '../modelos/autenticacion.dart';
import '../modelos/registro.dart';
import 'cliente_http.dart';

/// Pasos del registro inicial asistido en el celular de la persona (D-052, pasos 7–13).
/// RegistroController.java (`/api/v1/registros`) y EnrolamientoVozController.java (`/prototipo/registro`).
/// Sin sesión: los protege el código de registro de un solo uso (mecanismo PROVISIONAL del prototipo, no decisión
/// de D-052). Rutas PROVISIONALES (D-014).
class RegistroApi {
  RegistroApi(this._http);
  final ClienteHttp _http;

  Uri _registro(String codigo, [String ruta = '']) => _http.uriApi('/registros/${Uri.encodeComponent(codigo)}$ruta');
  Uri _enrolamiento(String codigo, String ruta) =>
      _http.uriPrototipo('/registro/${Uri.encodeComponent(codigo)}/enrolamiento$ruta');

  /// GET /api/v1/registros/{codigo} → {nombres, apellidos, paso}. 404 REGISTRO_NO_VALIDO; 409 IDENTIDAD_NO_VALIDADA.
  Future<DatosRegistro> datos(String codigo) async => DatosRegistro.desdeJson(await _http.get(_registro(codigo)));

  /// POST /api/v1/registros/{codigo}/datos {confirmaDatos, celular, pin, clavePublicaDispositivo} → {paso}.
  /// Devuelve DATOS_COMPLETOS o CANCELADO. 400 CELULAR_INVALIDO, PIN_INVALIDO, CLAVE_DISPOSITIVO_INVALIDA.
  Future<String> completarDatos(String codigo,
          {required bool confirma, String? celular, String? pin, String? clavePublicaBase64}) async =>
      campo<String>(
          await _http.postJson(_registro(codigo, '/datos'), {
            'confirmaDatos': confirma,
            'celular': celular,
            'pin': pin,
            'clavePublicaDispositivo': clavePublicaBase64,
          }),
          'paso');

  /// POST /prototipo/registro/{codigo}/enrolamiento/desafios → DesafioDTO.
  Future<Desafio> desafioEnrolamiento(String codigo) async {
    final d = Desafio.desdeJson(await _http.postJson(_enrolamiento(codigo, '/desafios')));
    if (d == null) throw const ErrorRespuesta();
    return d;
  }

  /// POST multipart /prototipo/registro/{codigo}/enrolamiento/muestras (campo `desafioId`, parte `audio`).
  Future<ResultadoMuestra> enviarMuestra(String codigo, String desafioId, Uint8List wav) async =>
      ResultadoMuestra.desdeJson(await _http.postAudio(_enrolamiento(codigo, '/muestras'), wav, {'desafioId': desafioId}));

  /// POST /prototipo/registro/{codigo}/enrolamiento/finalizacion → {correcto, motivo, muestrasValidas}.
  Future<bool> finalizarEnrolamiento(String codigo) async =>
      campo<bool>(await _http.postJson(_enrolamiento(codigo, '/finalizacion')), 'correcto');

  /// POST /api/v1/registros/{codigo}/finalizacion → {usuarioId, dispositivoId}. Paso 13: crea la cuenta de acceso.
  Future<RegistroFinalizado> finalizar(String codigo) async =>
      RegistroFinalizado.desdeJson(await _http.postJson(_registro(codigo, '/finalizacion')));
}
