import 'package:flutter/material.dart';

import '../tema/colores.dart';

/// Logo de los mockups: cuadrado amarillo con ondas de voz y el nombre NAYRA. Decorativo para el lector.
class LogoNayra extends StatelessWidget {
  const LogoNayra({super.key, this.tamano = 88, this.conNombre = true});
  final double tamano;
  final bool conNombre;

  @override
  Widget build(BuildContext context) => Semantics(
        label: 'Nayra',
        excludeSemantics: true,
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 12),
          child: Column(mainAxisSize: MainAxisSize.min, children: [
            CustomPaint(size: Size.square(tamano), painter: _Ondas()),
            if (conNombre) ...[
              const SizedBox(height: 8),
              Text('NAYRA',
                  style: TextStyle(fontSize: tamano * 0.45, fontWeight: FontWeight.w700, letterSpacing: tamano * 0.08)),
            ],
          ]),
        ),
      );
}

class _Ondas extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final e = size.width / 48;
    canvas.drawRRect(RRect.fromRectAndRadius(Offset.zero & size, Radius.circular(14 * e)),
        Paint()..color = ColoresNayra.foco);
    final trazo = Paint()
      ..color = ColoresNayra.azulMarino
      ..strokeWidth = 3.6 * e
      ..strokeCap = StrokeCap.round;
    for (final (x, y1, y2) in [(12.0, 22.0, 26.0), (18.0, 16.0, 32.0), (24.0, 11.0, 37.0), (30.0, 17.0, 31.0), (36.0, 21.0, 27.0)]) {
      canvas.drawLine(Offset(x * e, y1 * e), Offset(x * e, y2 * e), trazo);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
