import 'package:flutter/foundation.dart';

import '../api/billetera_api.dart';
import '../api/cliente_http.dart';
import '../api/destinatario_api.dart';
import '../modelos/billetera.dart';
import '../modelos/celular.dart';
import '../servicios/agenda.dart';
import 'mensajes.dart';

enum PasoTransferencia { destinatario, buscando, contactos, confirmarDestinatario, monto, revisar, enviando, resultado }

/// Transferencia a otro usuario de Nayra (HU-69 a HU-72; D-042; G-1 modificada el 2026-09-28):
/// celular del destinatario → buscar su cuenta Nayra → "¿Deseas transferir a María Sala...?" (Sí / No, buscar otro
/// número) → monto → revisar → confirmar o cancelar → enviar → resultado. La persona puede cancelar antes de
/// confirmar (HU-71); cancelar no llama al backend. El ID interno de la cuenta destino nunca se pide ni se muestra.
/// Con [_agenda] (D-042, 2026-10-08), la persona también puede elegir entre sus contactos que tienen cuenta Nayra:
/// solo se muestran los que confirma el backend, y la búsqueda manual sigue disponible.
class FlujoTransferencia extends ChangeNotifier {
  FlujoTransferencia(this._api, this._destinatarios, [this._agenda]) {
    if (_agenda != null) mensaje = _pedirCelularOContacto;
  }

  final BilleteraApi _api;
  final DestinatarioApi _destinatarios;
  final AgendaContactos? _agenda;

  /// true si se puede elegir entre los contactos del teléfono.
  bool get conAgenda => _agenda != null;

  /// Contactos de la agenda confirmados por el backend. Solo en memoria mientras se elige; se vacía al salir.
  List<ContactoDisponible> contactos = const [];

  static const pedirCelular = 'Escribe el número de celular de la persona a quien transfieres. Luego toca Buscar.';

  static const _pedirCelularOContacto =
      'Escribe el número de celular de la persona a quien transfieres y toca Buscar, o toca Elegir de mis contactos.';

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

  static const _formatoCelular = 'El número debe tener 9 dígitos y empezar con 9. Revísalo y toca Buscar.';

  static const _errores = {
    'MONTO_FUERA_DE_RANGO': 'El monto debe ser mayor que 0 y menor que 500 soles.',
    'MONTO_ESCALA_INVALIDA': 'El monto puede tener como máximo 2 decimales.',
    'CUENTAS_IGUALES': 'Ese es tu propio número. Escribe el celular de la persona a quien transfieres.',
    'CELULAR_INVALIDO': _formatoCelular,
    'DESTINATARIO_NO_ENCONTRADO': 'No hay una cuenta Nayra asociada a ese número. Revísalo y toca Buscar.',
  };

  static const sinContactos = 'No tienes contactos disponibles en Nayra para realizar una transferencia.';
  static const _puedesEscribir = 'Puedes continuar escribiendo el número de celular.';
  static const hasta500 = 'En esta versión, la selección de contactos consulta hasta 500 números de tu agenda.';

  /// Pregunta de confirmación del destinatario, igual en pantalla y para el lector.
  String get preguntaDestinatario => '¿Deseas transferir a ${destinatario!.nombreVisible}?';

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
      _preguntarDestinatario();
    } on FalloNayra catch (e) {
      // El número escrito se conserva para corregirlo o volver a intentar.
      final texto = mensajeFallo(e, porCodigo: _errores);
      _ir(PasoTransferencia.destinatario,
          e is ErrorConexion || e is ErrorTiempoAgotado ? '$texto Toca Buscar para intentarlo otra vez.' : texto,
          error: true);
    }
  }

  void _preguntarDestinatario() => _ir(PasoTransferencia.confirmarDestinatario,
      '¿Deseas transferir a ${destinatario!.nombreHablado}? Botón 1: Sí. Botón 2: No, buscar otro número.');

  /// «Elegir de mis contactos»: recién aquí se pide el permiso de contactos. Se envían al backend solo los celulares
  /// válidos de la agenda y se muestran solo los contactos cuyo número confirma. Si algo falla, se vuelve al
  /// teclado sin mostrar contactos no confirmados.
  Future<void> buscarEnContactos() async {
    final agenda = _agenda;
    if (agenda == null) return;
    contactos = const [];
    _ir(PasoTransferencia.buscando, 'Buscando tus contactos que tienen cuenta Nayra.');
    final List<ContactoAgenda> leidos;
    try {
      leidos = await agenda.leer();
    } on AgendaSinPermiso {
      _ir(PasoTransferencia.destinatario, 'No tengo permiso para ver tus contactos. $_puedesEscribir', error: true);
      return;
    } catch (_) {
      _ir(PasoTransferencia.destinatario, 'No pude leer tus contactos. $_puedesEscribir', error: true);
      return;
    }
    final (:celulares, :recortada) = CruceAgenda.celulares(leidos);
    final aviso = recortada ? ' $hasta500' : '';
    if (celulares.isEmpty) {
      _ir(PasoTransferencia.destinatario, '$sinContactos $_puedesEscribir', error: true);
      return;
    }
    try {
      contactos = CruceAgenda.cruzar(leidos, await _destinatarios.buscarVarios(celulares));
    } on FalloNayra catch (e) {
      _ir(PasoTransferencia.destinatario, '${mensajeFallo(e)} $_puedesEscribir', error: true);
      return;
    }
    if (contactos.isEmpty) {
      _ir(PasoTransferencia.destinatario, '$sinContactos$aviso $_puedesEscribir', error: true);
      return;
    }
    final n = contactos.length;
    _ir(PasoTransferencia.contactos,
        '${n == 1 ? 'Tienes 1 contacto' : 'Tienes $n contactos'} con cuenta Nayra.$aviso Toca uno para elegirlo, '
        'o toca Escribir el número.');
  }

  /// El contacto elegido pasa a la misma confirmación que la búsqueda manual.
  void elegirContacto(ContactoDisponible contacto) {
    contactos = const [];
    destinatario = contacto.destinatario;
    celularTexto = contacto.destinatario.celular;
    _preguntarDestinatario();
  }

  /// Sale de la lista de contactos y vuelve al teclado.
  void escribirNumero() {
    contactos = const [];
    _ir(PasoTransferencia.destinatario, _pedirCelularOContacto);
  }

  /// "Sí" continúa al monto; "No, buscar otro número" reinicia el ingreso del número.
  void confirmarDestinatario(bool correcto) {
    if (!correcto) {
      destinatario = null;
      celularTexto = '';
      montoTexto = '';
      monto = null;
      _ir(PasoTransferencia.destinatario, 'Indica nuevamente el número de celular del destinatario.');
      return;
    }
    _ir(PasoTransferencia.monto,
        '¿Cuánto envías? Puedes enviar desde un céntimo hasta 499 soles con 99 céntimos.');
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
        'Vas a enviar ${monto!.hablado} a ${destinatario!.nombreHablado}. Botón 1: Confirmar. Botón 2: Cancelar.');
  }

  /// HU-70: la persona confirma y recién entonces se envía la solicitud.
  Future<void> confirmar() async {
    _ir(PasoTransferencia.enviando, 'Enviando la transferencia. Espera, por favor.');
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
