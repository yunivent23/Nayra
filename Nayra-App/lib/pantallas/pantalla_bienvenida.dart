import 'package:flutter/material.dart';

import '../app/navegacion.dart';
import '../componentes/botones.dart';
import '../componentes/logo.dart';
import '../componentes/pantalla.dart';
import 'inicio_sesion/pantalla_inicio_sesion.dart';
import 'registro/pantalla_registro.dart';

/// A1 — Bienvenida: iniciar sesión o registrarse con un representante (D-052, D-053).
class PantallaBienvenida extends StatelessWidget {
  const PantallaBienvenida({super.key});

  @override
  Widget build(BuildContext context) => PantallaNayra(
        textoVoz: 'Te doy la bienvenida a Nayra. Botón 1 de 2: Iniciar sesión. Botón 2 de 2: Registrarme con un representante.',
        mostrarAtras: false,
        cabecera: const LogoNayra(),
        hijos: [
          BotonNayra(
            texto: 'Iniciar sesión',
            icono: Icons.mic,
            alto: 172,
            alPulsar: () => Navegacion.ir(context, (_) => const PantallaInicioSesion()),
          ),
          BotonNayra(
            texto: 'Registrarme',
            subtitulo: 'con un representante',
            icono: Icons.person_add_alt_1,
            tipo: TipoBoton.secundario,
            alto: 172,
            alPulsar: () => Navegacion.ir(context, (_) => const PantallaRegistro()),
          ),
        ],
      );
}
