import 'package:flutter/material.dart';

import 'api/cliente_http.dart';
import 'app/dependencias.dart';
import 'app/nayra_app.dart';
import 'config.dart';
import 'servicios/dispositivo.dart';
import 'servicios/escucha_comando.dart';
import 'servicios/grabador.dart';
import 'servicios/voz_nayra.dart';

void main() {
  runApp(NayraApp(
    dependencias: Dependencias(
      http: ClienteHttp(ConfiguracionApp.urlBackend),
      dispositivo: CanalClaveDispositivo(),
      grabador: GrabadorMicrofono(),
      voz: VozTts(),
      comando: EscuchaVosk(),
    ),
  ));
}
