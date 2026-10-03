import 'package:flutter/semantics.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import '../app/dependencias.dart';

/// Nayra dice [mensaje] con su propia voz (D-075), sin depender de TalkBack. Fuera de la app (pruebas de
/// componentes sueltos) se anuncia al lector de pantalla. Nunca debe recibir el PIN ni datos biométricos.
void anunciar(BuildContext context, String mensaje) {
  if (mensaje.isEmpty) return;
  final voz = ProveedorNayra.quizas(context)?.voz;
  if (voz != null) {
    voz.decir(mensaje);
  } else {
    SemanticsService.sendAnnouncement(View.of(context), mensaje, TextDirection.ltr);
  }
}

/// Semidúplex (D-075): antes de abrir el micrófono, Nayra deja de hablar y dice [aviso] completo; recién entonces se
/// puede grabar, para que su propia voz no quede en la captura.
Future<void> prepararEscucha(BuildContext context, {String aviso = 'Te escucho.'}) async {
  final voz = ProveedorNayra.quizas(context)?.voz;
  if (voz == null) return;
  await voz.callar();
  await voz.decir(aviso);
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
