import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import '../api/cliente_http.dart';
import '../api/registro_api.dart';
import '../modelos/autenticacion.dart';
import '../modelos/celular.dart';
import '../modelos/registro.dart';
import '../servicios/dispositivo.dart';
import 'mensajes.dart';

enum PasoRegistro { codigo, confirmarDatos, celular, pin, confirmarPin, vinculando, enviando, muestra, terminado, error }

/// Pasos del registro inicial asistido que ocurren en el celular de la persona (D-052, pasos 7–13), guiada en
/// persona por el representante.
///
/// Los pasos 3–6 (documento, consulta al registro simulado y validación de identidad) los hace el representante con
/// su sesión ADMIN (RepresentanteApi) y entrega un código de registro de un solo uso: mecanismo PROVISIONAL del
/// prototipo, no decisión de D-052. Cancelar si la persona no confirma sus datos también es PROVISIONAL.
/// El tutorial (paso 12, HU-122) queda pendiente: su contenido no está definido.
class FlujoRegistro extends ChangeNotifier {
  FlujoRegistro(this._api, this._dispositivo);

  final RegistroApi _api;
  final ClaveDispositivo _dispositivo;

  PasoRegistro paso = PasoRegistro.codigo;
  String mensaje = 'Escriba el código de registro que le entregó el representante.';
  bool mensajeEsError = false;
  DatosRegistro? datos;
  Desafio? desafio;
  int muestrasValidas = 0;
  int muestrasRequeridas = 3;
  String? _codigo;
  String? _celular;

  /// PIN en memoria solo entre "crear" y "confirmar"; se borra al enviarlo o si no coincide. Nunca se registra.
  String? _pin;

  void _ir(PasoRegistro nuevo, String texto, {bool error = false}) {
    paso = nuevo;
    mensaje = texto;
    mensajeEsError = error;
    notifyListeners();
  }

  static const _errores = {
    'REGISTRO_NO_VALIDO': 'El código no es válido o venció. Pida uno nuevo al representante.',
    'IDENTIDAD_NO_VALIDADA': 'El representante todavía no validó su identidad.',
    'PASO_NO_VALIDO': 'Este registro ya avanzó a otro paso. Pida un código nuevo al representante.',
    'CELULAR_INVALIDO': 'El número de celular no es válido.',
    'CELULAR_REGISTRADO': 'Ese número de celular ya está registrado en Nayra.',
    'PIN_INVALIDO': 'El PIN debe tener 6 dígitos.',
    'CLAVE_DISPOSITIVO_INVALIDA': 'No se pudo vincular este celular.',
    'DOCUMENTO_REGISTRADO': 'Este documento ya tiene una cuenta en Nayra.',
    'VOZ_NO_ENROLADA': 'Todavía falta registrar su voz.',
    'REGISTRO_NO_LISTO_PARA_ENROLAR': 'Todavía no se puede registrar su voz.',
  };

  /// Paso anterior dentro del registro (barra «Atrás»). Devuelve false si no hay paso anterior en este celular.
  /// Volver de la creación del PIN borra el PIN en memoria.
  bool volver() {
    switch (paso) {
      case PasoRegistro.confirmarDatos:
        _codigo = null;
        datos = null;
        _ir(PasoRegistro.codigo, 'Escriba el código de registro que le entregó el representante.');
      case PasoRegistro.celular:
        _ir(PasoRegistro.confirmarDatos, 'Sus datos son: ${datos!.nombres} ${datos!.apellidos}. ¿Son correctos?');
      case PasoRegistro.pin:
        _ir(PasoRegistro.celular, 'Escriba su número de celular. Luego toque Continuar.');
      case PasoRegistro.confirmarPin:
        _pin = null;
        _ir(PasoRegistro.pin, 'Cree un PIN de 6 dígitos.');
      default:
        return false;
    }
    return true;
  }

  Future<void> ingresarCodigo(String codigo) async {
    final limpio = codigo.trim();
    if (limpio.isEmpty) {
      _ir(PasoRegistro.codigo, 'Escriba el código de registro.', error: true);
      return;
    }
    _ir(PasoRegistro.enviando, 'Consultando sus datos.');
    try {
      datos = await _api.datos(limpio);
      _codigo = limpio;
      _ir(PasoRegistro.confirmarDatos, 'Sus datos son: ${datos!.nombres} ${datos!.apellidos}. ¿Son correctos?');
    } on FalloNayra catch (e) {
      _ir(PasoRegistro.codigo, mensajeFallo(e, porCodigo: _errores), error: true);
    }
  }

  Future<void> confirmarDatos(bool correctos) async {
    if (correctos) {
      _ir(PasoRegistro.celular, 'Escriba su número de celular. Luego toque Continuar.');
      return;
    }
    try {
      await _api.completarDatos(_codigo!, confirma: false);
    } on FalloNayra {
      // El registro vence solo aunque no se pueda avisar al backend.
    }
    _codigo = null;
    datos = null;
    _ir(PasoRegistro.error, 'Registro cancelado. Informe al representante que los datos no son correctos.', error: true);
  }

  void ingresarCelular(String celular) {
    // Celular de Perú: 9 dígitos que empiezan por 9; "+51" se quita (G-1, V012; igual que el backend).
    final canonico = Celular.normalizar(celular);
    if (canonico == null) {
      _ir(PasoRegistro.celular, 'El número debe tener 9 dígitos y empezar con 9. Escríbalo otra vez.', error: true);
      return;
    }
    _celular = canonico;
    _ir(PasoRegistro.pin, 'Cree un PIN de 6 dígitos.');
  }

  void ingresarPin(String pin) {
    _pin = pin;
    _ir(PasoRegistro.confirmarPin, 'Escriba el PIN otra vez para confirmarlo.');
  }

  Future<void> confirmarPin(String pin) async {
    if (pin != _pin) {
      _pin = null;
      _ir(PasoRegistro.pin, 'Los PIN no coinciden. Cree un PIN de 6 dígitos.', error: true);
      return;
    }
    _ir(PasoRegistro.vinculando, 'Vinculando su celular. Espere, por favor.');
    try {
      // D-048: el par de claves se genera en el almacén del teléfono; solo viaja la clave pública.
      await _api.completarDatos(_codigo!,
          confirma: true, celular: _celular, pin: pin, clavePublicaBase64: await _dispositivo.clavePublica());
      await _siguienteDesafio('Celular vinculado. Ahora registraremos su voz.');
    } on FalloNayra catch (e) {
      if (e is ErrorApi && e.codigo == 'CELULAR_REGISTRADO') {
        // El número ya pertenece a otra cuenta Nayra (celular único, V012): se pide otro sin perder el registro.
        _celular = null;
        _ir(PasoRegistro.celular, '${_errores['CELULAR_REGISTRADO']} Escriba otro número.', error: true);
      } else {
        _ir(PasoRegistro.error, mensajeFallo(e, porCodigo: _errores), error: true);
      }
    } on PlatformException {
      _ir(PasoRegistro.error, 'No se pudo crear la clave de seguridad de este celular.', error: true);
    } finally {
      _pin = null;
    }
  }

  Future<void> _siguienteDesafio(String prefijo, {bool error = false}) async {
    desafio = await _api.desafioEnrolamiento(_codigo!);
    _ir(PasoRegistro.muestra,
        '$prefijo Muestra ${muestrasValidas + 1} de $muestrasRequeridas. ${instruccionDesafio(desafio!.texto)}',
        error: error);
  }

  Future<void> enviarMuestra(Uint8List wav) async {
    final anterior = desafio;
    _ir(PasoRegistro.enviando, 'Procesando la muestra. Espere, por favor.');
    try {
      final r = await _api.enviarMuestra(_codigo!, anterior!.id, wav);
      muestrasRequeridas = r.muestrasRequeridas;
      if (r.aceptada) muestrasValidas = r.muestrasValidas ?? muestrasValidas + 1;
      if (muestrasValidas >= muestrasRequeridas) {
        if (!await _api.finalizarEnrolamiento(_codigo!)) {
          _ir(PasoRegistro.error, 'No se pudo registrar su voz. Pida ayuda al representante.', error: true);
          return;
        }
        final fin = await _api.finalizar(_codigo!);
        await _dispositivo.guardarIdentificadores(fin.usuarioId, fin.dispositivoId);
        _codigo = null;
        _ir(PasoRegistro.terminado, 'Registro completado. Ya puede iniciar sesión en Nayra.');
        return;
      }
      await _siguienteDesafio(r.aceptada ? 'Muestra aceptada.' : mensajeMotivo(r.motivo), error: !r.aceptada);
    } on FalloNayra catch (e) {
      _ir(PasoRegistro.error, mensajeFallo(e, porCodigo: _errores), error: true);
    } on PlatformException {
      _ir(PasoRegistro.error, 'No se pudo guardar el registro en este celular.', error: true);
    }
  }
}
