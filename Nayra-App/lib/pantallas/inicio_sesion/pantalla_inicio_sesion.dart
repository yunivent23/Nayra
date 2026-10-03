import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../../api/cliente_http.dart';
import '../../app/dependencias.dart';
import '../../app/navegacion.dart';
import '../../componentes/anuncio.dart';
import '../../componentes/boton_voz.dart';
import '../../componentes/botones.dart';
import '../../componentes/contenido.dart';
import '../../componentes/logo.dart';
import '../../componentes/pantalla.dart';
import '../../componentes/teclado.dart';
import '../../flujo/flujo_inicio_sesion.dart';

/// B1–B6 — Inicio de sesión (D-037 modificada por D-061; HU-12, HU-40 a HU-47) con la voz propia de Nayra y
/// el botón de voz fijo (D-075): «Iniciar sesión Nayra» → celular vinculado → PIN → frase → voz → billetera.
///
/// El comando hablado «Iniciar sesión Nayra» todavía no se reconoce (D-046 resto y D-076, PENDIENTES): en la
/// primera pantalla el botón de voz inicia el proceso con un toque.
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
  late final CicloCaptura _ciclo;
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
    _ciclo = CicloCaptura(_d.voz, _d.grabador);
    _flujo.addListener(_alCambiar);
  }

  void _alCambiar() {
    final f = _flujo;
    // Una captura abierta solo tiene sentido en los pasos de PIN y de voz.
    if (f.paso != PasoInicioSesion.pin && f.paso != PasoInicioSesion.voz) _ciclo.cancelar();
    if (f.paso == PasoInicioSesion.autenticado && !_entrando) _entrar();
    if (f.paso == PasoInicioSesion.bloqueada || (f.paso == PasoInicioSesion.terminado && f.mensajeEsError)) {
      Senales.error();
    }
  }

  /// B6: entrega el JWT al gestor de sesión, saluda con el nombre real (GET /usuarios/me) y abre la billetera.
  Future<void> _entrar() async {
    _entrando = true;
    Senales.exito();
    final token = _flujo.tomarSesion();
    if (token == null) return;
    try {
      final datos = await _d.sesion.iniciar(token);
      _nombre = datos.primerNombre;
    } on FalloNayra {
      // La sesión sigue; solo no se pudo saludar por el nombre.
    }
    if (!mounted) return;
    setState(() {});
    await _d.voz.decir(_saludo);
    if (mounted && _d.sesion.activa) Navegacion.aBilletera();
  }

  String get _saludo => _nombre == null ? 'Identidad verificada.' : 'Identidad verificada. ¡Hola, $_nombre!';

  @override
  void dispose() {
    _flujo.removeListener(_alCambiar);
    _ciclo.dispose();
    super.dispose();
  }

  Widget _mensaje(FlujoInicioSesion f) =>
      f.mensajeEsError ? MensajeEstado(f.mensaje, tipo: TipoMensaje.error) : const SizedBox.shrink();

  /// Botón de voz de los pasos que capturan (PIN dictado o verificación de voz).
  BotonVoz _botonCaptura(ModoCaptura modo, ValueChanged<Uint8List> destino, {required bool error, String? texto}) {
    final estado = BotonVoz.combinar(_ciclo, error ? EstadoVoz.error : EstadoVoz.esperando);
    return BotonVoz(
      estado: _ciclo.abriendo ? EstadoVoz.nayraHabla : estado,
      modo: modo,
      texto: estado == EstadoVoz.capturando || _ciclo.abriendo ? null : texto,
      subtexto: estado == EstadoVoz.error ? 'toca para intentar otra vez' : null,
      alTocar: () => _ciclo.alternar(modo, destino),
      derecha: _ciclo.capturando ? AccionDock('Cancelar', Icons.close, _ciclo.cancelar) : null,
    );
  }

  @override
  Widget build(BuildContext context) => ListenableBuilder(
        listenable: Listenable.merge([_flujo, _ciclo]),
        builder: (context, _) {
          final f = _flujo;
          return switch (f.paso) {
            PasoInicioSesion.inicial => PantallaNayra(
                titulo: 'Hola de nuevo',
                textoVoz: 'Hola de nuevo. Toca el botón de voz para iniciar sesión.',
                cabecera: const LogoNayra(tamano: 56),
                hijos: const [TextoNayra('Toca el botón de voz para iniciar sesión.', centrado: true)],
                botonVoz: BotonVoz(
                  estado: BotonVoz.combinar(_ciclo, EstadoVoz.esperando),
                  texto: 'Iniciar sesión Nayra',
                  subtexto: 'toca el botón',
                  alTocar: () {
                    _d.voz.callar();
                    f.iniciar();
                  },
                ),
              ),
            PasoInicioSesion.verificando => PantallaNayra(
                titulo: f.desafio != null ? 'Verificando tu voz' : 'Verificando',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [IndicadorCarga(f.mensaje)],
                botonVoz: const BotonVoz(estado: EstadoVoz.procesando, alTocar: null),
              ),
            PasoInicioSesion.pin => PantallaNayra(
                titulo: 'Ingresa tu PIN',
                textoVoz: f.mensaje,
                hijos: [
                  _mensaje(f),
                  TecladoPin(alCompletar: f.enviarPin, habilitado: !_ciclo.capturando && !_ciclo.abriendo),
                ],
                botonVoz: _botonCaptura(ModoCaptura.pin, f.enviarPinDictado,
                    error: f.mensajeEsError, texto: 'Toca para dictar'),
              ),
            PasoInicioSesion.voz => PantallaNayra(
                titulo: 'Verifiquemos tu voz',
                textoVoz: f.mensaje,
                hijos: [
                  _mensaje(f),
                  const TextoNayra('Toca el botón y repite la frase. Toca otra vez al terminar.'),
                  FraseDesafio(f.desafio!.texto),
                ],
                botonVoz: _botonCaptura(ModoCaptura.verificacionVoz, f.enviarVoz,
                    error: f.mensajeEsError),
              ),
            PasoInicioSesion.autenticado => PantallaNayra(
                titulo: 'Identidad verificada',
                textoVoz: _saludo,
                hablarAlMostrar: false,
                mostrarAtras: false,
                hijos: [
                  const IconoResultado(TipoResultado.exito),
                  TextoNayra(_nombre == null ? '¡Hola!' : '¡Hola, $_nombre!', centrado: true),
                  BotonNayra(
                    texto: 'Ir a mi billetera',
                    icono: Icons.account_balance_wallet,
                    tipo: TipoBoton.secundario,
                    alPulsar: _d.sesion.activa ? Navegacion.aBilletera : null,
                  ),
                ],
                botonVoz: const BotonVoz(estado: EstadoVoz.resultado, alTocar: null),
              ),
            PasoInicioSesion.bloqueada => PantallaNayra(
                titulo: 'Cuenta bloqueada',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [
                  const IconoResultado(TipoResultado.error, icono: Icons.lock_outline),
                  TextoNayra(f.mensaje, centrado: true),
                ],
                botonVoz: BotonVoz(
                  estado: EstadoVoz.error,
                  texto: 'Cuenta bloqueada',
                  alTocar: null,
                  derecha: const AccionDock('Inicio', Icons.home, Navegacion.alInicio),
                ),
              ),
            PasoInicioSesion.terminado => PantallaNayra(
                titulo: 'No pude iniciar tu sesión',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [
                  const IconoResultado(TipoResultado.error),
                  TextoNayra(f.mensaje, centrado: true),
                ],
                botonVoz: BotonVoz(
                  estado: EstadoVoz.error,
                  texto: 'Intentar de nuevo',
                  subtexto: 'toca el botón',
                  alTocar: f.iniciar,
                  derecha: const AccionDock('Inicio', Icons.home, Navegacion.alInicio),
                ),
              ),
          };
        },
      );
}
