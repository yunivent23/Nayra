import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../servicios/grabador.dart';
import '../tema/colores.dart';
import '../tema/tema.dart';
import 'anuncio.dart';

/// Tecla grande del teclado (84 px de alto en los mockups v3).
class _Tecla extends StatelessWidget {
  const _Tecla({required this.etiqueta, required this.accion, this.visible, this.icono, this.clara = false});
  final String etiqueta;
  final VoidCallback? accion;
  final String? visible;
  final IconData? icono;
  final bool clara;

  @override
  Widget build(BuildContext context) {
    final fondo = clara ? ColoresNayra.blanco : ColoresNayra.superficie;
    final color = clara ? ColoresNayra.azulMarino : ColoresNayra.blanco;
    return Padding(
      padding: const EdgeInsets.all(4.5),
      child: Semantics(
        container: true,
        button: true,
        enabled: accion != null,
        label: etiqueta,
        onTap: accion,
        excludeSemantics: true,
        child: Material(
          color: fondo,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
            side: BorderSide(color: clara ? ColoresNayra.blanco : ColoresNayra.azulSecundario, width: 3),
          ),
          child: InkWell(
            borderRadius: BorderRadius.circular(20),
            onTap: accion,
            child: ConstrainedBox(
              // Alto mínimo de 84 px; crece si la persona agranda la letra del sistema.
              constraints: const BoxConstraints(minHeight: MedidasNayra.altoTecla),
              child: Center(
                child: icono == null
                    ? Text(visible ?? etiqueta,
                        style: TextStyle(fontSize: 40, fontWeight: FontWeight.w700, color: color))
                    : Column(mainAxisSize: MainAxisSize.min, children: [
                        Icon(icono, size: 32, color: color),
                        Text(visible ?? etiqueta,
                            style: TextStyle(fontSize: 17, fontWeight: FontWeight.w700, color: color)),
                      ]),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

Widget _rejilla(List<Widget> teclas) => Column(children: [
      for (var f = 0; f < 4; f++)
        Row(children: [for (var c = 0; c < 3; c++) Expanded(child: teclas[f * 3 + c])]),
    ]);

/// Teclado numérico accesible para el PIN de 6 dígitos (D-061).
///
/// - Teclas grandes con etiqueta para el lector de pantalla.
/// - Solo se anuncia el avance ("3 de 6 dígitos"), nunca los dígitos pulsados.
/// - El PIN se mantiene solo en memoria y se entrega al completarse; no se guarda ni se registra.
/// - Con [grabador] y [alDictar], la tecla izquierda permite dictar el PIN (D-061): primera pulsación graba,
///   segunda detiene y entrega el audio en memoria.
class TecladoPin extends StatefulWidget {
  const TecladoPin({super.key, required this.alCompletar, this.habilitado = true, this.grabador, this.alDictar});

  final ValueChanged<String> alCompletar;
  final bool habilitado;
  final GrabadorVoz? grabador;
  final ValueChanged<Uint8List>? alDictar;

  static const longitud = 6;

  @override
  State<TecladoPin> createState() => _TecladoPinState();
}

class _TecladoPinState extends State<TecladoPin> {
  final _digitos = <String>[];
  bool _dictando = false;

  void _pulsar(String d) {
    if (!widget.habilitado || _dictando || _digitos.length >= TecladoPin.longitud) return;
    setState(() => _digitos.add(d));
    if (_digitos.length == TecladoPin.longitud) {
      final pin = _digitos.join();
      setState(_digitos.clear);
      anunciar(context, 'PIN completo. Verificando.');
      widget.alCompletar(pin);
    } else {
      anunciar(context, '${_digitos.length} de ${TecladoPin.longitud} dígitos');
    }
  }

  void _borrar() {
    if (_digitos.isEmpty) return;
    setState(_digitos.removeLast);
    anunciar(context, 'Dígito borrado. ${_digitos.length} de ${TecladoPin.longitud} dígitos');
  }

  Future<void> _dictar() async {
    final grabador = widget.grabador!;
    if (_dictando) {
      final wav = await grabador.detener();
      if (!mounted) return;
      setState(() => _dictando = false);
      anunciar(context, 'Dictado terminado. Verificando.');
      widget.alDictar!(wav);
      return;
    }
    if (!await grabador.tienePermiso()) {
      if (mounted) anunciar(context, 'Nayra necesita permiso para usar el micrófono.');
      return;
    }
    await grabador.iniciar();
    if (!mounted) return;
    setState(() {
      _dictando = true;
      _digitos.clear();
    });
    anunciar(context, 'Dicte su PIN dígito por dígito y toque Detener al terminar.');
  }

  @override
  void dispose() {
    if (_dictando) widget.grabador?.cancelar();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final h = widget.habilitado && !_dictando;
    final puedeDictar = widget.grabador != null && widget.alDictar != null;
    final teclas = <Widget>[
      for (final d in ['1', '2', '3', '4', '5', '6', '7', '8', '9']) _Tecla(etiqueta: d, accion: h ? () => _pulsar(d) : null),
      if (puedeDictar)
        _Tecla(
          etiqueta: _dictando ? 'Detener dictado' : 'Dictar PIN',
          visible: _dictando ? 'Detener' : 'Dictar',
          icono: _dictando ? Icons.stop : Icons.mic,
          clara: true,
          accion: widget.habilitado ? _dictar : null,
        )
      else
        const SizedBox.shrink(),
      _Tecla(etiqueta: '0', accion: h ? () => _pulsar('0') : null),
      _Tecla(etiqueta: 'Borrar último dígito', visible: 'Borrar', icono: Icons.backspace_outlined, clara: true, accion: h ? _borrar : null),
    ];
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Semantics(
          label: _dictando ? 'Dictando el PIN' : '${_digitos.length} de ${TecladoPin.longitud} dígitos ingresados',
          excludeSemantics: true,
          child: Column(children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                for (var i = 0; i < TecladoPin.longitud; i++)
                  Container(
                    margin: const EdgeInsets.all(7),
                    width: 34,
                    height: 34,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: i < _digitos.length ? ColoresNayra.foco : Colors.transparent,
                      border: Border.all(
                          color: i < _digitos.length ? ColoresNayra.foco : ColoresNayra.blanco, width: 4),
                    ),
                  ),
              ],
            ),
            Text(_dictando ? 'Dictando…' : '${_digitos.length} de ${TecladoPin.longitud} dígitos',
                style: const TextStyle(fontSize: 25, fontWeight: FontWeight.w700, color: ColoresNayra.foco)),
          ]),
        ),
        const SizedBox(height: 8),
        _rejilla(teclas),
      ],
    );
  }
}

/// Teclado del monto de una transferencia (C6): dígitos, coma decimal y borrar. Anuncia el importe completo en
/// cada pulsación porque el monto no es secreto (HU-61).
class TecladoMonto extends StatelessWidget {
  const TecladoMonto({super.key, required this.texto, required this.alCambiar});
  final String texto;
  final ValueChanged<String> alCambiar;

  static const maximoDecimales = 2;

  void _pulsar(String c) {
    final coma = texto.indexOf(',');
    if (c == ',' && (coma >= 0 || texto.isEmpty)) return;
    if (coma >= 0 && texto.length - coma > maximoDecimales) return;
    if (c != ',' && coma < 0 && texto.replaceAll(',', '').length >= 3) return;
    alCambiar(texto == '0' && c != ',' ? c : texto + c);
  }

  @override
  Widget build(BuildContext context) {
    final teclas = <Widget>[
      for (final d in ['1', '2', '3', '4', '5', '6', '7', '8', '9']) _Tecla(etiqueta: d, accion: () => _pulsar(d)),
      _Tecla(etiqueta: 'Coma decimal', visible: ',', accion: () => _pulsar(',')),
      _Tecla(etiqueta: '0', accion: () => _pulsar('0')),
      _Tecla(
        etiqueta: 'Borrar',
        icono: Icons.backspace_outlined,
        clara: true,
        accion: texto.isEmpty ? null : () => alCambiar(texto.substring(0, texto.length - 1)),
      ),
    ];
    return _rejilla(teclas);
  }
}

/// Teclado del celular del destinatario (G-1): 10 dígitos y borrar, hasta 9 dígitos. El número no es secreto, así
/// que la pantalla lo anuncia completo en cada pulsación. El dictado del número queda pendiente (D-046).
class TecladoCelular extends StatelessWidget {
  const TecladoCelular({super.key, required this.texto, required this.alCambiar, this.maximo = 9});
  final String texto;
  final ValueChanged<String> alCambiar;
  final int maximo;

  void _pulsar(String d) {
    if (texto.length < maximo) alCambiar(texto + d);
  }

  @override
  Widget build(BuildContext context) {
    final teclas = <Widget>[
      for (final d in ['1', '2', '3', '4', '5', '6', '7', '8', '9']) _Tecla(etiqueta: d, accion: () => _pulsar(d)),
      const SizedBox.shrink(),
      _Tecla(etiqueta: '0', accion: () => _pulsar('0')),
      _Tecla(
        etiqueta: 'Borrar último dígito',
        visible: 'Borrar',
        icono: Icons.backspace_outlined,
        clara: true,
        accion: texto.isEmpty ? null : () => alCambiar(texto.substring(0, texto.length - 1)),
      ),
    ];
    return _rejilla(teclas);
  }
}
