import 'package:flutter/foundation.dart';

import '../api/billetera_api.dart';
import '../api/cliente_http.dart';
import '../api/destinatario_api.dart';
import '../modelos/billetera.dart';
import '../modelos/celular.dart';
import 'mensajes.dart';

enum PasoTransferencia { destinatario, buscando, confirmarDestinatario, monto, revisar, enviando, resultado }

/// Transferencia a otro usuario de Nayra (HU-69 a HU-72; D-042; G-1 modificada el 2026-09-28):
/// celular del destinatario → buscar su cuenta Nayra → "¿Desea transferir a María Sala...?" (Sí / No, buscar otro
/// número) → monto → revisar → confirmar o cancelar → enviar → resultado. La persona puede cancelar antes de
/// confirmar (HU-71); cancelar no llama al backend. El ID interno de la cuenta destino nunca se pide ni se muestra.
class FlujoTransferencia extends ChangeNotifier {
  FlujoTransferencia(this._api, this._destinatarios);

  final BilleteraApi _api;
  final DestinatarioApi _destinatarios;

  static const pedirCelular = 'Escriba el número de celular de la persona a quien transfiere. Luego toque Buscar.';

  PasoTransferencia paso = PasoTransferencia.destinatario;
  String mensaje = pedirCelular;
  bool mensajeEsError = false;

  /// Dígitos del celular escritos con el teclado (sin espacios). Se conservan si la búsqueda falla.
  String celularTexto = '';
  Destinatario? destinatario;
  String montoTexto = '';
  Soles? monto;
  ResultadoTransferencia? resultado;

  /// true cuando la persona canceló en la revisión (HU-71): no se envió nada al backend.
  bool cancelada = false;

  void _ir(PasoTransferencia nuevo, String texto, {bool error = false}) {
    paso = nuevo;
    mensaje = texto;
    mensajeEsError = error;
    notifyListeners();
  }

  static const _formatoCelular = 'El número debe tener 9 dígitos y empezar con 9. Revíselo y toque Buscar.';

  static const _errores = {
    'MONTO_FUERA_DE_RANGO': 'El monto debe ser mayor que 0 y menor que 500 soles.',
    'MONTO_ESCALA_INVALIDA': 'El monto puede tener como máximo 2 decimales.',
    'CUENTAS_IGUALES': 'Ese es su propio número. Escriba el celular de la persona a quien transfiere.',
    'CELULAR_INVALIDO': _formatoCelular,
    'DESTINATARIO_NO_ENCONTRADO': 'No hay una cuenta Nayra asociada a ese número. Revíselo y toque Buscar.',
  };

  /// Pregunta de confirmación del destinatario, igual en pantalla y para el lector.
  String get preguntaDestinatario => '¿Desea transferir a ${destinatario!.nombreVisible}?';

  void cambiarCelular(String texto) {
    celularTexto = texto;
    _ir(PasoTransferencia.destinatario, texto.isEmpty ? 'Número vacío.' : Celular.hablado(texto));
  }

  Future<void> buscar() async {
    final celular = Celular.normalizar(celularTexto);
    if (celular == null) {
      _ir(PasoTransferencia.destinatario, _formatoCelular, error: true);
      return;
    }
    _ir(PasoTransferencia.buscando, 'Buscando la cuenta Nayra de ese número.');
    try {
      destinatario = await _destinatarios.buscar(celular);
      _ir(PasoTransferencia.confirmarDestinatario,
          '¿Desea transferir a ${destinatario!.nombreHablado}? Botón 1: Sí. Botón 2: No, buscar otro número.');
    } on FalloNayra catch (e) {
      // El número escrito se conserva para corregirlo o volver a intentar.
      final texto = mensajeFallo(e, porCodigo: _errores);
      _ir(PasoTransferencia.destinatario,
          e is ErrorConexion || e is ErrorTiempoAgotado ? '$texto Toque Buscar para intentarlo otra vez.' : texto,
          error: true);
    }
  }

  /// "Sí" continúa al monto; "No, buscar otro número" reinicia el ingreso del número.
  void confirmarDestinatario(bool correcto) {
    if (!correcto) {
      destinatario = null;
      celularTexto = '';
      montoTexto = '';
      monto = null;
      _ir(PasoTransferencia.destinatario, 'Indique nuevamente el número de celular del destinatario.');
      return;
    }
    _ir(PasoTransferencia.monto,
        '¿Cuánto envía? Puede enviar desde un céntimo hasta 499 soles con 99 céntimos.');
  }

  void cambiarMonto(String texto) {
    montoTexto = texto;
    final s = Soles.leer(texto);
    _ir(PasoTransferencia.monto, s == null ? (texto.isEmpty ? 'Monto vacío.' : texto) : s.hablado);
  }

  void continuarConMonto() {
    final motivo = ReglaMonto.validar(montoTexto);
    if (motivo != null) {
      _ir(PasoTransferencia.monto, _errores[motivo]!, error: true);
      return;
    }
    monto = Soles.leer(montoTexto);
    _ir(PasoTransferencia.revisar,
        'Va a enviar ${monto!.hablado} a ${destinatario!.nombreHablado}. Botón 1: Confirmar. Botón 2: Cancelar.');
  }

  /// HU-70: la persona confirma y recién entonces se envía la solicitud.
  Future<void> confirmar() async {
    _ir(PasoTransferencia.enviando, 'Enviando la transferencia. Espere, por favor.');
    try {
      resultado = await _api.transferir(destinatario!, monto!);
      final r = resultado!;
      _ir(PasoTransferencia.resultado, switch (r.estado) {
        EstadoOperacion.exitoso => 'Transferencia exitosa. ${monto!.hablado} a ${destinatario!.nombreHablado}.'
            '${r.codigoReferencia == null ? '' : ' Código: ${r.codigoReferencia!.split('').join(' ')}.'}',
        EstadoOperacion.fallido => 'La transferencia no se realizó.',
        EstadoOperacion.cancelado => 'La transferencia fue cancelada.',
      }, error: r.estado != EstadoOperacion.exitoso);
    } on FalloNayra catch (e) {
      _ir(PasoTransferencia.revisar, mensajeFallo(e, porCodigo: _errores), error: true);
    }
  }

  /// HU-71: cancelar antes de confirmar.
  void cancelar() {
    cancelada = true;
    resultado = null;
    _ir(PasoTransferencia.resultado, 'Transferencia cancelada. No se envió dinero.');
  }
}
