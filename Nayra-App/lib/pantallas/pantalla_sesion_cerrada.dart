import 'package:flutter/material.dart';

import '../app/navegacion.dart';
import '../componentes/anuncio.dart';
import '../componentes/botones.dart';
import '../componentes/contenido.dart';
import '../componentes/pantalla.dart';
import '../sesion/gestor_sesion.dart';

/// D3 — La sesión terminó sin que la persona la cerrara: 5 minutos sin actividad (D-018, sin aviso previo ni
/// opción de continuar) o rechazo del servidor (401: sesión vencida, cerrada o revocada).
class PantallaSesionCerrada extends StatefulWidget {
  const PantallaSesionCerrada({super.key, required this.motivo});
  final MotivoCierre motivo;

  @override
  State<PantallaSesionCerrada> createState() => _PantallaSesionCerradaState();
}

class _PantallaSesionCerradaState extends State<PantallaSesionCerrada> {
  @override
  void initState() {
    super.initState();
    Senales.aviso();
  }

  @override
  Widget build(BuildContext context) {
    final detalle = switch (widget.motivo) {
      MotivoCierre.inactividad => 'Pasaron 5 minutos sin actividad.',
      MotivoCierre.rechazadaPorServidor => 'Su sesión ya no es válida.',
      MotivoCierre.manual => 'Cerró su sesión.',
    };
    return PantallaNayra(
      titulo: 'Su sesión se cerró',
      textoVoz: 'Su sesión se cerró. $detalle Vuelva a iniciar sesión.',
      mostrarAtras: false,
      hijos: [
        const IconoResultado(TipoResultado.advertencia),
        TextoNayra(detalle, centrado: true),
        BotonNayra(texto: 'Iniciar sesión', icono: Icons.mic, alto: 160, alPulsar: Navegacion.aIniciarSesion),
      ],
    );
  }
}
