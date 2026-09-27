import 'package:flutter/material.dart';

import '../flujo/flujo_registro.dart';
import '../servicios/grabador.dart';
import 'boton_grabacion.dart';
import 'campo_texto.dart';
import 'teclado_pin.dart';
import 'texto_estado.dart';

/// Pasos del registro inicial asistido en el celular de la persona (D-052).
class PantallaRegistro extends StatelessWidget {
  const PantallaRegistro({super.key, required this.flujo, required this.grabador});

  final FlujoRegistro flujo;
  final GrabadorVoz grabador;

  Widget _botonGrande(String texto, VoidCallback accion) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: SizedBox(
          height: 72,
          child: FilledButton(onPressed: accion, child: Text(texto, style: const TextStyle(fontSize: 20))),
        ),
      );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Registro en Nayra')),
      body: SafeArea(
        child: ListenableBuilder(
          listenable: flujo,
          builder: (context, _) => ListView(
            padding: const EdgeInsets.all(16),
            children: [
              TextoEstado(flujo.mensaje),
              if (flujo.paso == PasoRegistro.enviando) const Center(child: CircularProgressIndicator()),
              if (flujo.paso == PasoRegistro.codigo)
                CampoTexto(
                  key: const ValueKey('codigo'),
                  etiqueta: 'Código de registro',
                  textoBoton: 'Continuar',
                  alEnviar: flujo.ingresarCodigo,
                ),
              if (flujo.paso == PasoRegistro.confirmarDatos) ...[
                _botonGrande('Sí, son mis datos', () => flujo.confirmarDatos(true)),
                _botonGrande('No son mis datos', () => flujo.confirmarDatos(false)),
              ],
              if (flujo.paso == PasoRegistro.celular)
                CampoTexto(
                  key: const ValueKey('celular'),
                  etiqueta: 'Número de celular',
                  textoBoton: 'Continuar',
                  teclado: TextInputType.phone,
                  alEnviar: flujo.ingresarCelular,
                ),
              if (flujo.paso == PasoRegistro.pin) TecladoPin(key: const ValueKey('pin'), alCompletar: flujo.ingresarPin),
              if (flujo.paso == PasoRegistro.confirmarPin)
                TecladoPin(key: const ValueKey('confirmar'), alCompletar: flujo.confirmarPin),
              if (flujo.paso == PasoRegistro.muestra && flujo.desafio != null) ...[
                TextoDesafio(flujo.desafio!.texto),
                const SizedBox(height: 16),
                BotonGrabacion(
                  key: ValueKey(flujo.desafio!.id),
                  grabador: grabador,
                  etiqueta: 'Grabar frase',
                  alTerminar: flujo.enviarMuestra,
                ),
              ],
              if (flujo.paso == PasoRegistro.terminado || flujo.paso == PasoRegistro.error)
                _botonGrande('Volver', () => Navigator.of(context).pop()),
            ],
          ),
        ),
      ),
    );
  }
}
