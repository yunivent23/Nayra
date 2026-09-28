/// Modelos de la billetera (entorno bancario simulado, D-021, D-022; moneda PEN, modelo v4).
///
/// IMPORTANTE: Nayra-Back todavía NO expone endpoints de saldo, movimientos, transferencias ni avisos
/// (08_ESTADO_PROYECTO §3.1: "sin servicio ni endpoint"). Estos modelos recogen solo los campos que el modelo de
/// datos v4 ya define (`nayra.cuentas`, `nayra.operaciones`, `nayra.notificaciones`); no tienen `desdeJson`
/// porque no hay contrato JSON que respetar (D-014). Ver docs/FRONTEND_ENDPOINTS_PENDIENTES.md.
library;

/// Importe en soles guardado en céntimos: nunca se usa `double` para dinero (E-03: sin redondeo).
class Soles implements Comparable<Soles> {
  const Soles(this.centimos);
  final int centimos;

  static final _formato = RegExp(r'^\d{1,13}([.,]\d{1,2})?$');

  /// Convierte "80", "80.5", "80,50" en céntimos sin redondear. Null si el texto no es un importe con 2 decimales
  /// como máximo.
  static Soles? leer(String texto) {
    final t = texto.trim();
    if (!_formato.hasMatch(t)) return null;
    final partes = t.replaceAll(',', '.').split('.');
    final decimales = partes.length == 2 ? partes[1].padRight(2, '0') : '00';
    return Soles(int.parse(partes[0]) * 100 + int.parse(decimales));
  }

  /// "S/ 1,250.00" como en los mockups (separador de miles con coma, decimales con punto).
  String get texto {
    final enteros = (centimos ~/ 100).toString();
    final miles = enteros.replaceAllMapped(RegExp(r'\B(?=(\d{3})+(?!\d))'), (_) => ',');
    return 'S/ $miles.${(centimos % 100).toString().padLeft(2, '0')}';
  }

  /// Valor para el backend con 2 decimales exactos ("80.00"), compatible con `BigDecimal`.
  String get decimal => '${centimos ~/ 100}.${(centimos % 100).toString().padLeft(2, '0')}';

  /// Lectura para el lector de pantalla: "80 soles", "80 soles con 50 céntimos", "1 sol".
  String get hablado {
    final s = centimos ~/ 100;
    final c = centimos % 100;
    final soles = '$s ${s == 1 ? 'sol' : 'soles'}';
    if (c == 0) return soles;
    return '$soles con $c ${c == 1 ? 'céntimo' : 'céntimos'}';
  }

  @override
  int compareTo(Soles other) => centimos.compareTo(other.centimos);

  @override
  bool operator ==(Object other) => other is Soles && other.centimos == centimos;

  @override
  int get hashCode => centimos.hashCode;
}

/// Regla del monto de una transferencia (modelo v4 §2.8, `Operaciones.validarMonto`): mayor que 0 y menor que 500
/// soles, con 2 decimales como máximo. El backend vuelve a validarlo; la app solo evita enviar un monto imposible.
abstract final class ReglaMonto {
  static const minimo = Soles(1);
  static const maximo = Soles(49999);

  /// Motivo del rechazo, o null si el monto es válido. Mismos códigos que el backend.
  static String? validar(String texto) {
    final s = Soles.leer(texto);
    if (s == null) return texto.contains(RegExp(r'[.,]\d{3,}')) ? 'MONTO_ESCALA_INVALIDA' : 'MONTO_FUERA_DE_RANGO';
    if (s.compareTo(minimo) < 0 || s.compareTo(maximo) > 0) return 'MONTO_FUERA_DE_RANGO';
    return null;
  }
}

/// Estados de una operación en el modelo v4 (HU-87): no existe estado pendiente.
enum EstadoOperacion {
  exitoso('EXITOSO', 'Exitosa'),
  fallido('FALLIDO', 'Fallida'),
  cancelado('CANCELADO', 'Cancelada');

  const EstadoOperacion(this.codigo, this.texto);
  final String codigo;
  final String texto;
}

/// Saldo de la única cuenta financiera del usuario (D-025, HU-66).
class Saldo {
  const Saldo(this.disponible);
  final Soles disponible;
}

/// Movimiento (HU-67) según `nayra.operaciones`. PENDIENTE DE CONTRATO: el dato que identifica a la otra parte
/// ante la persona (nombre visible) no es una columna de la operación; debe decidirlo el endpoint.
class Movimiento {
  const Movimiento({
    required this.id,
    required this.enviado,
    required this.monto,
    required this.estado,
    required this.codigoReferencia,
    required this.fecha,
    this.contraparte,
  });
  final String id;

  /// true si la cuenta del usuario es la de origen.
  final bool enviado;
  final Soles monto;
  final EstadoOperacion estado;

  /// Código de referencia de 6 dígitos (v4 §2.8).
  final String codigoReferencia;
  final DateTime fecha;
  final String? contraparte;
}

/// Destinatario localizado por su celular (G-1, 2026-09-28). El ID interno de su cuenta queda en el backend y la
/// app nunca lo pide ni lo muestra. [celular] es el número canónico que escribió el remitente (solo en memoria);
/// [nombreVisible] es el primer nombre y el primer apellido parcial que genera el backend ("María Sala...", "María De la...").
class Destinatario {
  const Destinatario(this.celular, this.nombreVisible);
  final String celular;
  final String nombreVisible;

  /// Nombre para el lector de pantalla, sin los puntos suspensivos: "María De la".
  String get nombreHablado => nombreVisible.replaceAll('...', '').replaceAll('…', '').trim();
}

/// Resultado de una transferencia (HU-72): estado del modelo v4 y código de referencia.
class ResultadoTransferencia {
  const ResultadoTransferencia(this.estado, this.codigoReferencia);
  final EstadoOperacion estado;
  final String? codigoReferencia;
}

/// Aviso de operación (HU-77) según `nayra.notificaciones`: tipo OPERACION, texto generado por el backend.
class Aviso {
  const Aviso(this.id, this.titulo, this.contenido, this.leida, this.fecha);
  final String id;
  final String titulo;
  final String contenido;
  final bool leida;
  final DateTime fecha;
}
