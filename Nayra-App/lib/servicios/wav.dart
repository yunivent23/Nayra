import 'dart:typed_data';

/// Envuelve PCM 16 bits little-endian en un contenedor WAV (RIFF) en memoria.
/// El audio nunca se escribe en disco ni se conserva después del envío (D-013).
Uint8List envolverWav(Uint8List pcm16, {int frecuencia = 16000, int canales = 1}) {
  const bits = 16;
  final bytesPorSegundo = frecuencia * canales * bits ~/ 8;
  final cabecera = ByteData(44);
  void texto(int desde, String s) {
    for (var i = 0; i < s.length; i++) {
      cabecera.setUint8(desde + i, s.codeUnitAt(i));
    }
  }

  texto(0, 'RIFF');
  cabecera.setUint32(4, 36 + pcm16.length, Endian.little);
  texto(8, 'WAVE');
  texto(12, 'fmt ');
  cabecera.setUint32(16, 16, Endian.little);
  cabecera.setUint16(20, 1, Endian.little); // PCM
  cabecera.setUint16(22, canales, Endian.little);
  cabecera.setUint32(24, frecuencia, Endian.little);
  cabecera.setUint32(28, bytesPorSegundo, Endian.little);
  cabecera.setUint16(32, canales * bits ~/ 8, Endian.little);
  cabecera.setUint16(34, bits, Endian.little);
  texto(36, 'data');
  cabecera.setUint32(40, pcm16.length, Endian.little);
  return Uint8List.fromList([...cabecera.buffer.asUint8List(), ...pcm16]);
}
