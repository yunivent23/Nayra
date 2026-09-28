import '../api/cliente_http.dart';

/// Frase de desafío (D-054): palabra + 3 dígitos + palabra, de un solo uso. DTO `DesafioDTO` del backend.
class Desafio {
  const Desafio(this.id, this.texto);
  final String id;
  final String texto;

  static Desafio? desdeJson(Object? json) {
    if (json == null) return null;
    if (json is! Map<String, dynamic>) throw const ErrorRespuesta();
    return Desafio(campo<String>(json, 'desafioId'), campo<String>(json, 'texto'));
  }
}

/// Estados de `ResultadoPaso` (AutenticacionVozDTOs.java). La decisión es siempre de Spring Boot (D-056).
abstract final class EstadoPaso {
  static const continuar = 'CONTINUAR';
  static const autenticado = 'AUTENTICADO';
  static const reintentar = 'REINTENTAR';
  static const bloqueada = 'BLOQUEADA';
  static const servicioNoDisponible = 'SERVICIO_NO_DISPONIBLE';
  static const rechazado = 'RECHAZADO';
}

/// Decisión de Spring Boot en cada paso del inicio de sesión. No contiene puntajes biométricos.
class ResultadoPaso {
  const ResultadoPaso(this.estado, this.motivo, this.intentosRestantes, this.desafio, [this.sesion]);
  final String estado;
  final String? motivo;
  final int? intentosRestantes;
  final Desafio? desafio;

  /// JWT de sesión, solo con AUTENTICADO (D-018). Se guarda solo en memoria.
  final String? sesion;

  factory ResultadoPaso.desdeJson(Map<String, dynamic> j) => ResultadoPaso(
        campo<String>(j, 'estado'),
        campoOpcional<String>(j, 'motivo'),
        campoOpcional<int>(j, 'intentosRestantes'),
        Desafio.desdeJson(j['desafio']),
        campoOpcional<String>(j, 'sesion'),
      );
}
