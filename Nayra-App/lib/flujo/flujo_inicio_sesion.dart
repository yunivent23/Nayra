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

  /// true si el último PIN se dictó: Nayra confirma «Recibí seis dígitos» sin repetirlos (D-061, D-075).
  bool _pinDictado = false;

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
    'CUENTA_BLOQUEADA': 'Tu cuenta está bloqueada.',
    'CUENTA_INACTIVA': 'Tu cuenta no está activa.',
    'DISPOSITIVO_NO_VERIFICADO': 'No pude verificar este celular.',
    'TRANSACCION_NO_VALIDA': 'La operación venció. Vuelve a iniciar sesión.',
    'PASO_NO_VALIDO': 'La operación venció. Vuelve a iniciar sesión.',
    'USUARIO_NO_ENCONTRADO': 'No encontré tu cuenta. Vuelve a registrarte.',
  };

  Future<void> iniciar() async {
    _ir(PasoInicioSesion.verificando, 'Estoy verificando este celular.');
    desafio = null;
    intentosRestantes = null;
    try {
      final ids = await _dispositivo.leerIdentificadores();
      final dispositivoId = ids['dispositivoId'];
      if (dispositivoId == null) {
        _ir(PasoInicioSesion.terminado, 'Este celular todavía no tiene una cuenta. Primero regístrate.',
            error: true);
        return;
      }
      final nonce = await _api.nonce(dispositivoId);
      final firma = await _dispositivo.firmar(mensajeAFirmar(nonce, dispositivoId, proposito));
      _transaccion = await _api.abrirTransaccion(dispositivoId, nonce, firma);
      _ir(PasoInicioSesion.pin, mensajePin);
    } on ErrorApi catch (e) {
      _terminarPorError(e);
    } on FalloNayra catch (e) {
      _ir(PasoInicioSesion.terminado, mensajeFallo(e), error: true);
    } on PlatformException {
      _ir(PasoInicioSesion.terminado, 'No pude usar la clave de seguridad de este celular.', error: true);
    }
  }

  /// Lo que Nayra dice al pedir el PIN. Nunca contiene los dígitos.
  static const mensajePin = 'Escribe tu PIN de seis dígitos en el teclado, o toca el botón de voz y díctalo. '
      'Si hay personas cerca, usa el teclado.';

  Future<void> enviarPin(String pin) {
    _pinDictado = false;
    return _paso(() => _api.verificarPin(_transaccion!, pin));
  }

  Future<void> enviarPinDictado(Uint8List wav) {
    _pinDictado = true;
    return _paso(() => _api.verificarPinDictado(_transaccion!, wav));
  }

  Future<void> enviarVoz(Uint8List wav) => _paso(() => _api.verificarVoz(_transaccion!, wav));

  Future<void> _paso(Future<ResultadoPaso> Function() llamada) async {
    final anterior = paso;
    _ir(PasoInicioSesion.verificando,
        anterior == PasoInicioSesion.voz ? 'Estoy verificando tu identidad.' : 'Estoy verificando tu PIN.');
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
        final recibido = _pinDictado ? 'Recibí seis dígitos. ' : '';
        _ir(PasoInicioSesion.voz, '${recibido}PIN correcto. Ahora verificaré tu voz. ${pedirDesafio(desafio!.texto)}');
      case EstadoPaso.autenticado:
        desafio = null;
        _transaccion = null;
        _sesion = r.sesion;
        _ir(PasoInicioSesion.autenticado, 'Identidad verificada.');
      case EstadoPaso.bloqueada:
        desafio = null;
        _transaccion = null;
        _ir(PasoInicioSesion.bloqueada, 'Se agotaron los intentos. Por seguridad, tu cuenta está bloqueada.', error: true);
      case EstadoPaso.rechazado:
        desafio = null;
        _transaccion = null;
        _ir(PasoInicioSesion.terminado, mensajeMotivo(r.motivo), error: true);
      default: // REINTENTAR o SERVICIO_NO_DISPONIBLE: se repite el mismo paso.
        final texto = mensajeMotivo(r.motivo, intentosRestantes: r.intentosRestantes);
        _ir(anterior, anterior == PasoInicioSesion.voz && desafio != null
            ? '$texto ${pedirDesafio(desafio!.texto)}'
            : texto, error: true);
    }
  }
}

/// Pide repetir la frase de desafío con el botón de voz: «Toca el botón y repite: sol, nueve, tres, uno, taza».
String pedirDesafio(String texto) => 'Toca el botón y repite: $texto';
