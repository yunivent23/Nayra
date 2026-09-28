import 'package:flutter/material.dart';

import '../pantallas/billetera/pantalla_menu.dart';
import '../pantallas/inicio_sesion/pantalla_inicio_sesion.dart';
import '../pantallas/pantalla_bienvenida.dart';
import '../pantallas/pantalla_sesion_cerrada.dart';
import '../sesion/gestor_sesion.dart';
import 'dependencias.dart';

/// Navegación real de la app:
/// bienvenida → registro (en el celular de la persona) → inicio de sesión → menú de la billetera → operaciones.
/// Al autenticarse o al cerrarse la sesión se vacía la pila: no se puede volver con «Atrás» a pantallas
/// protegidas ni a los pasos del PIN y la voz.
abstract final class Navegacion {
  static final clave = GlobalKey<NavigatorState>();

  static Route<void> ruta(WidgetBuilder constructor) => MaterialPageRoute<void>(builder: constructor);

  static Future<void> ir(BuildContext context, WidgetBuilder constructor) =>
      Navigator.of(context).push(ruta(constructor));

  static void reemplazarTodo(WidgetBuilder constructor) =>
      clave.currentState?.pushAndRemoveUntil(ruta(constructor), (_) => false);

  static void alInicio() => reemplazarTodo((_) => const PantallaBienvenida());

  static void aIniciarSesion() => reemplazarTodo((_) => const PantallaInicioSesion());

  static void aBilletera() => reemplazarTodo((_) => const Protegida(child: PantallaMenu()));

  static void aSesionCerrada(MotivoCierre motivo) => reemplazarTodo((_) => PantallaSesionCerrada(motivo: motivo));
}

/// Protección de las pantallas autenticadas: sin sesión no muestran nada (la app ya está navegando a la
/// pantalla de sesión cerrada).
class Protegida extends StatelessWidget {
  const Protegida({super.key, required this.child});
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final sesion = ProveedorNayra.de(context).sesion;
    return ListenableBuilder(
      listenable: sesion,
      builder: (context, _) => sesion.activa ? child : const Scaffold(body: SizedBox.shrink()),
    );
  }
}
