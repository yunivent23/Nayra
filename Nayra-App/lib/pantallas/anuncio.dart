import 'package:flutter/semantics.dart';
import 'package:flutter/widgets.dart';

/// Anuncia un mensaje al lector de pantalla (TalkBack).
void anunciar(BuildContext context, String mensaje) {
  if (mensaje.isEmpty) return;
  SemanticsService.sendAnnouncement(View.of(context), mensaje, TextDirection.ltr);
}
