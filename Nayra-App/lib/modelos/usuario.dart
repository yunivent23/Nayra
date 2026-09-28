import '../api/cliente_http.dart';

/// Datos propios y estado de la cuenta de acceso (HU-09, HU-15). DTO `UsuarioDTOs.DatosPropios`.
/// Nunca incluye el hash del PIN ni datos biométricos.
class DatosPropios {
  const DatosPropios(this.nombres, this.apellidos, this.celular, this.rol, this.estado, this.dispositivoActivo);
  final String nombres;
  final String apellidos;
  final String? celular;

  /// USER o ADMIN (D-041).
  final String rol;

  /// ACTIVO, BLOQUEADO o INACTIVO (modelo v4, `Usuario.Estado`).
  final String estado;
  final bool dispositivoActivo;

  bool get esAdministrador => rol == 'ADMIN';

  String get nombreCompleto => '$nombres $apellidos';

  /// Primer nombre para el saludo.
  String get primerNombre => nombres.trim().split(RegExp(r'\s+')).first;

  factory DatosPropios.desdeJson(Map<String, dynamic> j) => DatosPropios(
        campo<String>(j, 'nombres'),
        campo<String>(j, 'apellidos'),
        campoOpcional<String>(j, 'celular'),
        campo<String>(j, 'rol'),
        campo<String>(j, 'estado'),
        campo<bool>(j, 'dispositivoActivo'),
      );
}

/// Texto del estado de la cuenta de acceso para la persona. No depende solo del color.
String textoEstadoCuenta(String estado) => switch (estado) {
      'ACTIVO' => 'Activa',
      'BLOQUEADO' => 'Bloqueada',
      'INACTIVO' => 'Inactiva',
      _ => 'Desconocido',
    };
