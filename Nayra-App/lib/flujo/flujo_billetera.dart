import 'package:flutter/foundation.dart';

import '../api/billetera_api.dart';
import '../modelos/billetera.dart';
import 'mensajes.dart';

enum EstadoCarga { cargando, listo, vacio, error }

/// Consulta genérica con estados de carga, vacío y error (saldo, movimientos, avisos).
/// Un 401 lo maneja el gestor de sesión (la app vuelve al inicio); aquí solo se informa.
class Consulta<T> extends ChangeNotifier {
  Consulta(this._cargar, {this.esVacio});

  final Future<T> Function() _cargar;
  final bool Function(T)? esVacio;

  EstadoCarga estado = EstadoCarga.cargando;
  T? valor;
  String mensaje = '';

  Future<void> cargar() async {
    estado = EstadoCarga.cargando;
    notifyListeners();
    try {
      final v = await _cargar();
      valor = v;
      estado = (esVacio?.call(v) ?? false) ? EstadoCarga.vacio : EstadoCarga.listo;
      mensaje = '';
    } catch (e) {
      valor = null;
      estado = EstadoCarga.error;
      mensaje = mensajeFallo(e);
    }
    notifyListeners();
  }
}

/// Lista que se recorre de uno en uno (mockups C3 y D2: «Anterior» y «Siguiente»).
class ListaUnoEnUno<T> extends ChangeNotifier {
  ListaUnoEnUno(this.elementos);
  final List<T> elementos;
  int indice = 0;

  T get actual => elementos[indice];
  bool get hayAnterior => indice > 0;
  bool get haySiguiente => indice < elementos.length - 1;

  void anterior() {
    if (hayAnterior) {
      indice--;
      notifyListeners();
    }
  }

  void siguiente() {
    if (haySiguiente) {
      indice++;
      notifyListeners();
    }
  }
}

Consulta<Saldo> consultaSaldo(BilleteraApi api) => Consulta(api.saldo);

Consulta<List<Movimiento>> consultaMovimientos(BilleteraApi api) =>
    Consulta(api.movimientos, esVacio: (l) => l.isEmpty);

Consulta<List<Aviso>> consultaAvisos(BilleteraApi api) => Consulta(api.avisos, esVacio: (l) => l.isEmpty);
