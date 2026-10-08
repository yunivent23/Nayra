import 'package:flutter/material.dart';

import '../../app/dependencias.dart';
import '../../componentes/anuncio.dart';
import '../../componentes/botones.dart';
import '../../componentes/contenido.dart';
import '../../componentes/pantalla.dart';
import '../../componentes/teclado.dart';
import '../../flujo/flujo_transferencia.dart';
import '../../modelos/celular.dart';
import '../../modelos/billetera.dart';
import '../../tema/colores.dart';

/// C4–C8 — Transferencia a otro usuario de Nayra (HU-69 a HU-72, HU-60, HU-61; D-042; G-1 modificada el 2026-09-28).
/// C4: la persona escribe el celular del destinatario con un teclado grande. C5: Nayra-Back localiza la cuenta Nayra
/// asociada y la pantalla pregunta "¿Deseas transferir a María Sala...?" con dos botones, Sí y No, buscar otro número.
/// El ID interno de la cuenta nunca se pide ni se muestra, y la búsqueda no prueba quién es titular del número.
/// La transferencia todavía no existe en Nayra-Back: la pantalla lo informa y nunca simula una transferencia.
class PantallaTransferencia extends StatefulWidget {
  const PantallaTransferencia({super.key, this.flujo});
  final FlujoTransferencia? flujo;

  @override
  State<PantallaTransferencia> createState() => _PantallaTransferenciaState();
}

class _PantallaTransferenciaState extends State<PantallaTransferencia> {
  FlujoTransferencia? _flujo;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_flujo == null) {
      final dep = ProveedorNayra.de(context);
      _flujo = widget.flujo ?? FlujoTransferencia(dep.billetera, dep.destinatarios, dep.agenda);
      _flujo!.addListener(_alCambiar);
    }
  }

  void _alCambiar() {
    final f = _flujo!;
    if (f.paso != PasoTransferencia.resultado) return;
    if (f.resultado?.estado == EstadoOperacion.exitoso) {
      Senales.exito();
    } else {
      Senales.aviso();
    }
  }

  @override
  void dispose() {
    _flujo?.removeListener(_alCambiar);
    if (widget.flujo == null) _flujo?.dispose();
    super.dispose();
  }

  Widget _mensaje(FlujoTransferencia f) =>
      f.mensajeEsError ? MensajeEstado(f.mensaje, tipo: TipoMensaje.error) : const SizedBox.shrink();

  void _volverAlMenu() => Navigator.of(context).maybePop();

  @override
  Widget build(BuildContext context) => ListenableBuilder(
        listenable: _flujo!,
        builder: (context, _) {
          final f = _flujo!;
          final d = f.destinatario;
          return switch (f.paso) {
            PasoTransferencia.destinatario => PantallaNayra(
                titulo: '¿A quién transfieres?',
                textoVoz: f.mensaje,
                hijos: [
                  _mensaje(f),
                  _CampoCelular(f.celularTexto),
                  TecladoCelular(texto: f.celularTexto, alCambiar: f.cambiarCelular),
                  BotonNayra(texto: 'Buscar', icono: Icons.search, alto: 90, alPulsar: f.buscar),
                  if (f.conAgenda)
                    BotonNayra(
                      texto: 'Elegir de mis contactos',
                      icono: Icons.contacts,
                      tipo: TipoBoton.secundario,
                      alPulsar: f.buscarEnContactos,
                    ),
                ],
              ),
            PasoTransferencia.contactos => PantallaNayra(
                titulo: 'Tus contactos en Nayra',
                textoVoz: f.mensaje,
                alVolver: f.escribirNumero,
                hijos: [
                  for (final c in f.contactos)
                    BotonNayra(
                      texto: c.nombreAgenda,
                      subtitulo: c.destinatario.nombreVisible,
                      icono: Icons.person,
                      tipo: TipoBoton.secundario,
                      etiqueta: '${c.nombreAgenda}. En Nayra: ${c.destinatario.nombreHablado}.',
                      alPulsar: () => f.elegirContacto(c),
                    ),
                  BotonNayra(texto: 'Escribir el número', icono: Icons.dialpad, alPulsar: f.escribirNumero),
                ],
              ),
            PasoTransferencia.buscando || PasoTransferencia.enviando => PantallaNayra(
                titulo: f.paso == PasoTransferencia.buscando ? 'Buscando' : 'Enviando',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [IndicadorCarga(f.mensaje)],
              ),
            PasoTransferencia.confirmarDestinatario => PantallaNayra(
                titulo: 'Destinatario encontrado',
                textoVoz: f.mensaje,
                alVolver: () => f.confirmarDestinatario(false),
                hijos: [
                  TarjetaInfo(
                    principal: f.preguntaDestinatario,
                    secundario: 'Tiene una cuenta Nayra asociada a este número.',
                    etiqueta: '¿Deseas transferir a ${d!.nombreHablado}? Tiene una cuenta Nayra asociada a este número.',
                  ),
                  BotonNayra(texto: 'Sí', icono: Icons.check, alPulsar: () => f.confirmarDestinatario(true)),
                  BotonNayra(
                    texto: 'No, buscar otro número',
                    icono: Icons.close,
                    tipo: TipoBoton.secundario,
                    alPulsar: () => f.confirmarDestinatario(false),
                  ),
                ],
              ),
            PasoTransferencia.monto => PantallaNayra(
                titulo: '¿Cuánto envías?',
                textoVoz: f.mensaje,
                alVolver: () => f.confirmarDestinatario(false),
                hijos: [
                  _mensaje(f),
                  _Monto(f.montoTexto),
                  const Text('Máximo S/ 499.99',
                      textAlign: TextAlign.center,
                      style: TextStyle(fontSize: 22, fontWeight: FontWeight.w700, color: ColoresNayra.foco)),
                  TecladoMonto(texto: f.montoTexto, alCambiar: f.cambiarMonto),
                  BotonNayra(texto: 'Continuar', icono: Icons.chevron_right, alto: 90, alPulsar: f.continuarConMonto),
                ],
              ),
            PasoTransferencia.revisar => PantallaNayra(
                titulo: '¿Confirmas?',
                textoVoz: f.mensaje,
                mostrarAtras: false,
                hijos: [
                  _mensaje(f),
                  TarjetaInfo(
                    destacado: f.monto!.texto,
                    principal: 'para ${d!.nombreVisible}',
                    etiqueta: 'Vas a enviar ${f.monto!.hablado} para ${d.nombreHablado}.',
                  ),
                  BotonNayra(texto: 'Confirmar', icono: Icons.check, alPulsar: f.confirmar),
                  BotonNayra(texto: 'Cancelar', icono: Icons.close, tipo: TipoBoton.secundario, alPulsar: f.cancelar),
                ],
              ),
            PasoTransferencia.resultado => _resultado(f),
          };
        },
      );

  Widget _resultado(FlujoTransferencia f) {
    final r = f.resultado;
    final (titulo, tipo) = switch (r?.estado) {
      EstadoOperacion.exitoso => ('Transferencia exitosa', TipoResultado.exito),
      EstadoOperacion.fallido => ('Transferencia fallida', TipoResultado.error),
      _ => ('Transferencia cancelada', TipoResultado.advertencia),
    };
    return PantallaNayra(
      titulo: titulo,
      textoVoz: f.mensaje,
      mostrarAtras: false,
      hijos: [
        IconoResultado(tipo, icono: tipo == TipoResultado.advertencia ? Icons.block : null),
        if (r?.estado == EstadoOperacion.exitoso) ...[
          TextoNayra('${f.monto!.texto} a ${f.destinatario!.nombreVisible}', centrado: true),
          if (r!.codigoReferencia != null)
            Semantics(
              label: 'Código ${r.codigoReferencia!.split('').join(' ')}',
              excludeSemantics: true,
              child: Text('Código ${r.codigoReferencia}',
                  textAlign: TextAlign.center, style: const TextStyle(fontSize: 26, fontWeight: FontWeight.w700)),
            ),
        ] else
          TextoNayra(f.mensaje, centrado: true),
        BotonNayra(texto: 'Volver al menú', icono: Icons.home, alto: 140, alPulsar: _volverAlMenu),
      ],
    );
  }
}

class _Monto extends StatelessWidget {
  const _Monto(this.texto);
  final String texto;

  @override
  Widget build(BuildContext context) {
    final s = Soles.leer(texto);
    // Sin región viva: la pantalla ya anuncia el monto en cada pulsación.
    return Semantics(
      label: s == null ? 'Monto vacío' : 'Monto: ${s.hablado}',
      excludeSemantics: true,
      child: Container(
        height: 96,
        alignment: Alignment.center,
        decoration: BoxDecoration(color: ColoresNayra.blanco, borderRadius: BorderRadius.circular(22)),
        child: Text('S/ ${texto.isEmpty ? '0' : texto}',
            style: const TextStyle(fontSize: 50, fontWeight: FontWeight.w700, color: ColoresNayra.textoPrincipal)),
      ),
    );
  }
}

/// Número escrito hasta ahora, agrupado de 3 en 3. El lector lo oye dígito por dígito.
class _CampoCelular extends StatelessWidget {
  const _CampoCelular(this.texto);
  final String texto;

  @override
  Widget build(BuildContext context) => Semantics(
        label: texto.isEmpty ? 'Número de celular: vacío' : 'Número de celular: ${Celular.hablado(texto)}',
        excludeSemantics: true,
        child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          const Text('Número de celular',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.w700, color: ColoresNayra.foco)),
          const SizedBox(height: 6),
          Container(
            height: 96,
            alignment: Alignment.center,
            decoration: BoxDecoration(color: ColoresNayra.blanco, borderRadius: BorderRadius.circular(22)),
            child: FittedBox(
              child: Text(texto.isEmpty ? '9__ ___ ___' : Celular.agrupado(texto),
                  style: const TextStyle(fontSize: 46, fontWeight: FontWeight.w700, color: ColoresNayra.textoPrincipal)),
            ),
          ),
        ]),
      );
}
