import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nayra_app/componentes/grabacion.dart';

import 'falsos.dart';

/// Micrófono ocupado o error del sistema al empezar a grabar.
class GrabadorQueFalla extends GrabadorFalso {
  int cancelaciones = 0;

  @override
  Future<void> iniciar() async => throw Exception('micrófono ocupado');

  @override
  Future<void> cancelar() async => cancelaciones++;
}

void main() {
  testWidgets('si el micrófono falla, la app no se cierra y el botón sigue disponible', (tester) async {
    final grabador = GrabadorQueFalla();
    Uint8List? enviado;
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(body: BotonGrabacion(grabador: grabador, alTerminar: (wav) => enviado = wav)),
    ));

    await tester.tap(find.text('Toque para grabar'));
    await tester.pumpAndSettle();

    expect(tester.takeException(), isNull);
    expect(grabador.cancelaciones, 1);
    expect(enviado, isNull);
    expect(find.text('Toque para grabar'), findsOneWidget);
  });
}
