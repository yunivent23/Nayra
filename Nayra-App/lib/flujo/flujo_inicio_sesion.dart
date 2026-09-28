import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import '../api/autenticacion_api.dart';
import '../api/cliente_http.dart';
import '../modelos/autenticacion.dart';
import '../servicios/dispositivo.dart';
import 'mensajes.dart';

enum PasoInicioSesion { inicial, verificando, pin, voz, autenticado, bloqueada, terminado }

/// Orquesta en el móvil el flujo de D-037 (modificada por D-061): dispositivo (firma del nonce, D-048) → PIN →
/// desafío (D-054) → voz. La decisión es siempre del backend (D-056); la app solo presenta el resultado.
/// Al autenticar recibe el JWT de sesión (D-018), que se entrega al gestor de sesión y no se guarda en disco.
class FlujoInicioSesion extends ChangeNotifier {
  FlujoInicioSesion(this._api, this._dispositivo);

  static const proposito = 'INICIO_SESION';

  final AutenticacionApi _api;
  final ClaveDispositivo _dispositivo;

  PasoInicioSesion paso = PasoInicioSesion.inicial;
  String mensaje = '';

  /// true si [mensaje] informa un fallo (PIN incorrecto, voz no verificada, sin conexión).
  bool mensajeEsError = false;
  Desafio? desafio;
  int? intentosRestantes;
  String? _sesion;
  String? _transaccion;

  /// JWT recibido con AUTENTICADO (solo en memoria).
  String? get sesion => _sesion;

  /// Entrega el JWT al gestor de sesión y lo borra de este flujo.
  String? tomarSesion() {
    final s = _sesion;
    _sesion = null;
    return s;
  }

  void _ir(PasoInicioSesion nuevo, String texto, {bool error = false}) {
    paso = nuevo;
    mensaje = texto;
    mensajeEsError = error;
    notifyListeners();
  }

  static const _errores = {
    'CUENTA_BLOQUEADA': 'Su cuenta está bloqueada.',
    'CUENTA_INACTIVA': 'Su cuenta no está activa.',
    'DISPOSITIVO_NO_VERIFICADO': 'No se pudo verificar este celular.',
    'TRANSACCION_NO_VALIDA': 'La operación venció. Vuelva a iniciar sesión.',
    'PASO_NO_VALIDO': 'La operación venció. Vuelva a iniciar sesión.',
    'USUARIO_NO_ENCONTRADO': 'No se encontró su cuenta. Vuelva a registrarse con un representante.',
  };

  Future<void> iniciar() async {
    _ir(PasoInicioSesion.verificando, 'Verificando su celular.');
    desafio = null;
    intentosRestantes = null;
    try {
      final ids = await _dispositivo.leerIdentificadores();
      final dispositivoId = ids['dispositivoId'];
      if (dispositivoId == null) {
        _ir(PasoInicioSesion.terminado, 'Este celular no está vinculado a una cuenta. Primero regístrese con un representante.',
            error: true);
        return;
      }
      final nonce = await _api.nonce(dispositivoId);
      final firma = await _dispositivo.firmar(mensajeAFirmar(nonce, dispositivoId, proposito));
      _transaccion = await _api.abrirTransaccion(dispositivoId, nonce, firma);
      _ir(PasoInicioSesion.pin, 'Celular verificado. Ingrese su PIN de 6 dígitos o díctelo.');
    } on ErrorApi catch (e) {
      _terminarPorError(e);
    } on FalloNayra catch (e) {
      _ir(PasoInicioSesion.terminado, mensajeFallo(e), error: true);
    } on PlatformException {
      _ir(PasoInicioSesion.terminado, 'No se pudo usar la clave de seguridad de este celular.', error: true);
    }
  }

  Future<void> enviarPin(String pin) => _paso(() => _api.verificarPin(_transaccion!, pin));

  Future<void> enviarPinDictado(Uint8List wav) => _paso(() => _api.verificarPinDictado(_transaccion!, wav));

  Future<void> enviarVoz(Uint8List wav) => _paso(() => _api.verificarVoz(_transaccion!, wav));

  Future<void> _paso(Future<ResultadoPaso> Function() llamada) async {
    final anterior = paso;
    _ir(PasoInicioSesion.verificando, anterior == PasoInicioSesion.voz ? 'Verificando su voz. Espere, por favor.' : 'Verificando.');
    try {
      aplicar(await llamada(), anterior);
    } on ErrorApi catch (e) {
      _terminarPorError(e);
    } on FalloNayra catch (e) {
      // Sin conexión o sin respuesta: se puede repetir el mismo paso.
      _ir(anterior, mensajeFallo(e), error: true);
    }
  }

  void _terminarPorError(ErrorApi e) {
    desafio = null;
    _transaccion = null;
    _ir(e.codigo == 'CUENTA_BLOQUEADA' ? PasoInicioSesion.bloqueada : PasoInicioSesion.terminado,
        mensajeFallo(e, porCodigo: _errores), error: true);
  }

  @visibleForTesting
  void aplicar(ResultadoPaso r, PasoInicioSesion anterior) {
    if (r.desafio != null) desafio = r.desafio;
    intentosRestantes = r.intentosRestantes;
    switch (r.estado) {
      case EstadoPaso.continuar:
        _ir(PasoInicioSesion.voz, 'PIN correcto. ${instruccionDesafio(desafio!.texto)}');
      case EstadoPaso.autenticado:
        desafio = null;
        _transaccion = null;
        _sesion = r.sesion;
        _ir(PasoInicioSesion.autenticado, 'Identidad verificada.');
      case EstadoPaso.bloqueada:
        desafio = null;
        _transaccion = null;
        _ir(PasoInicioSesion.bloqueada, 'Se agotaron los intentos. Su cuenta está bloqueada.', error: true);
      case EstadoPaso.rechazado:
        desafio = null;
        _transaccion = null;
        _ir(PasoInicioSesion.terminado, mensajeMotivo(r.motivo), error: true);
      default: // REINTENTAR o SERVICIO_NO_DISPONIBLE: se repite el mismo paso.
        final texto = mensajeMotivo(r.motivo, intentosRestantes: r.intentosRestantes);
        _ir(anterior, anterior == PasoInicioSesion.voz && desafio != null
            ? '$texto ${instruccionDesafio(desafio!.texto)}'
            : texto, error: true);
    }
  }
}
