import 'package:flutter/foundation.dart';

import '../servicios/escucha_comando.dart';
import '../servicios/grabador.dart';
import '../servicios/voz_nayra.dart';

/// Estados de la escucha del comando «Iniciar sesión Nayra» (D-081). No son estados visibles del botón de voz:
/// el botón conserva sus siete estados (D-075).
enum EstadoComando { inactivo, esperando, escuchando, reconocido, deteniendo, iniciandoLogin, error }

/// Escucha automática del comando en la primera pantalla del inicio de sesión (D-081).
///
/// Semidúplex (D-075): mientras Vosk escucha, la voz de Nayra tiene el micrófono reservado y no habla; Vosk solo
/// empieza cuando Nayra calló. Al reconocer el comando, primero se detiene Vosk y se libera el micrófono, y recién
/// después se llama a [alComando] (el mismo inicio que el botón de voz). Si Vosk falla, se informa el error y
/// el botón de voz sigue funcionando.
class ComandoInicioSesion extends ChangeNotifier {
  ComandoInicioSesion(this._escucha, this._voz, this._grabador, {required this.alComando});

  final EscuchaComando? _escucha;
  final VozNayra _voz;
  final GrabadorVoz _grabador;
  final VoidCallback alComando;

  EstadoComando _estado = EstadoComando.inactivo;
  bool _desechado = false;

  /// Arranque de Vosk en curso: detener espera a que termine para cerrar lo que haya abierto.
  Future<void>? _arranque;

  EstadoComando get estado => _estado;

  /// true si hay un reconocedor de comandos (en las pruebas y fuera de Android puede no haberlo).
  bool get disponible => _escucha != null;

  void _ir(EstadoComando e) {
    _estado = e;
    if (!_desechado) notifyListeners();
  }

  /// Empieza a escuchar si no está escuchando ya, si Nayra calló y si no hubo un error antes en esta pantalla.
  Future<void> escuchar() async {
    final escucha = _escucha;
    if (escucha == null || _desechado) return;
    if (_estado != EstadoComando.inactivo && _estado != EstadoComando.esperando) return;
    // Mientras Nayra habla, Vosk no escucha: se vuelve a intentar cuando calle.
    if (_voz.hablando.value) return _ir(EstadoComando.esperando);
    _ir(EstadoComando.escuchando);
    try {
      if (!await _grabador.tienePermiso()) throw StateError('Sin permiso para usar el micrófono');
      if (_desechado || _estado != EstadoComando.escuchando) return;
      // Desde aquí Nayra no habla hasta que se libere el micrófono; si no se confirma el silencio, no se escucha.
      await _voz.reservarMicrofono();
    } catch (e, pila) {
      return _fallar(e, pila, reservado: false);
    }
    if (_desechado || _estado != EstadoComando.escuchando) return _voz.liberarMicrofono();
    final arranque = _arranque = escucha.iniciar(_alTexto, (e) => _fallar(e, StackTrace.current, reservado: true));
    try {
      await arranque;
    } catch (e, pila) {
      return _fallar(e, pila, reservado: true);
    } finally {
      if (identical(_arranque, arranque)) _arranque = null;
    }
  }

  Future<void> _alTexto(String texto) async {
    if (_estado != EstadoComando.escuchando || !esComandoIniciarSesion(texto)) return;
    _ir(EstadoComando.reconocido);
    await detener();
    if (_desechado) return;
    _ir(EstadoComando.iniciandoLogin);
    alComando();
  }

  /// Detiene Vosk y libera el micrófono. Al terminar, Nayra puede hablar y el inicio de sesión puede abrir su
  /// propio micrófono.
  Future<void> detener() async {
    switch (_estado) {
      case EstadoComando.esperando:
        _ir(EstadoComando.inactivo);
      case EstadoComando.escuchando || EstadoComando.reconocido:
        _ir(EstadoComando.deteniendo);
        await _esperarArranque();
        await _cerrar();
        if (_estado == EstadoComando.deteniendo) _ir(EstadoComando.inactivo);
      default:
    }
  }

  /// Si Vosk todavía está arrancando, se espera a que termine para no dejar abierto lo que abra. Un fallo del
  /// arranque ya lo informa [escuchar], que además cierra y libera el micrófono.
  Future<void> _esperarArranque() async {
    try {
      await _arranque;
    } catch (_) {}
  }

  Future<void> _cerrar() async {
    try {
      await _escucha?.detener();
    } finally {
      _voz.liberarMicrofono();
    }
  }

  Future<void> _fallar(Object e, StackTrace pila, {required bool reservado}) async {
    if (_estado == EstadoComando.error) return;
    _ir(EstadoComando.error);
    FlutterError.reportError(FlutterErrorDetails(
        exception: e, stack: pila, library: 'Nayra', context: ErrorDescription('al escuchar «Iniciar sesión Nayra»')));
    if (!reservado) return;
    try {
      await _cerrar();
    } catch (e2, pila2) {
      FlutterError.reportError(FlutterErrorDetails(
          exception: e2,
          stack: pila2,
          library: 'Nayra',
          context: ErrorDescription('al detener la escucha del comando')));
    }
  }

  @override
  void dispose() {
    _desechado = true;
    // Al destruir la pantalla: se detiene Vosk, se cierra su micrófono y se libera la voz de Nayra.
    if (_estado == EstadoComando.escuchando || _estado == EstadoComando.reconocido) {
      _esperarArranque().then((_) => _cerrar());
    }
    super.dispose();
  }
}
