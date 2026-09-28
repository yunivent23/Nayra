import '../api/cliente_http.dart';

/// Tipos de documento de identidad del modelo v4 (§2.1; P-2 provisional).
enum TipoDocumento {
  dni('DNI', 'DNI'),
  ce('CE', 'Carné de extranjería');

  const TipoDocumento(this.codigo, this.nombre);
  final String codigo;
  final String nombre;
}

/// Datos del registro de identidad simulado que la persona confirma (D-052, paso 7). DTO `DatosParaConfirmar`.
class DatosRegistro {
  const DatosRegistro(this.nombres, this.apellidos, this.paso);
  final String nombres;
  final String apellidos;
  final String? paso;

  factory DatosRegistro.desdeJson(Map<String, dynamic> j) =>
      DatosRegistro(campo<String>(j, 'nombres'), campo<String>(j, 'apellidos'), campoOpcional<String>(j, 'paso'));
}

/// Pasos 3–5 hechos por el representante: datos del registro simulado y código de un solo uso para continuar
/// en el celular de la persona (mecanismo PROVISIONAL del prototipo, no decisión de D-052). DTO `RegistroIniciado`.
class RegistroIniciado {
  const RegistroIniciado(this.codigo, this.nombres, this.apellidos);
  final String codigo;
  final String nombres;
  final String apellidos;

  factory RegistroIniciado.desdeJson(Map<String, dynamic> j) => RegistroIniciado(
      campo<String>(j, 'codigoRegistro'), campo<String>(j, 'nombres'), campo<String>(j, 'apellidos'));
}

/// Resultado de una muestra de enrolamiento. DTO `ResultadoMuestra`.
class ResultadoMuestra {
  const ResultadoMuestra(this.aceptada, this.motivo, this.muestrasValidas, this.muestrasRequeridas);
  final bool aceptada;
  final String? motivo;
  final int? muestrasValidas;
  final int muestrasRequeridas;

  factory ResultadoMuestra.desdeJson(Map<String, dynamic> j) => ResultadoMuestra(campo<bool>(j, 'aceptada'),
      campoOpcional<String>(j, 'motivo'), campoOpcional<int>(j, 'muestrasValidas'), campo<int>(j, 'muestrasRequeridas'));
}

/// Cuenta de acceso creada al finalizar el registro (HU-04). DTO `RegistroFinalizado`.
class RegistroFinalizado {
  const RegistroFinalizado(this.usuarioId, this.dispositivoId);
  final String usuarioId;
  final String dispositivoId;

  factory RegistroFinalizado.desdeJson(Map<String, dynamic> j) =>
      RegistroFinalizado(campo<String>(j, 'usuarioId'), campo<String>(j, 'dispositivoId'));
}
