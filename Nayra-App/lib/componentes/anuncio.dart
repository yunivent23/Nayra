import 'package:flutter/semantics.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// Anuncia un mensaje al lector de pantalla (TalkBack). Nunca debe recibir el PIN ni datos biométricos.
void anunciar(BuildContext context, String mensaje) {
  if (mensaje.isEmpty) return;
  SemanticsService.sendAnnouncement(View.of(context), mensaje, TextDirection.ltr);
}

/// Señales diferenciadas de éxito, aviso y error (HU-57), sin depender de la vista.
///
/// PROVISIONAL: se usan la vibración y el sonido del sistema porque el primer entregable no define sonidos
/// propios; incorporar audios propios requiere decidir los recursos y una dependencia de reproducción.
abstract final class Senales {
  static Future<void> exito() => HapticFeedback.lightImpact();

  static Future<void> aviso() async {
    await HapticFeedback.mediumImpact();
    await SystemSound.play(SystemSoundType.alert);
  }

  static Future<void> error() async {
    await HapticFeedback.heavyImpact();
    await SystemSound.play(SystemSoundType.alert);
  }
}
