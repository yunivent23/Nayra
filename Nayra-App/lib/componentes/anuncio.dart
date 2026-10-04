import 'package:flutter/semantics.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import '../app/dependencias.dart';
import '../servicios/voz_nayra.dart';

/// Nayra dice [mensaje] con su propia voz (D-075), sin depender de TalkBack. Fuera de la app (pruebas de
/// componentes sueltos) se anuncia al lector de pantalla. Nunca debe recibir el PIN ni datos biométricos.
void anunciar(BuildContext context, String mensaje) {
  if (mensaje.isEmpty) return;
  final voz = ProveedorNayra.quizas(context)?.voz;
  if (voz == null) {
    SemanticsService.sendAnnouncement(View.of(context), mensaje, TextDirection.ltr);
    return;
  }
  final vista = View.of(context);
  voz.decir(mensaje).catchError((Object e, StackTrace pila) {
    // El motor de voz no confirmó que calló: se informa el fallo (Nayra sigue «hablando», así que el micrófono no
    // se abre) y el mensaje va al lector de pantalla para no perderlo.
    FlutterError.reportError(FlutterErrorDetails(
        exception: e, stack: pila, library: 'Nayra', context: ErrorDescription('al decir un mensaje')));
    SemanticsService.sendAnnouncement(vista, mensaje, TextDirection.ltr);
  }, test: (e) => e is FalloVoz);
}

/// Semidúplex (D-075): antes de abrir el micrófono, Nayra deja de hablar, dice [aviso] completo y reserva el
/// micrófono: desde ahí no vuelve a hablar hasta que se libere, para que su propia voz no quede en la captura.
/// Devuelve la voz a liberar después de detener o cancelar la grabación (null fuera de la app). Lanza [FalloVoz]
/// si no se puede confirmar que Nayra calló; entonces no se debe grabar.
Future<VozNayra?> prepararEscucha(BuildContext context, {String aviso = 'Te escucho.'}) async {
  final voz = ProveedorNayra.quizas(context)?.voz;
  if (voz == null) return null;
  await voz.callar();
  await voz.decir(aviso);
  await voz.reservarMicrofono();
  return voz;
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
