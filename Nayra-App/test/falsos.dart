import 'package:flutter/foundation.dart';

import 'package:nayra_app/servicios/dispositivo.dart';
import 'package:nayra_app/servicios/grabador.dart';
import 'package:nayra_app/servicios/voz_nayra.dart';

class DispositivoFalso implements ClaveDispositivo {
  Map<String, String> ids = {};
  final firmados = <String>[];

  @override
  Future<String> clavePublica() async => 'CLAVE_PUBLICA';

  @override
  Future<String> firmar(String mensaje) async {
    firmados.add(mensaje);
    return 'FIRMA';
  }

  @override
  Future<Map<String, String>> leerIdentificadores() async => ids;

  @override
  Future<void> guardarIdentificadores(String cuentaId, String dispositivoId) async =>
      ids = {'cuentaId': cuentaId, 'dispositivoId': dispositivoId};
}

class GrabadorFalso implements GrabadorVoz {
  bool grabando = false;

  @override
  Future<bool> tienePermiso() async => true;

  @override
  Future<void> iniciar() async => grabando = true;

  @override
  Future<Uint8List> detener() async {
    grabando = false;
    return Uint8List.fromList([1, 2, 3]);
  }

  @override
  Future<void> cancelar() async => grabando = false;
}

/// Voz de Nayra sin audio: registra lo que dice y termina de inmediato.
class VozFalsa implements VozNayra {
  final dichos = <String>[];
  int interrupciones = 0;
  final _hablando = ValueNotifier(false);

  @override
  ValueListenable<bool> get hablando => _hablando;

  @override
  Future<void> decir(String texto) async => dichos.add(texto);

  @override
  Future<void> callar() async => interrupciones++;
}
