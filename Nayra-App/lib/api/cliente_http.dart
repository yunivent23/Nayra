import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:http/http.dart' as http;

import '../config.dart';

/// Fallo al hablar con el backend. Las pantallas nunca ven excepciones de `http`: solo estos tipos,
/// que `mensajeFallo` (flujo/mensajes.dart) convierte en un texto comprensible.
sealed class FalloNayra implements Exception {
  const FalloNayra();
}

/// El backend respondió con un error en su formato único `{"error": "CODIGO"}` (ManejadorErrores.java).
class ErrorApi extends FalloNayra {
  const ErrorApi(this.estadoHttp, this.codigo);
  final int estadoHttp;
  final String codigo;

  @override
  String toString() => 'ErrorApi($estadoHttp, $codigo)';
}

/// Sin conexión con el backend (red caída, servidor apagado, DNS...).
class ErrorConexion extends FalloNayra {
  const ErrorConexion();
}

/// El backend no respondió dentro del tiempo de espera del cliente.
class ErrorTiempoAgotado extends FalloNayra {
  const ErrorTiempoAgotado();
}

/// Respuesta con un formato que no corresponde al contrato (JSON inválido o campos faltantes).
class ErrorRespuesta extends FalloNayra {
  const ErrorRespuesta();
}

/// La función necesita un endpoint que todavía no existe en Nayra-Back. Ver docs/FRONTEND_ENDPOINTS_PENDIENTES.md
/// en Nayra-App. No se simula con datos ficticios.
class FuncionNoDisponible extends FalloNayra {
  const FuncionNoDisponible(this.funcion);
  final String funcion;

  @override
  String toString() => 'FuncionNoDisponible($funcion)';
}

/// Cliente HTTP central de la app: URL base, cabeceras, JWT, tiempos de espera, JSON y errores.
///
/// - Rutas generales bajo `/api/v1` y rutas de voz bajo `/prototipo` (excepción PROVISIONAL del primer entregable;
///   el contrato sigue pendiente en docs/04_API.md, D-014).
/// - El JWT de sesión (D-018) se pide a [tokenSesion] y viaja en `Authorization: Bearer`. Nunca se registra.
/// - Un 401 en una petición con sesión significa que la sesión terminó (inactividad, cierre o revocación):
///   se avisa a [alSesionInvalida] antes de lanzar el error.
/// - Nunca registra cuerpos: pueden contener PIN o audio.
class ClienteHttp {
  ClienteHttp(
    this._base, {
    http.Client? cliente,
    this.tiempoEspera = ConfiguracionApp.tiempoEsperaPeticion,
    this.tiempoEsperaAudio = ConfiguracionApp.tiempoEsperaAudio,
  }) : _http = cliente ?? http.Client();

  final String _base;
  final http.Client _http;
  final Duration tiempoEspera;
  final Duration tiempoEsperaAudio;

  /// Token de la sesión vigente, o null. Lo asigna el gestor de sesión.
  String? Function() tokenSesion = () => null;

  /// Se llama cuando el backend rechaza una sesión (401 con token).
  void Function() alSesionInvalida = () {};

  /// Se llama tras cada respuesta correcta de una petición con sesión (el servidor reinicia su contador de
  /// inactividad en cada petición, D-018).
  void Function() alPeticionConSesion = () {};

  Uri uriApi(String ruta) => Uri.parse('$_base/api/v1$ruta');
  Uri uriPrototipo(String ruta) => Uri.parse('$_base/prototipo$ruta');

  Map<String, String> _cabeceras({required bool conSesion, bool json = false}) {
    final token = conSesion ? tokenSesion() : null;
    if (conSesion && token == null) {
      // Pantalla protegida sin sesión: se trata igual que una sesión vencida.
      throw const ErrorApi(401, 'NO_AUTENTICADO');
    }
    return {
      'Accept': 'application/json',
      if (json) 'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  Future<Map<String, dynamic>> get(Uri uri, {bool conSesion = false}) =>
      _enviar(() => _http.get(uri, headers: _cabeceras(conSesion: conSesion)), conSesion, tiempoEspera);

  Future<Map<String, dynamic>> postJson(Uri uri, [Map<String, Object?>? datos, bool conSesion = false]) => _enviar(
      () => _http.post(uri,
          headers: _cabeceras(conSesion: conSesion, json: true), body: jsonEncode(datos ?? const <String, Object?>{})),
      conSesion,
      tiempoEspera);

  Future<Map<String, dynamic>> delete(Uri uri, {bool conSesion = false}) =>
      _enviar(() => _http.delete(uri, headers: _cabeceras(conSesion: conSesion)), conSesion, tiempoEspera);

  /// Envía un WAV en memoria como `multipart/form-data` en la parte `audio` (contrato de los controllers de voz).
  /// El audio no se escribe en disco ni se conserva (D-013).
  Future<Map<String, dynamic>> postAudio(Uri uri, Uint8List wav, [Map<String, String>? campos]) => _enviar(() async {
        final solicitud = http.MultipartRequest('POST', uri)
          ..headers.addAll(_cabeceras(conSesion: false))
          ..fields.addAll(campos ?? const {})
          ..files.add(http.MultipartFile.fromBytes('audio', wav, filename: 'muestra.wav'));
        return http.Response.fromStream(await _http.send(solicitud));
      }, false, tiempoEsperaAudio);

  Future<Map<String, dynamic>> _enviar(
      Future<http.Response> Function() peticion, bool conSesion, Duration espera) async {
    final http.Response r;
    try {
      r = await peticion().timeout(espera);
    } on TimeoutException {
      throw const ErrorTiempoAgotado();
    } on http.ClientException {
      throw const ErrorConexion();
    }
    final json = _json(r.body);
    if (r.statusCode >= 400) {
      final codigo = json?['error'];
      if (conSesion && r.statusCode == 401) alSesionInvalida();
      throw ErrorApi(r.statusCode, codigo is String ? codigo : 'ERROR');
    }
    if (conSesion) alPeticionConSesion();
    if (r.body.isNotEmpty && json == null) throw const ErrorRespuesta();
    return json ?? <String, dynamic>{};
  }

  static Map<String, dynamic>? _json(String cuerpo) {
    if (cuerpo.isEmpty) return null;
    try {
      final v = jsonDecode(cuerpo);
      return v is Map<String, dynamic> ? v : null;
    } on FormatException {
      return null;
    }
  }
}

/// Lectura estricta de campos: un campo faltante o de otro tipo es un contrato roto, no un valor por defecto.
T campo<T>(Map<String, dynamic> j, String nombre) {
  final v = j[nombre];
  if (v is T) return v;
  throw const ErrorRespuesta();
}

T? campoOpcional<T>(Map<String, dynamic> j, String nombre) {
  final v = j[nombre];
  if (v == null || v is T) return v as T?;
  throw const ErrorRespuesta();
}
