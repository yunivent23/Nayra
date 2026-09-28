import 'dart:async';

import 'package:flutter/foundation.dart';

import '../api/cliente_http.dart';
import '../api/usuario_api.dart';
import '../config.dart';
import '../modelos/usuario.dart';

/// Por qué terminó la sesión: define qué pantalla se muestra después.
enum MotivoCierre {
  /// HU-13: la persona cerró su sesión.
  manual,

  /// D-018: 5 minutos sin actividad, sin aviso previo.
  inactividad,

  /// El backend rechazó la sesión (401): vencida, cerrada o revocada (bloqueo, revocación del dispositivo, D-040).
  rechazadaPorServidor,
}

/// Sesión del usuario en la app (D-018).
///
/// - El JWT se guarda SOLO en memoria: nunca en disco, preferencias ni logs. Al cerrar la app, la sesión se pierde.
/// - Sin renovación ni refresh token (D-018).
/// - Inactividad: el servidor cierra la sesión tras 5 minutos sin peticiones. La app refleja esa misma regla: un
///   temporizador que se reinicia con cada respuesta correcta de una petición con sesión; al vencer, descarta el
///   token y muestra que la sesión se cerró. No hay aviso previo ni opción de continuar.
class GestorSesion extends ChangeNotifier {
  GestorSesion(this._http, this._usuarios, {Duration? inactividad})
      : _inactividad = inactividad ?? ConfiguracionApp.inactividadSesion {
    _http
      ..tokenSesion = (() => _token)
      ..alSesionInvalida = (() => _cerrarLocal(MotivoCierre.rechazadaPorServidor))
      ..alPeticionConSesion = _reiniciarTemporizador;
  }

  final ClienteHttp _http;
  final UsuarioApi _usuarios;
  final Duration _inactividad;

  String? _token;
  DatosPropios? _usuario;
  MotivoCierre? _ultimoCierre;
  Timer? _temporizador;

  bool get activa => _token != null;
  DatosPropios? get usuario => _usuario;
  MotivoCierre? get ultimoCierre => _ultimoCierre;

  /// Inicia la sesión con el JWT recibido al autenticar (estado AUTENTICADO) y carga los datos propios.
  Future<DatosPropios> iniciar(String token) async {
    _token = token;
    _ultimoCierre = null;
    _reiniciarTemporizador();
    notifyListeners();
    // Si no se pueden cargar los datos, la sesión sigue: el backend decide si es válida (un 401 la cierra).
    return actualizarUsuario();
  }

  /// Vuelve a pedir los datos propios (HU-09, HU-15).
  Future<DatosPropios> actualizarUsuario() async {
    final datos = await _usuarios.datosPropios();
    _usuario = datos;
    notifyListeners();
    return datos;
  }

  /// HU-13. Avisa al backend (DELETE /sesiones/actual) y descarta el token aunque el aviso falle: la sesión del
  /// servidor también vence sola por inactividad.
  Future<void> cerrar() async {
    if (!activa) return;
    try {
      await _usuarios.cerrarSesion();
    } on FalloNayra {
      // Sin conexión: se cierra igualmente en la app.
    }
    _cerrarLocal(MotivoCierre.manual);
  }

  void _reiniciarTemporizador() {
    _temporizador?.cancel();
    if (_token == null) return;
    _temporizador = Timer(_inactividad, () => _cerrarLocal(MotivoCierre.inactividad));
  }

  void _cerrarLocal(MotivoCierre motivo) {
    if (_token == null) return;
    _temporizador?.cancel();
    _temporizador = null;
    _token = null;
    _usuario = null;
    _ultimoCierre = motivo;
    notifyListeners();
  }

  @override
  void dispose() {
    _temporizador?.cancel();
    super.dispose();
  }
}
