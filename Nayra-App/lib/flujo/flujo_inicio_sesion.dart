import 'package:flutter/foundation.dart';

import '../servicios/api_prototipo.dart';
import '../servicios/dispositivo.dart';
import 'mensajes.dart';

enum PasoInicioSesion { inicial, verificando, pin, voz, autenticado, bloqueada, terminado }

/// Orquesta en el móvil: dispositivo → PIN → desafío → voz. La decisión es siempre del
/// backend (D-056); la app solo presenta el resultado. Al autenticar recibe el token de sesión
/// (mecanismo PROVISIONAL, D-018), que se guarda solo en memoria.
class FlujoInicioSesion extends ChangeNotifier {
  FlujoInicioSesion(this._api, this._dispositivo);

  static const proposito = 'INICIO_SESION';

  final ApiPrototipo _api;
  final ClaveDispositivo _dispositivo;

  PasoInicioSesion paso = PasoInicioSesion.inicial;
  String mensaje = '';
  Desafio? desafio;
  String? sesion;
  String? _transaccion;

  void _ir(PasoInicioSesion nuevo, String texto) {
    paso = nuevo;
    mensaje = texto;
    notifyListeners();
  }

  Future<void> iniciar() async {
    _ir(PasoInicioSesion.verificando, 'Verificando el dispositivo.');
    try {
      final ids = await _dispositivo.leerIdentificadores();
      final dispositivoId = ids['dispositivoId'];
      if (dispositivoId == null) {
        _ir(PasoInicioSesion.terminado, 'Este dispositivo no está vinculado. Complete primero el registro.');
        return;
      }
      final nonce = await _api.nonce(dispositivoId);
      final firma = await _dispositivo.firmar(mensajeAFirmar(nonce, dispositivoId, proposito));
      _transaccion = await _api.abrirTransaccion(dispositivoId, nonce, firma);
      _ir(PasoInicioSesion.pin, 'Dispositivo verificado. Ingrese su PIN de 6 dígitos o díctelo.');
    } on ErrorApi catch (e) {
      _ir(e.codigo == 'CUENTA_BLOQUEADA' ? PasoInicioSesion.bloqueada : PasoInicioSesion.terminado,
          switch (e.codigo) {
            'CUENTA_BLOQUEADA' => 'Su cuenta está bloqueada.',
            'DISPOSITIVO_NO_VERIFICADO' => 'No se pudo verificar este dispositivo.',
            _ => 'No se pudo iniciar sesión.',
          });
    } catch (_) {
      _ir(PasoInicioSesion.terminado, 'No se pudo conectar con Nayra. Inténtelo más tarde.');
    }
  }

  Future<void> enviarPin(String pin) => _paso(() => _api.verificarPin(_transaccion!, pin));

  Future<void> enviarPinDictado(Uint8List wav) => _paso(() => _api.verificarPinDictado(_transaccion!, wav));

  Future<void> enviarVoz(Uint8List wav) => _paso(() => _api.verificarVoz(_transaccion!, wav));

  Future<void> _paso(Future<ResultadoPaso> Function() llamada) async {
    final anterior = paso;
    _ir(PasoInicioSesion.verificando, 'Verificando.');
    try {
      aplicar(await llamada(), anterior);
    } on ErrorApi catch (e) {
      _ir(e.codigo == 'CUENTA_BLOQUEADA' ? PasoInicioSesion.bloqueada : PasoInicioSesion.terminado,
          e.codigo == 'CUENTA_BLOQUEADA' ? 'Su cuenta está bloqueada.' : 'La operación venció. Vuelva a iniciar sesión.');
    } catch (_) {
      _ir(anterior, 'No se pudo conectar con Nayra. Inténtelo otra vez.');
    }
  }

  @visibleForTesting
  void aplicar(ResultadoPaso r, PasoInicioSesion anterior) {
    if (r.desafio != null) desafio = r.desafio;
    switch (r.estado) {
      case 'CONTINUAR':
        _ir(PasoInicioSesion.voz, 'PIN correcto. ${instruccionDesafio(desafio!.texto)}');
      case 'AUTENTICADO':
        desafio = null;
        sesion = r.sesion;
        _ir(PasoInicioSesion.autenticado, 'Identidad verificada. Bienvenido a Nayra.');
      case 'BLOQUEADA':
        desafio = null;
        _ir(PasoInicioSesion.bloqueada, 'Se agotaron los intentos. Su cuenta está bloqueada.');
      case 'RECHAZADO':
        desafio = null;
        _ir(PasoInicioSesion.terminado, mensajeMotivo(r.motivo));
      default: // REINTENTAR o SERVICIO_NO_DISPONIBLE: se repite el mismo paso.
        final texto = mensajeMotivo(r.motivo, intentosRestantes: r.intentosRestantes);
        _ir(anterior, anterior == PasoInicioSesion.voz && desafio != null
            ? '$texto ${instruccionDesafio(desafio!.texto)}'
            : texto);
    }
  }
}
