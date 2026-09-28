import 'package:flutter/foundation.dart';

import '../api/cliente_http.dart';
import '../api/representante_api.dart';
import '../modelos/registro.dart';
import 'mensajes.dart';

enum PasoRepresentante { documento, enviando, validar, codigo, cancelado }

/// Pasos 3–6 del registro asistido (D-052, HU-99) que hace el representante con su sesión ADMIN:
/// documento de identidad (DNI o CE) → consulta al registro de identidad simulado → validación de la identidad.
/// Al validar, entrega el código de registro de un solo uso con el que la persona sigue en su propio celular
/// (mecanismo PROVISIONAL del prototipo, no decisión de D-052).
class FlujoRepresentante extends ChangeNotifier {
  FlujoRepresentante(this._api);

  final RepresentanteApi _api;

  PasoRepresentante paso = PasoRepresentante.documento;
  String mensaje = 'Elija el tipo de documento y escriba su número.';
  bool mensajeEsError = false;
  TipoDocumento tipo = TipoDocumento.dni;
  RegistroIniciado? registro;
  String? _numero;

  String? get numeroDocumento => _numero;

  void _ir(PasoRepresentante nuevo, String texto, {bool error = false}) {
    paso = nuevo;
    mensaje = texto;
    mensajeEsError = error;
    notifyListeners();
  }

  static const _errores = {
    'DOCUMENTO_NO_ENCONTRADO': 'El documento no está en el registro de identidad simulado.',
    'DOCUMENTO_REGISTRADO': 'Este documento ya tiene una cuenta de acceso en Nayra.',
    'CUENTA_FINANCIERA_NO_ENCONTRADA': 'Este documento no tiene una cuenta financiera en el entorno simulado.',
    'SOLICITUD_INVALIDA': 'El número de documento no es válido.',
    'REGISTRO_NO_VALIDO': 'El registro venció. Empiece de nuevo.',
    'ACCESO_DENEGADO': 'Solo un representante autorizado puede hacer este paso.',
  };

  void elegirTipo(TipoDocumento nuevo) {
    tipo = nuevo;
    _ir(PasoRepresentante.documento, 'Documento elegido: ${nuevo.nombre}.');
  }

  Future<void> consultar(String numero) async {
    final limpio = numero.trim();
    if (limpio.isEmpty) {
      _ir(PasoRepresentante.documento, 'Escriba el número de documento.', error: true);
      return;
    }
    _ir(PasoRepresentante.enviando, 'Consultando el registro de identidad.');
    try {
      registro = await _api.iniciarRegistro(tipo, limpio);
      _numero = limpio;
      _ir(PasoRepresentante.validar,
          'Compruebe que la persona es ${registro!.nombres} ${registro!.apellidos}, ${tipo.codigo} $limpio.');
    } on FalloNayra catch (e) {
      _ir(PasoRepresentante.documento, mensajeFallo(e, porCodigo: _errores), error: true);
    }
  }

  /// "Identidad validada" (paso 6).
  Future<void> validar() async {
    final r = registro!;
    _ir(PasoRepresentante.enviando, 'Registrando la validación.');
    try {
      await _api.validarIdentidad(r.codigo);
      _ir(PasoRepresentante.codigo,
          'Identidad validada. Entregue este código a la persona para que continúe en su celular: ${r.codigo}.');
    } on FalloNayra catch (e) {
      _ir(PasoRepresentante.validar, mensajeFallo(e, porCodigo: _errores), error: true);
    }
  }

  /// "No coincide": no se valida la identidad. El backend no tiene un endpoint de rechazo; el registro iniciado
  /// vence solo (vida PROVISIONAL de 900 s).
  void noCoincide() {
    registro = null;
    _numero = null;
    _ir(PasoRepresentante.cancelado, 'No se validó la identidad. No se creará la cuenta.', error: true);
  }
}
