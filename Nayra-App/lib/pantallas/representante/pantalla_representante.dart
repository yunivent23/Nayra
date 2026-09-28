import 'package:flutter/material.dart';

import '../../app/dependencias.dart';
import '../../componentes/botones.dart';
import '../../componentes/campo_texto.dart';
import '../../componentes/contenido.dart';
import '../../componentes/pantalla.dart';
import '../../flujo/flujo_representante.dart';
import '../../modelos/registro.dart';
import '../../tema/colores.dart';

/// A2–A3 — Pasos del representante en el registro asistido (D-052 pasos 3–6; HU-99): documento (DNI o CE),
/// consulta al registro de identidad simulado y validación de la identidad.
///
/// Usa los endpoints reales `/api/v1/admin/registros`, que exigen una sesión ADMIN: por eso esta pantalla solo
/// aparece en el menú de una cuenta ADMIN. PENDIENTE DE DECISIÓN: los mockups v3 muestran estos pasos en el
/// celular de la persona, pero el backend no permite hacerlos sin la sesión del representante; el código de
/// registro que se entrega al final es el puente PROVISIONAL del prototipo.
class PantallaRepresentante extends StatefulWidget {
  const PantallaRepresentante({super.key});

  @override
  State<PantallaRepresentante> createState() => _PantallaRepresentanteState();
}

class _PantallaRepresentanteState extends State<PantallaRepresentante> {
  FlujoRepresentante? _flujo;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _flujo ??= FlujoRepresentante(ProveedorNayra.de(context).representante);
  }

  @override
  void dispose() {
    _flujo?.dispose();
    super.dispose();
  }

  Widget _mensaje(FlujoRepresentante f) =>
      f.mensajeEsError ? MensajeEstado(f.mensaje, tipo: TipoMensaje.error) : const SizedBox.shrink();

  @override
  Widget build(BuildContext context) => ListenableBuilder(
        listenable: _flujo!,
        builder: (context, _) {
          final f = _flujo!;
          final r = f.registro;
          return switch (f.paso) {
            PasoRepresentante.documento => PantallaNayra(
                paso: 'Paso 1 de 8',
                titulo: 'Su documento',
                textoVoz: f.mensaje,
                hijos: [
                  const EtiquetaQuien('Con ayuda del representante'),
                  _mensaje(f),
                  _SelectorDocumento(actual: f.tipo, alElegir: f.elegirTipo),
                  CampoTexto(
                    key: ValueKey(f.tipo),
                    etiqueta: 'Número de ${f.tipo.nombre}',
                    textoBoton: 'Consultar',
                    iconoBoton: Icons.search,
                    teclado: f.tipo == TipoDocumento.dni ? TextInputType.number : TextInputType.text,
                    alEnviar: f.consultar,
                  ),
                ],
              ),
            PasoRepresentante.enviando => PantallaNayra(
                titulo: 'Un momento',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [IndicadorCarga(f.mensaje)],
              ),
            PasoRepresentante.validar => PantallaNayra(
                paso: 'Paso 2 de 8',
                titulo: 'Validación de identidad',
                textoVoz: 'Representante: ${f.mensaje}',
                alVolver: f.noCoincide,
                hijos: [
                  const EtiquetaQuien('Lo hace el representante'),
                  _mensaje(f),
                  TarjetaInfo(principal: '${r!.nombres} ${r.apellidos}', secundario: '${f.tipo.codigo} ${f.numeroDocumento}'),
                  BotonNayra(texto: 'Identidad validada', icono: Icons.verified_user, alPulsar: f.validar),
                  BotonNayra(texto: 'No coincide', icono: Icons.close, tipo: TipoBoton.secundario, alPulsar: f.noCoincide),
                ],
              ),
            PasoRepresentante.codigo => PantallaNayra(
                titulo: 'Código de registro',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [
                  const TextoNayra('La persona escribe este código en su celular, en «Registrarme».'),
                  Semantics(
                    label: 'Código: ${r!.codigo.split('').join(' ')}',
                    excludeSemantics: true,
                    child: Container(
                      padding: const EdgeInsets.all(20),
                      decoration: BoxDecoration(color: ColoresNayra.blanco, borderRadius: BorderRadius.circular(22)),
                      child: SelectableText(r.codigo,
                          textAlign: TextAlign.center,
                          style: const TextStyle(
                              fontSize: 34, fontWeight: FontWeight.w700, color: ColoresNayra.textoPrincipal)),
                    ),
                  ),
                  BotonNayra(texto: 'Volver al menú', icono: Icons.home, alPulsar: () => Navigator.of(context).maybePop()),
                ],
              ),
            PasoRepresentante.cancelado => PantallaNayra(
                titulo: 'Registro no validado',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [
                  const IconoResultado(TipoResultado.error),
                  TextoNayra(f.mensaje, centrado: true),
                  BotonNayra(
                      texto: 'Volver al menú',
                      icono: Icons.home,
                      tipo: TipoBoton.secundario,
                      alPulsar: () => Navigator.of(context).maybePop()),
                ],
              ),
          };
        },
      );
}

/// Selector DNI / CE (mockup A2): dos opciones grandes; la elegida se anuncia como seleccionada.
class _SelectorDocumento extends StatelessWidget {
  const _SelectorDocumento({required this.actual, required this.alElegir});
  final TipoDocumento actual;
  final ValueChanged<TipoDocumento> alElegir;

  @override
  Widget build(BuildContext context) => Row(children: [
        for (final (i, t) in TipoDocumento.values.indexed) ...[
          if (i > 0) const SizedBox(width: 10),
          Expanded(
            child: Semantics(
              container: true,
              button: true,
              selected: t == actual,
              inMutuallyExclusiveGroup: true,
              label: '${t.nombre}, opción ${i + 1} de ${TipoDocumento.values.length}',
              onTap: () => alElegir(t),
              excludeSemantics: true,
              child: Material(
                color: t == actual ? ColoresNayra.foco : ColoresNayra.superficie,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(22),
                  side: BorderSide(color: t == actual ? ColoresNayra.foco : ColoresNayra.azulSecundario, width: 3),
                ),
                child: InkWell(
                  borderRadius: BorderRadius.circular(22),
                  onTap: () => alElegir(t),
                  child: SizedBox(
                    height: 96,
                    child: Center(
                      child: Row(mainAxisSize: MainAxisSize.min, children: [
                        if (t == actual) const Icon(Icons.check, size: 30, color: ColoresNayra.azulMarino),
                        Text(t.codigo,
                            style: TextStyle(
                                fontSize: 34,
                                fontWeight: FontWeight.w700,
                                color: t == actual ? ColoresNayra.azulMarino : ColoresNayra.blanco)),
                      ]),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ]);
}
