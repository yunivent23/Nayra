import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nayra_app/componentes/teclado.dart';

void main() {
  testWidgets('el teclado entrega el PIN al sexto dígito y solo expone el progreso', (tester) async {
    final semantica = tester.ensureSemantics();
    // Pantalla de teléfono en vertical (412 × 915 dp).
    tester.view.physicalSize = const Size(1236, 2745);
    tester.view.devicePixelRatio = 3;
    addTearDown(tester.view.reset);
    String? entregado;
    await tester.pumpWidget(MaterialApp(home: Scaffold(body: TecladoPin(alCompletar: (p) => entregado = p))));

    for (final d in ['4', '8', '2']) {
      await tester.tap(find.bySemanticsLabel(d));
      await tester.pump();
    }
    expect(find.bySemanticsLabel('3 de 6 dígitos ingresados'), findsOneWidget);
    // Ningún nodo accesible revela los dígitos ya pulsados.
    expect(find.bySemanticsLabel(RegExp('482')), findsNothing);

    await tester.tap(find.bySemanticsLabel('Borrar último dígito'));
    await tester.pump();
    expect(find.bySemanticsLabel('2 de 6 dígitos ingresados'), findsOneWidget);

    for (final d in ['2', '9', '1', '3']) {
      await tester.tap(find.bySemanticsLabel(d));
      await tester.pump();
    }
    expect(entregado, '482913');
    expect(find.bySemanticsLabel('0 de 6 dígitos ingresados'), findsOneWidget);
    semantica.dispose();
  });
}
