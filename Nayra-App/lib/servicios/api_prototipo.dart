import 'dart:convert';
import 'dart:typed_data';

import 'package:http/http.dart' as http;

/// Cliente del backend Spring Boot: rutas generales /api/v1 y rutas de voz /prototipo (AG-13).
/// CONTRATO PROVISIONAL — pendiente de docs/04_API.md (D-014).

class Desafio {
  const Desafio(this.id, this.texto);
  final String id;
  final String texto;

  static Desafio? desdeJson(Object? json) {
    if (json is! Map<String, dynamic>) return null;
    return Desafio(json['desafioId'] as String, json['texto'] as String);
  }
}

/// Decisión de Spring Boot en cada paso (D-056). No contiene puntajes biométricos.
class ResultadoPaso {
  const ResultadoPaso(this.estado, this.motivo, this.intentosRestantes, this.desafio, [this.sesion]);
  final String estado;
  final String? motivo;
  final int? intentosRestantes;
  final Desafio? desafio;

  /// Token de sesión, solo con AUTENTICADO (mecanismo PROVISIONAL, D-018). Se guarda solo en memoria.
  final String? sesion;

  factory ResultadoPaso.desdeJson(Map<String, dynamic> j) => ResultadoPaso(
        j['estado'] as String,
        j['motivo'] as String?,
        j['intentosRestantes'] as int?,
        Desafio.desdeJson(j['desafio']),
        j['sesion'] as String?,
      );
}

class ResultadoMuestra {
  const ResultadoMuestra(this.aceptada, this.motivo, this.muestrasValidas, this.muestrasRequeridas);
  final bool aceptada;
  final String? motivo;
  final int? muestrasValidas;
  final int muestrasRequeridas;
}

/// Datos del registro de identidad simulado que la persona confirma (D-052, paso 7).
class DatosRegistro {
  const DatosRegistro(this.nombres, this.apellidos);
  final String nombres;
  final String apellidos;
}

/// Cuenta de acceso creada al finalizar el registro (HU-04).
class RegistroFinalizado {
  const RegistroFinalizado(this.usuarioId, this.dispositivoId);
  final String usuarioId;
  final String dispositivoId;
}

/// Datos propios y estado de la cuenta de acceso (HU-09, HU-15).
class DatosPropios {
  const DatosPropios(this.nombres, this.apellidos, this.celular, this.estado);
  final String nombres;
  final String apellidos;
  final String? celular;
  final String estado;
}

/// Error del backend con su código genérico (p. ej. CUENTA_BLOQUEADA, DISPOSITIVO_NO_VERIFICADO).
class ErrorApi implements Exception {
  const ErrorApi(this.estadoHttp, this.codigo);
  final int estadoHttp;
  final String codigo;
  @override
  String toString() => 'ErrorApi($estadoHttp, $codigo)';
}

class ApiPrototipo {
  ApiPrototipo(this._base, {http.Client? cliente}) : _http = cliente ?? http.Client();

  final String _base;
  final http.Client _http;

  Uri _uri(String ruta) => Uri.parse('$_base/prototipo$ruta');
  Uri _uriApi(String ruta) => Uri.parse('$_base/api/v1$ruta');

  static Map<String, String> _cabeceras({String? sesion, bool json = false}) => {
        if (json) 'Content-Type': 'application/json',
        if (sesion != null) 'Authorization': 'Bearer $sesion',
      };

  Map<String, dynamic> _cuerpo(http.Response r) {
    final cuerpo = r.body.isEmpty ? null : jsonDecode(r.body);
    final json = cuerpo is Map<String, dynamic> ? cuerpo : <String, dynamic>{};
    if (r.statusCode >= 400) {
      throw ErrorApi(r.statusCode, (json['error'] as String?) ?? 'ERROR');
    }
    return json;
  }

  Future<Map<String, dynamic>> _postJson(String ruta, [Map<String, Object?>? datos]) async {
    final r = await _http.post(_uri(ruta), headers: _cabeceras(json: true), body: jsonEncode(datos ?? const {}));
    return _cuerpo(r);
  }

  Future<Map<String, dynamic>> _postApi(String ruta, [Map<String, Object?>? datos]) async {
    final r = await _http.post(_uriApi(ruta), headers: _cabeceras(json: true), body: jsonEncode(datos ?? const {}));
    return _cuerpo(r);
  }

  Future<Map<String, dynamic>> _postAudio(String ruta, Uint8List wav, [Map<String, String>? campos]) async {
    final solicitud = http.MultipartRequest('POST', _uri(ruta))
      ..fields.addAll(campos ?? const {})
      ..files.add(http.MultipartFile.fromBytes('audio', wav, filename: 'muestra.wav'));
    return _cuerpo(await http.Response.fromStream(await _http.send(solicitud)));
  }

  // ---- Registro inicial asistido (D-052), pasos en el celular de la persona ----
  // El representante entrega un código de registro de un solo uso (mecanismo PROVISIONAL del prototipo,
  // no decisión de D-052).

  Future<DatosRegistro> datosRegistro(String codigo) async {
    final j = _cuerpo(await _http.get(_uriApi('/registros/${Uri.encodeComponent(codigo)}')));
    return DatosRegistro(j['nombres'] as String, j['apellidos'] as String);
  }

  /// Pasos 7–10. Devuelve el paso resultante (DATOS_COMPLETOS o CANCELADO).
  Future<String> completarDatos(String codigo,
      {required bool confirma, String? celular, String? pin, String? clavePublicaBase64}) async {
    final j = await _postApi('/registros/${Uri.encodeComponent(codigo)}/datos', {
      'confirmaDatos': confirma,
      'celular': celular,
      'pin': pin,
      'clavePublicaDispositivo': clavePublicaBase64,
    });
    return j['paso'] as String;
  }

  Future<Desafio> desafioEnrolamiento(String codigo) async =>
      Desafio.desdeJson(await _postJson('/registro/${Uri.encodeComponent(codigo)}/enrolamiento/desafios'))!;

  Future<ResultadoMuestra> enviarMuestra(String codigo, String desafioId, Uint8List wav) async {
    final j = await _postAudio('/registro/${Uri.encodeComponent(codigo)}/enrolamiento/muestras', wav, {'desafioId': desafioId});
    return ResultadoMuestra(
        j['aceptada'] as bool, j['motivo'] as String?, j['muestrasValidas'] as int?, j['muestrasRequeridas'] as int);
  }

  Future<bool> finalizarEnrolamiento(String codigo) async =>
      (await _postJson('/registro/${Uri.encodeComponent(codigo)}/enrolamiento/finalizacion'))['correcto'] as bool;

  /// Paso 13: crea la cuenta de acceso y vincula el dispositivo.
  Future<RegistroFinalizado> finalizarRegistro(String codigo) async {
    final j = await _postApi('/registros/${Uri.encodeComponent(codigo)}/finalizacion');
    return RegistroFinalizado(j['usuarioId'] as String, j['dispositivoId'] as String);
  }

  // ---- Cuenta de acceso con sesión (D-018; mecanismo PROVISIONAL) ----

  Future<DatosPropios> datosPropios(String sesion) async {
    final j = _cuerpo(await _http.get(_uriApi('/usuarios/me'), headers: _cabeceras(sesion: sesion)));
    return DatosPropios(j['nombres'] as String, j['apellidos'] as String, j['celular'] as String?, j['estado'] as String);
  }

  /// HU-13.
  Future<void> cerrarSesion(String sesion) async =>
      _cuerpo(await _http.delete(_uriApi('/sesiones/actual'), headers: _cabeceras(sesion: sesion)));

  // ---- Inicio de sesión (D-037 modificada por D-061) ----

  Future<String> nonce(String dispositivoId) async =>
      (await _postJson('/autenticacion/nonces', {'dispositivoId': dispositivoId}))['nonce'] as String;

  Future<String> abrirTransaccion(String dispositivoId, String nonce, String firma) async =>
      (await _postJson('/autenticacion/transacciones',
          {'dispositivoId': dispositivoId, 'nonce': nonce, 'firma': firma}))['transaccionId'] as String;

  Future<ResultadoPaso> verificarPin(String transaccionId, String pin) async =>
      ResultadoPaso.desdeJson(await _postJson('/autenticacion/transacciones/$transaccionId/pin', {'pin': pin}));

  Future<ResultadoPaso> verificarPinDictado(String transaccionId, Uint8List wav) async =>
      ResultadoPaso.desdeJson(await _postAudio('/autenticacion/transacciones/$transaccionId/pin-dictado', wav));

  Future<ResultadoPaso> verificarVoz(String transaccionId, Uint8List wav) async =>
      ResultadoPaso.desdeJson(await _postAudio('/autenticacion/transacciones/$transaccionId/voz', wav));
}
