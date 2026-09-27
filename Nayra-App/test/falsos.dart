import 'dart:typed_data';

import 'package:nayra_app/servicios/dispositivo.dart';
import 'package:nayra_app/servicios/grabador.dart';

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
