import 'package:flutter/foundation.dart';

import '../servicios/api_prototipo.dart';
import '../servicios/dispositivo.dart';
import 'mensajes.dart';

enum PasoRegistro { codigo, confirmarDatos, celular, pin, confirmarPin, enviando, muestra, terminado, error }

/// Pasos del registro inicial asistido que ocurren en el celular de la persona (D-052, pasos 7–13).
/// Los pasos 3–6 (DNI, consulta al registro simulado y validación de identidad) los hace el
/// representante, que entrega un código de registro de un solo uso (mecanismo PROVISIONAL del prototipo,
/// no decisión de D-052). Cancelar si la persona no confirma sus datos también es PROVISIONAL.
/// El tutorial (paso 12) queda pendiente: su contenido no está definido.
class FlujoRegistro extends ChangeNotifier {
  FlujoRegistro(this._api, this._dispositivo);

  final ApiPrototipo _api;
  final ClaveDispositivo _dispositivo;

  PasoRegistro paso = PasoRegistro.codigo;
  String mensaje = 'Ingrese el código de registro que le entregó el representante.';
  DatosRegistro? datos;
  Desafio? desafio;
  int muestrasValidas = 0;
  int muestrasRequeridas = 3;
  String? _codigo;
  String? _celular;
  String? _pin;

  void _ir(PasoRegistro nuevo, String texto) {
    paso = nuevo;
    mensaje = texto;
    notifyListeners();
  }

  Future<void> ingresarCodigo(String codigo) async {
    final limpio = codigo.trim();
    if (limpio.isEmpty) return;
    _ir(PasoRegistro.enviando, 'Consultando sus datos.');
    try {
      datos = await _api.datosRegistro(limpio);
      _codigo = limpio;
      _ir(PasoRegistro.confirmarDatos,
          'Sus datos son: ${datos!.nombres} ${datos!.apellidos}. ¿Son correctos?');
    } on ErrorApi catch (e) {
      _ir(PasoRegistro.codigo, e.codigo == 'IDENTIDAD_NO_VALIDADA'
          ? 'El representante todavía no validó su identidad.'
          : 'El código no es válido o venció. Pida uno nuevo al representante.');
    } catch (_) {
      _ir(PasoRegistro.codigo, 'No se pudo conectar con Nayra. Inténtelo otra vez.');
    }
  }

  Future<void> confirmarDatos(bool correctos) async {
    if (correctos) {
      _ir(PasoRegistro.celular, 'Ingrese su número de celular.');
      return;
    }
    try {
      await _api.completarDatos(_codigo!, confirma: false);
    } catch (_) {
      // El registro vence solo aunque no se pueda avisar al backend.
    }
    _codigo = null;
    datos = null;
    _ir(PasoRegistro.error, 'Registro cancelado. Informe al representante que los datos no son correctos.');
  }

  void ingresarCelular(String celular) {
    final limpio = celular.replaceAll(RegExp(r'[\s-]'), '');
    // PROVISIONAL: D-043 no fija un formato; se aceptan de 9 a 15 dígitos con "+" opcional.
    if (!RegExp(r'^\+?\d{9,15}$').hasMatch(limpio)) {
      _ir(PasoRegistro.celular, 'El número de celular no es válido. Ingréselo otra vez.');
      return;
    }
    _celular = limpio;
    _ir(PasoRegistro.pin, 'Cree un PIN de 6 dígitos.');
  }

  void ingresarPin(String pin) {
    _pin = pin;
    _ir(PasoRegistro.confirmarPin, 'Ingrese el PIN otra vez para confirmarlo.');
  }

  Future<void> confirmarPin(String pin) async {
    if (pin != _pin) {
      _pin = null;
      _ir(PasoRegistro.pin, 'Los PIN no coinciden. Cree un PIN de 6 dígitos.');
      return;
    }
    _ir(PasoRegistro.enviando, 'Vinculando este dispositivo.');
    try {
      await _api.completarDatos(_codigo!,
          confirma: true, celular: _celular, pin: pin, clavePublicaBase64: await _dispositivo.clavePublica());
      _pin = null;
      await _siguienteDesafio('Dispositivo listo. Ahora registraremos su voz.');
    } on ErrorApi catch (e) {
      _pin = null;
      _ir(PasoRegistro.error, e.codigo == 'REGISTRO_NO_VALIDO'
          ? 'El registro venció. Pida un código nuevo al representante.'
          : 'No se pudo completar el registro. Inténtelo más tarde.');
    } catch (_) {
      _pin = null;
      _ir(PasoRegistro.error, 'No se pudo completar el registro. Inténtelo más tarde.');
    }
  }

  Future<void> _siguienteDesafio(String prefijo) async {
    desafio = await _api.desafioEnrolamiento(_codigo!);
    _ir(PasoRegistro.muestra,
        '$prefijo Muestra ${muestrasValidas + 1} de $muestrasRequeridas. ${instruccionDesafio(desafio!.texto)}');
  }

  Future<void> enviarMuestra(Uint8List wav) async {
    _ir(PasoRegistro.enviando, 'Procesando la muestra.');
    try {
      final r = await _api.enviarMuestra(_codigo!, desafio!.id, wav);
      muestrasRequeridas = r.muestrasRequeridas;
      if (r.aceptada) muestrasValidas = r.muestrasValidas ?? muestrasValidas + 1;
      if (muestrasValidas >= muestrasRequeridas) {
        if (!await _api.finalizarEnrolamiento(_codigo!)) {
          _ir(PasoRegistro.error, 'No se pudo registrar su voz.');
          return;
        }
        final fin = await _api.finalizarRegistro(_codigo!);
        await _dispositivo.guardarIdentificadores(fin.usuarioId, fin.dispositivoId);
        _codigo = null;
        _ir(PasoRegistro.terminado, 'Registro completado. Ya puede iniciar sesión en Nayra.');
        return;
      }
      await _siguienteDesafio(r.aceptada ? 'Muestra aceptada.' : mensajeMotivo(r.motivo));
    } catch (_) {
      _ir(PasoRegistro.error, 'No se pudo procesar la muestra. Inténtelo más tarde.');
    }
  }
}
