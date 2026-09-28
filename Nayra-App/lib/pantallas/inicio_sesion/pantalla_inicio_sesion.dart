import 'package:flutter/material.dart';

import '../../api/cliente_http.dart';
import '../../app/dependencias.dart';
import '../../app/navegacion.dart';
import '../../componentes/anuncio.dart';
import '../../componentes/botones.dart';
import '../../componentes/contenido.dart';
import '../../componentes/grabacion.dart';
import '../../componentes/logo.dart';
import '../../componentes/pantalla.dart';
import '../../componentes/teclado.dart';
import '../../flujo/flujo_inicio_sesion.dart';
import '../../flujo/mensajes.dart';

/// B1–B6 — Inicio de sesión (D-037 modificada por D-061; HU-12, HU-40 a HU-47):
/// «Iniciar sesión Nayra» → celular vinculado → PIN → frase de desafío → voz → resultado → billetera.
///
/// El comando de voz «Iniciar sesión Nayra» (B1) no está implementado: su reconocimiento es parte de D-046
/// (resto, PENDIENTE). Por ahora se activa con el botón.
class PantallaInicioSesion extends StatefulWidget {
  const PantallaInicioSesion({super.key, this.flujo});

  /// Flujo a usar (pruebas); por defecto se crea con las dependencias de la app.
  final FlujoInicioSesion? flujo;

  @override
  State<PantallaInicioSesion> createState() => _PantallaInicioSesionState();
}

class _PantallaInicioSesionState extends State<PantallaInicioSesion> {
  late final FlujoInicioSesion _flujo;
  late final Dependencias _d;
  bool _fraseMostrada = false;
  String? _nombre;
  bool _entrando = false;
  bool _listo = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_listo) return;
    _listo = true;
    _d = ProveedorNayra.de(context);
    _flujo = widget.flujo ?? FlujoInicioSesion(_d.autenticacion, _d.dispositivo);
    _flujo.addListener(_alCambiar);
  }

  void _alCambiar() {
    final f = _flujo;
    if (f.paso == PasoInicioSesion.autenticado && !_entrando) _entrar();
    if (f.paso == PasoInicioSesion.bloqueada || (f.paso == PasoInicioSesion.terminado && f.mensajeEsError)) {
      Senales.error();
    }
  }

  /// B6: entrega el JWT al gestor de sesión y saluda con el nombre real (GET /usuarios/me).
  Future<void> _entrar() async {
    _entrando = true;
    Senales.exito();
    final token = _flujo.tomarSesion();
    if (token == null) return;
    try {
      final datos = await _d.sesion.iniciar(token);
      if (mounted) setState(() => _nombre = datos.primerNombre);
    } on FalloNayra {
      // La sesión sigue; solo no se pudo saludar por el nombre.
      if (mounted) setState(() {});
    }
  }

  @override
  void dispose() {
    _flujo.removeListener(_alCambiar);
    super.dispose();
  }

  Widget _mensaje(FlujoInicioSesion f) =>
      f.mensajeEsError ? MensajeEstado(f.mensaje, tipo: TipoMensaje.error) : const SizedBox.shrink();

  @override
  Widget build(BuildContext context) => ListenableBuilder(
        listenable: _flujo,
        builder: (context, _) {
          final f = _flujo;
          return switch (f.paso) {
            PasoInicioSesion.inicial => PantallaNayra(
                textoVoz: 'Toque el botón Iniciar sesión Nayra.',
                cabecera: const LogoNayra(tamano: 56),
                hijos: [
                  BotonNayra(
                    texto: 'Iniciar sesión Nayra',
                    icono: Icons.mic,
                    alto: 300,
                    vertical: true,
                    tamanoIcono: 110,
                    alPulsar: f.iniciar,
                  ),
                  const TextoNayra('Toque el botón para empezar.', centrado: true, secundario: true),
                ],
              ),
            PasoInicioSesion.verificando => PantallaNayra(
                titulo: f.desafio != null ? 'Verificando su voz' : 'Verificando',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [IndicadorCarga(f.mensaje)],
              ),
            PasoInicioSesion.pin => PantallaNayra(
                titulo: 'Ingrese su PIN',
                textoVoz: f.mensaje,
                hijos: [
                  _mensaje(f),
                  TecladoPin(alCompletar: f.enviarPin, grabador: _d.grabador, alDictar: f.enviarPinDictado),
                ],
              ),
            PasoInicioSesion.voz when !_fraseMostrada => PantallaNayra(
                titulo: 'Verificación de voz',
                textoVoz: 'PIN correcto. Ahora verificaremos su voz. Toque Escuchar la frase.',
                hijos: [
                  const TextoNayra('Escuche una frase y repítala con su voz.'),
                  BotonNayra(
                    texto: 'Escuchar la frase',
                    icono: Icons.volume_up,
                    alto: 230,
                    vertical: true,
                    alPulsar: () => setState(() => _fraseMostrada = true),
                  ),
                ],
              ),
            PasoInicioSesion.voz => PantallaNayra(
                titulo: 'Repita la frase',
                textoVoz: f.mensajeEsError ? f.mensaje : instruccionDesafio(f.desafio!.texto),
                hijos: [
                  _mensaje(f),
                  FraseDesafio(f.desafio!.texto),
                  BotonGrabacion(key: ValueKey(f.desafio!.id), grabador: _d.grabador, alTerminar: f.enviarVoz),
                ],
              ),
            PasoInicioSesion.autenticado => PantallaNayra(
                titulo: 'Identidad verificada',
                textoVoz: _nombre == null ? 'Identidad verificada.' : 'Identidad verificada. Le damos la bienvenida, $_nombre.',
                mostrarAtras: false,
                hijos: [
                  const IconoResultado(TipoResultado.exito),
                  TextoNayra(_nombre == null ? 'Le damos la bienvenida a Nayra.' : 'Le damos la bienvenida, $_nombre.', centrado: true),
                  BotonNayra(
                    texto: 'Ir a mi billetera',
                    icono: Icons.account_balance_wallet,
                    alto: 160,
                    alPulsar: _d.sesion.activa ? Navegacion.aBilletera : null,
                  ),
                ],
              ),
            PasoInicioSesion.bloqueada => PantallaNayra(
                titulo: 'Cuenta bloqueada',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [
                  const IconoResultado(TipoResultado.error, icono: Icons.lock_outline),
                  TextoNayra(f.mensaje, centrado: true),
                  BotonNayra(texto: 'Volver al inicio', icono: Icons.home, tipo: TipoBoton.secundario, alPulsar: Navegacion.alInicio),
                ],
              ),
            PasoInicioSesion.terminado => PantallaNayra(
                titulo: 'No se pudo iniciar sesión',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [
                  const IconoResultado(TipoResultado.error),
                  TextoNayra(f.mensaje, centrado: true),
                  BotonNayra(texto: 'Intentar de nuevo', icono: Icons.replay, alPulsar: () {
                    _fraseMostrada = false;
                    f.iniciar();
                  }),
                  BotonNayra(texto: 'Volver al inicio', icono: Icons.home, tipo: TipoBoton.secundario, alPulsar: Navegacion.alInicio),
                ],
              ),
          };
        },
      );
}
