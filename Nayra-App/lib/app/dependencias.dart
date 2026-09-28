import 'package:flutter/widgets.dart';

import '../api/autenticacion_api.dart';
import '../api/billetera_api.dart';
import '../api/cliente_http.dart';
import '../api/destinatario_api.dart';
import '../api/registro_api.dart';
import '../api/representante_api.dart';
import '../api/usuario_api.dart';
import '../servicios/dispositivo.dart';
import '../servicios/grabador.dart';
import '../sesion/gestor_sesion.dart';

/// Servicios de la app, creados una vez y compartidos por las pantallas. Las pantallas nunca hacen peticiones
/// HTTP: usan los flujos, que usan estas APIs, que usan el único [ClienteHttp].
class Dependencias {
  Dependencias({
    required this.http,
    required this.dispositivo,
    required this.grabador,
    BilleteraApi? billetera,
  })  : autenticacion = AutenticacionApi(http),
        registro = RegistroApi(http),
        usuarios = UsuarioApi(http),
        representante = RepresentanteApi(http),
        destinatarios = DestinatarioApi(http),
        billetera = billetera ?? const BilleteraApiPendiente() {
    sesion = GestorSesion(http, usuarios);
  }

  final ClienteHttp http;
  final ClaveDispositivo dispositivo;
  final GrabadorVoz grabador;
  final AutenticacionApi autenticacion;
  final RegistroApi registro;
  final UsuarioApi usuarios;
  final RepresentanteApi representante;
  final DestinatarioApi destinatarios;
  final BilleteraApi billetera;
  late final GestorSesion sesion;
}

/// Da acceso a [Dependencias] desde cualquier pantalla.
class ProveedorNayra extends InheritedWidget {
  const ProveedorNayra({super.key, required this.dependencias, required super.child});
  final Dependencias dependencias;

  static Dependencias de(BuildContext context) =>
      context.getInheritedWidgetOfExactType<ProveedorNayra>()!.dependencias;

  @override
  bool updateShouldNotify(ProveedorNayra oldWidget) => dependencias != oldWidget.dependencias;
}
