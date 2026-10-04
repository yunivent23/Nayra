import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../config.dart';
import '../servicios/grabador.dart';
import '../servicios/voz_nayra.dart';
import '../tema/colores.dart';
import 'anuncio.dart';

/// Los siete estados del botón de voz (D-075). Cada uno se distingue por icono, texto y forma, no solo por color.
enum EstadoVoz {
  esperando(Icons.mic, 'Toca para hablar'),
  nayraHabla(Icons.volume_up, 'Nayra está hablando'),
  capturando(Icons.stop_rounded, 'Te escucho'),
  procesando(Icons.more_horiz, 'Procesando'),
  resultado(Icons.check, 'Listo'),
  error(Icons.priority_high, 'No te entendí'),
  cancelado(Icons.close, 'Cancelado');

  const EstadoVoz(this.icono, this.texto);
  final IconData icono;
  final String texto;
}

/// Modo de una captura (D-075). Lo fija el estado del flujo, nunca el contenido del audio: captura ≠ comando ≠
/// autenticación. La etiqueta se muestra sobre el botón cuando la captura no es un comando.
enum ModoCaptura {
  comando(null, ConfiguracionApp.capturaMaximaComando),
  dictado('Dictado', ConfiguracionApp.capturaMaximaDictado),
  pin('PIN · privado', ConfiguracionApp.capturaMaximaDictado),
  verificacionVoz('Verificación de voz', ConfiguracionApp.capturaMaximaVoz);

  const ModoCaptura(this.etiqueta, this.duracionMaxima);
  final String? etiqueta;

  /// Respaldo: si la persona no vuelve a tocar, la captura se cierra sola (valores PENDIENTES DE VALIDACIÓN).
  final Duration duracionMaxima;
}

/// Ciclo de una captura (D-075): tocar → Nayra calla y dice «Te escucho» → se abre el micrófono → la persona
/// habla → tocar otra vez → se entrega solo ese segmento. Semidúplex: el micrófono nunca está abierto mientras
/// Nayra habla. El audio queda solo en memoria (D-013).
class CicloCaptura extends ChangeNotifier {
  CicloCaptura(this._voz, this._grabador) {
    _voz.hablando.addListener(notifyListeners);
  }

  final VozNayra _voz;
  final GrabadorVoz _grabador;

  bool _abriendo = false;
  bool _capturando = false;

  /// true tras [dispose]: una apertura que estaba en curso se deshace sin avisar a nadie.
  bool _desechado = false;
  Timer? _limite;
  ValueChanged<Uint8List>? _destino;

  /// true desde el toque hasta que el micrófono se abre (Nayra está diciendo «Te escucho»).
  bool get abriendo => _abriendo;
  bool get capturando => _capturando;
  bool get nayraHabla => _voz.hablando.value;

  /// Primer toque: abre la captura en [modo]; segundo toque: la cierra y entrega el audio a [alCapturar].
  Future<void> alternar(ModoCaptura modo, ValueChanged<Uint8List> alCapturar) async {
    if (_capturando) return cerrar();
    if (_abriendo) return;
    _abriendo = true;
    notifyListeners();
    try {
      if (!await _grabador.tienePermiso()) {
        await _decir('Necesito permiso para usar el micrófono.');
        return;
      }
      try {
        await _voz.callar();
        await _voz.decir('Te escucho.');
        if (_desechado) return;
        // Desde aquí Nayra no habla hasta liberar el micrófono; si no se confirma el silencio, no se abre.
        await _voz.reservarMicrofono();
        if (_desechado) return _voz.liberarMicrofono();
      } on FalloVoz catch (e, pila) {
        // El motor de voz no confirmó que calló: el micrófono no se abre y el botón vuelve a esperar.
        _informar(e, pila);
        return;
      }
      try {
        Senales.aviso();
        await _grabador.iniciar();
        if (_desechado) {
          // La pantalla se cerró mientras el micrófono se abría: se cierra antes de devolver la voz.
          try {
            await _grabador.cancelar();
          } finally {
            _voz.liberarMicrofono();
          }
          return;
        }
        _capturando = true;
        _destino = alCapturar;
        _limite = Timer(modo.duracionMaxima, cerrar);
      } catch (_) {
        // Micrófono ocupado o error del sistema: se descarta lo grabado y se puede volver a intentar.
        await _grabador.cancelar().catchError((_) {});
        _capturando = false;
        _voz.liberarMicrofono();
        if (!_desechado) await _decir('No pude usar el micrófono. Inténtalo otra vez.');
      }
    } finally {
      _abriendo = false;
      if (!_desechado) notifyListeners();
    }
  }

  /// Cierra la captura y entrega lo grabado hasta ese momento.
  Future<void> cerrar() async {
    if (!_capturando) return;
    _limite?.cancel();
    _capturando = false;
    notifyListeners();
    final destino = _destino;
    _destino = null;
    final Uint8List wav;
    try {
      wav = await _grabador.detener();
    } finally {
      // Nayra vuelve a poder hablar solo cuando el micrófono ya se cerró.
      _voz.liberarMicrofono();
    }
    destino?.call(wav);
  }

  /// Descarta la captura en curso sin procesarla.
  Future<void> cancelar() async {
    if (!_capturando) return;
    _limite?.cancel();
    _capturando = false;
    _destino = null;
    notifyListeners();
    try {
      await _grabador.cancelar();
    } finally {
      _voz.liberarMicrofono();
    }
  }

  /// Mensajes del propio ciclo: si el motor de voz falla, el error se informa y el botón sigue disponible.
  Future<void> _decir(String texto) async {
    try {
      await _voz.decir(texto);
    } on FalloVoz catch (e, pila) {
      _informar(e, pila);
    }
  }

  void _informar(FalloVoz e, StackTrace pila) => FlutterError.reportError(FlutterErrorDetails(
      exception: e, stack: pila, library: 'Nayra', context: ErrorDescription('en la captura de voz')));

  @override
  void dispose() {
    _desechado = true;
    _voz.hablando.removeListener(notifyListeners);
    if (_capturando) {
      _limite?.cancel();
      _grabador.cancelar().whenComplete(_voz.liberarMicrofono);
    }
    super.dispose();
  }
}

/// Lo que muestra y hace el botón de voz en una pantalla.
class BotonVoz {
  const BotonVoz({
    required this.estado,
    required this.alTocar,
    this.modo,
    this.texto,
    this.subtexto,
    this.derecha,
    this.antesDeRepetir,
  });

  final EstadoVoz estado;
  final VoidCallback? alTocar;
  final ModoCaptura? modo;

  /// Texto bajo el botón; por defecto, el del estado.
  final String? texto;
  final String? subtexto;

  /// Botón secundario derecho (Cancelar, Menú…); por defecto, «Atrás» si la pantalla lo permite.
  final AccionDock? derecha;

  /// Se espera antes de que «Repetir» haga hablar a Nayra: cierra una captura abierta (semidúplex, D-075).
  final Future<void> Function()? antesDeRepetir;

  /// Estado que se muestra: «Nayra habla» y «Capturando» los decide el ciclo; el resto, el flujo.
  static EstadoVoz combinar(CicloCaptura ciclo, EstadoVoz delFlujo) {
    if (ciclo.capturando) return EstadoVoz.capturando;
    if (ciclo.nayraHabla && delFlujo == EstadoVoz.esperando) return EstadoVoz.nayraHabla;
    return delFlujo;
  }
}

class AccionDock {
  const AccionDock(this.texto, this.icono, this.accion);
  final String texto;
  final IconData icono;
  final VoidCallback accion;
}

/// Dock fijo del botón de voz (D-075): mismo lugar (abajo al centro), tamaño, forma y comportamiento en todas las
/// pantallas. «Repetir» a la izquierda y «Atrás», «Cancelar» o «Menú» a la derecha son solo apoyo.
class DockVoz extends StatelessWidget {
  const DockVoz({super.key, required this.boton, required this.repetir, this.atras});

  final BotonVoz boton;
  final VoidCallback repetir;
  final VoidCallback? atras;

  static const diametro = 120.0;

  @override
  Widget build(BuildContext context) {
    final e = boton.estado;
    final derecha = boton.derecha ?? (atras == null ? null : AccionDock('Atrás', Icons.arrow_back, atras!));
    final texto = boton.texto ?? e.texto;
    final sub = boton.subtexto ??
        switch (e) {
          EstadoVoz.nayraHabla => 'toca para interrumpir',
          EstadoVoz.capturando => 'toca para terminar',
          _ => null,
        };
    return Padding(
      padding: const EdgeInsets.fromLTRB(12, 4, 12, 10),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Alto mínimo fijo para que el botón no se mueva al aparecer la etiqueta del modo.
          ConstrainedBox(
            constraints: const BoxConstraints(minHeight: 34),
            child: boton.modo?.etiqueta == null ? null : _EtiquetaModo(boton.modo!),
          ),
          Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Expanded(
                child: Center(
                  child: _Lateral(AccionDock('Repetir', Icons.replay, repetir), etiqueta: 'Repetir instrucción'),
                ),
              ),
              _BotonCentral(estado: e, alTocar: boton.alTocar, etiqueta: [texto, ?sub].join(', ')),
              Expanded(child: Center(child: derecha == null ? const SizedBox.shrink() : _Lateral(derecha))),
            ],
          ),
          const SizedBox(height: 6),
          // Alto mínimo fijo para que el botón no se mueva al cambiar el texto (crece con la letra del sistema).
          ConstrainedBox(
            constraints: const BoxConstraints(minHeight: 58),
            child: ExcludeSemantics(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    texto,
                    textAlign: TextAlign.center,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w700, color: ColoresNayra.blanco),
                  ),
                  if (sub != null)
                    Text(
                      sub,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(fontSize: 18, color: ColoresNayra.textoSecundarioSobreOscuro),
                    ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _EtiquetaModo extends StatelessWidget {
  const _EtiquetaModo(this.modo);
  final ModoCaptura modo;

  @override
  Widget build(BuildContext context) => Center(
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
          decoration: BoxDecoration(color: ColoresNayra.foco, borderRadius: BorderRadius.circular(20)),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                switch (modo) {
                  ModoCaptura.pin => Icons.lock,
                  ModoCaptura.verificacionVoz => Icons.graphic_eq,
                  _ => Icons.format_list_numbered,
                },
                size: 20,
                color: ColoresNayra.azulMarino,
              ),
              const SizedBox(width: 6),
              Flexible(
                child: Text(
                  modo.etiqueta!,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700, color: ColoresNayra.azulMarino),
                ),
              ),
            ],
          ),
        ),
      );
}

class _BotonCentral extends StatelessWidget {
  const _BotonCentral({required this.estado, required this.alTocar, required this.etiqueta});
  final EstadoVoz estado;
  final VoidCallback? alTocar;
  final String etiqueta;

  @override
  Widget build(BuildContext context) {
    // (relleno, icono, borde): el borde blanco grueso marca la captura; el gris, la cancelación.
    final (fondo, color, borde) = switch (estado) {
      EstadoVoz.esperando => (ColoresNayra.accionPrincipal, ColoresNayra.azulMarino, ColoresNayra.accionPrincipal),
      EstadoVoz.nayraHabla => (ColoresNayra.blanco, ColoresNayra.azulMarino, ColoresNayra.blanco),
      EstadoVoz.capturando => (ColoresNayra.celeste, ColoresNayra.azulMarino, ColoresNayra.blanco),
      EstadoVoz.procesando => (ColoresNayra.superficie, ColoresNayra.blanco, ColoresNayra.superficie),
      EstadoVoz.resultado => (ColoresNayra.exito, ColoresNayra.blanco, ColoresNayra.exito),
      EstadoVoz.error => (ColoresNayra.error, ColoresNayra.blanco, ColoresNayra.error),
      EstadoVoz.cancelado => (ColoresNayra.cancelado, ColoresNayra.blanco, ColoresNayra.cancelado),
    };
    final capturando = estado == EstadoVoz.capturando;
    return Semantics(
      key: const Key('botonVoz'),
      container: true,
      button: true,
      enabled: alTocar != null,
      label: 'Botón de voz. $etiqueta',
      liveRegion: true,
      onTap: alTocar,
      excludeSemantics: true,
      child: GestureDetector(
        onTap: alTocar,
        child: SizedBox(
          width: DockVoz.diametro + 24,
          height: DockVoz.diametro + 24,
          child: Stack(
            alignment: Alignment.center,
            children: [
              // Anillos fijos alrededor del botón mientras se escucha (forma distinta, no solo color).
              if (capturando)
                Container(
                  width: DockVoz.diametro + 24,
                  height: DockVoz.diametro + 24,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: ColoresNayra.celeste.withValues(alpha: 0.6), width: 6),
                  ),
                ),
              if (estado == EstadoVoz.procesando)
                const SizedBox(
                  width: DockVoz.diametro + 14,
                  height: DockVoz.diametro + 14,
                  child: CircularProgressIndicator(strokeWidth: 7, color: ColoresNayra.foco),
                ),
              Container(
                width: DockVoz.diametro,
                height: DockVoz.diametro,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: fondo,
                  border: Border.all(color: borde, width: capturando ? 6 : 3),
                ),
                child: Icon(estado.icono, size: 58, color: color),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _Lateral extends StatelessWidget {
  const _Lateral(this.accion, {this.etiqueta});
  final AccionDock accion;
  final String? etiqueta;

  @override
  Widget build(BuildContext context) => Semantics(
        container: true,
        button: true,
        label: etiqueta ?? accion.texto,
        onTap: accion.accion,
        excludeSemantics: true,
        child: InkWell(
          customBorder: const StadiumBorder(),
          onTap: accion.accion,
          child: ConstrainedBox(
            constraints: const BoxConstraints(minWidth: 72, minHeight: 72),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: ColoresNayra.superficie,
                    border: Border.all(color: ColoresNayra.azulSecundario, width: 3),
                  ),
                  child: Icon(accion.icono, size: 30, color: ColoresNayra.blanco),
                ),
                const SizedBox(height: 4),
                Text(
                  accion.texto,
                  textAlign: TextAlign.center,
                  style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700, color: ColoresNayra.blanco),
                ),
              ],
            ),
          ),
        ),
      );
}
