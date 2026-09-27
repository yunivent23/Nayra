import 'dart:convert';

import 'package:flutter/services.dart';

/// Clave del dispositivo vinculado (D-048): par EC P-256 generado y guardado en el
/// Android Keystore, no exportable. Dart solo recibe la clave pública y las firmas.
abstract class ClaveDispositivo {
  /// Crea el par si no existe y devuelve la clave pública X.509 en Base64.
  Future<String> clavePublica();

  /// Firma SHA256withECDSA de [mensaje]; devuelve la firma DER en Base64.
  Future<String> firmar(String mensaje);

  /// Identificadores no secretos del registro local (cuenta y dispositivo).
  Future<Map<String, String>> leerIdentificadores();
  Future<void> guardarIdentificadores(String cuentaId, String dispositivoId);
}

/// Mensaje firmado; debe coincidir con DispositivoServiceImplement.mensajeFirmado del backend.
String mensajeAFirmar(String nonce, String dispositivoId, String proposito) => '$nonce|$dispositivoId|$proposito';

/// Implementación con el canal de plataforma Kotlin (MainActivity.kt).
class CanalClaveDispositivo implements ClaveDispositivo {
  static const _canal = MethodChannel('pe.upc.nayra/dispositivo');

  @override
  Future<String> clavePublica() async => (await _canal.invokeMethod<String>('clavePublica'))!;

  @override
  Future<String> firmar(String mensaje) async =>
      (await _canal.invokeMethod<String>('firmar', {'mensaje': base64Encode(utf8.encode(mensaje))}))!;

  @override
  Future<Map<String, String>> leerIdentificadores() async =>
      Map<String, String>.from(await _canal.invokeMethod<Map<Object?, Object?>>('leerIdentificadores') ?? const {});

  @override
  Future<void> guardarIdentificadores(String cuentaId, String dispositivoId) =>
      _canal.invokeMethod('guardarIdentificadores', {'cuentaId': cuentaId, 'dispositivoId': dispositivoId});
}
