import 'package:flutter/material.dart';

import '../../componentes/botones.dart';
import '../../componentes/contenido.dart';
import '../../flujo/flujo_billetera.dart';

/// Estados comunes de una pantalla que depende del backend: cargando, error (con reintento) y vacío.
/// Devuelve null cuando hay datos y la pantalla debe mostrar su contenido.
List<Widget>? estadosConsulta(Consulta<Object?> c, {required String cargando, required String vacio}) =>
    switch (c.estado) {
      EstadoCarga.cargando => [IndicadorCarga(cargando)],
      EstadoCarga.error => [
          MensajeEstado(c.mensaje, tipo: TipoMensaje.error),
          BotonNayra(texto: 'Intentar de nuevo', icono: Icons.replay, alPulsar: c.cargar),
        ],
      EstadoCarga.vacio => [MensajeEstado(vacio)],
      EstadoCarga.listo => null,
    };

String textoVozConsulta(Consulta<Object?> c, {required String cargando, required String vacio, required String listo}) =>
    switch (c.estado) {
      EstadoCarga.cargando => cargando,
      EstadoCarga.error => c.mensaje,
      EstadoCarga.vacio => vacio,
      EstadoCarga.listo => listo,
    };

/// Fecha legible sin depender de paquetes de localización: "27 set 2026 · 18:42".
String fechaCorta(DateTime f) {
  const meses = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'set', 'oct', 'nov', 'dic'];
  final l = f.toLocal();
  String dos(int n) => n.toString().padLeft(2, '0');
  return '${l.day} ${meses[l.month - 1]} ${l.year} · ${dos(l.hour)}:${dos(l.minute)}';
}
