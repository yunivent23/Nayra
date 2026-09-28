import 'package:flutter/material.dart';

import '../../app/dependencias.dart';
import '../../app/navegacion.dart';
import '../../componentes/anuncio.dart';
import '../../componentes/botones.dart';
import '../../componentes/campo_texto.dart';
import '../../componentes/contenido.dart';
import '../../componentes/grabacion.dart';
import '../../componentes/pantalla.dart';
import '../../componentes/teclado.dart';
import '../../flujo/flujo_registro.dart';
import '../inicio_sesion/pantalla_inicio_sesion.dart';

/// A4–A9 — Registro inicial asistido en el celular de la persona, guiada en persona por el representante
/// (D-052; HU-01 a HU-04, HU-26 a HU-33, HU-117 a HU-119). Numeración de los mockups v3 (8 pasos).
///
/// Los pasos 1–2 (documento y validación de identidad) los hace el representante con su sesión y entregan el
/// código de registro (PantallaRepresentante). Ese código es un mecanismo PROVISIONAL del prototipo.
/// No implementado por falta de contrato o de decisión: dictar el PIN al crearlo (el registro solo acepta el PIN
/// tecleado) y el tutorial (HU-122, contenido sin definir).
class PantallaRegistro extends StatefulWidget {
  const PantallaRegistro({super.key, this.flujo});
  final FlujoRegistro? flujo;

  @override
  State<PantallaRegistro> createState() => _PantallaRegistroState();
}

class _PantallaRegistroState extends State<PantallaRegistro> {
  late FlujoRegistro _flujo;
  late Dependencias _d;
  bool _listo = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_listo) return;
    _listo = true;
    _d = ProveedorNayra.de(context);
    _flujo = widget.flujo ?? FlujoRegistro(_d.registro, _d.dispositivo);
    _flujo.addListener(_alCambiar);
  }

  void _alCambiar() {
    if (_flujo.paso == PasoRegistro.terminado) Senales.exito();
    if (_flujo.paso == PasoRegistro.error) Senales.error();
  }

  @override
  void dispose() {
    _flujo.removeListener(_alCambiar);
    super.dispose();
  }

  /// «Atrás» vuelve al paso anterior del mismo registro; en el primer paso sale del registro.
  void _atras() {
    if (!_flujo.volver()) Navigator.of(context).maybePop();
  }

  Widget _mensaje(FlujoRegistro f) =>
      f.mensajeEsError ? MensajeEstado(f.mensaje, tipo: TipoMensaje.error) : const SizedBox.shrink();

  @override
  Widget build(BuildContext context) => ListenableBuilder(
        listenable: _flujo,
        builder: (context, _) {
          final f = _flujo;
          final datos = f.datos;
          return switch (f.paso) {
            PasoRegistro.codigo => PantallaNayra(
                titulo: 'Código de registro',
                textoVoz: f.mensaje,
                hijos: [
                  const EtiquetaQuien('Con ayuda del representante'),
                  _mensaje(f),
                  CampoTexto(
                    key: const ValueKey('codigo'),
                    etiqueta: 'Código de registro',
                    pista: 'El representante le entrega este código después de validar su identidad.',
                    textoBoton: 'Continuar',
                    alEnviar: f.ingresarCodigo,
                  ),
                ],
              ),
            PasoRegistro.confirmarDatos => PantallaNayra(
                paso: 'Paso 3 de 8',
                titulo: '¿Son sus datos?',
                alVolver: _atras,
                textoVoz: f.mensaje,
                hijos: [
                  TarjetaInfo(principal: '${datos!.nombres} ${datos.apellidos}'),
                  BotonNayra(texto: 'Sí, son mis datos', icono: Icons.check, alPulsar: () => f.confirmarDatos(true)),
                  BotonNayra(
                    texto: 'No son mis datos',
                    icono: Icons.close,
                    tipo: TipoBoton.secundario,
                    alPulsar: () => f.confirmarDatos(false),
                  ),
                ],
              ),
            PasoRegistro.celular => PantallaNayra(
                paso: 'Paso 4 de 8',
                titulo: 'Su número de celular',
                alVolver: _atras,
                textoVoz: f.mensaje,
                hijos: [
                  _mensaje(f),
                  CampoTexto(
                    key: const ValueKey('celular'),
                    etiqueta: 'Número de celular',
                    pista: 'Es su dato de contacto.',
                    textoBoton: 'Continuar',
                    teclado: TextInputType.phone,
                    alEnviar: f.ingresarCelular,
                  ),
                ],
              ),
            PasoRegistro.pin => PantallaNayra(
                paso: 'Paso 5 de 8',
                titulo: 'Cree su PIN',
                alVolver: _atras,
                textoVoz: f.mensaje,
                hijos: [_mensaje(f), TecladoPin(key: const ValueKey('pin'), alCompletar: f.ingresarPin)],
              ),
            PasoRegistro.confirmarPin => PantallaNayra(
                paso: 'Paso 5 de 8',
                titulo: 'Confirme su PIN',
                alVolver: _atras,
                textoVoz: f.mensaje,
                hijos: [TecladoPin(key: const ValueKey('confirmar'), alCompletar: f.confirmarPin)],
              ),
            PasoRegistro.vinculando => PantallaNayra(
                paso: 'Paso 6 de 8',
                titulo: 'Vinculando su celular',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: const [
                  IconoResultado(TipoResultado.espera),
                  TextoNayra('Espere, por favor.', centrado: true),
                  TextoNayra('Este será el único celular de su cuenta.', centrado: true, secundario: true),
                ],
              ),
            PasoRegistro.enviando => PantallaNayra(
                titulo: 'Un momento',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [IndicadorCarga(f.mensaje)],
              ),
            PasoRegistro.muestra => PantallaNayra(
                paso: 'Paso 7 de 8',
                titulo: 'Registro de voz',
                textoVoz: f.mensaje,
                hijos: [
                  EtiquetaProgreso('Muestra ${f.muestrasValidas + 1} de ${f.muestrasRequeridas}'),
                  _mensaje(f),
                  FraseDesafio(f.desafio!.texto),
                  BotonGrabacion(key: ValueKey(f.desafio!.id), grabador: _d.grabador, alTerminar: f.enviarMuestra),
                ],
              ),
            PasoRegistro.terminado => PantallaNayra(
                paso: 'Paso 8 de 8',
                titulo: 'Registro completado',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [
                  const IconoResultado(TipoResultado.exito),
                  const TextoNayra('Ya puede iniciar sesión.', centrado: true),
                  BotonNayra(
                    texto: 'Iniciar sesión',
                    icono: Icons.mic,
                    alto: 160,
                    alPulsar: () => Navegacion.reemplazarTodo((_) => const PantallaInicioSesion()),
                  ),
                ],
              ),
            PasoRegistro.error => PantallaNayra(
                titulo: 'Registro no completado',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [
                  const IconoResultado(TipoResultado.error),
                  TextoNayra(f.mensaje, centrado: true),
                  BotonNayra(texto: 'Volver al inicio', icono: Icons.home, tipo: TipoBoton.secundario, alPulsar: Navegacion.alInicio),
                ],
              ),
          };
        },
      );
}
