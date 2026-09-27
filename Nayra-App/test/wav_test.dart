import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:nayra_app/servicios/wav.dart';

void main() {
  test('cabecera WAV PCM 16 bits, 16 kHz, mono', () {
    final pcm = Uint8List(32000); // 1 s
    final wav = envolverWav(pcm);
    final d = ByteData.sublistView(wav);
    expect(String.fromCharCodes(wav.sublist(0, 4)), 'RIFF');
    expect(String.fromCharCodes(wav.sublist(8, 12)), 'WAVE');
    expect(d.getUint16(20, Endian.little), 1);
    expect(d.getUint16(22, Endian.little), 1);
    expect(d.getUint32(24, Endian.little), 16000);
    expect(d.getUint16(34, Endian.little), 16);
    expect(d.getUint32(40, Endian.little), 32000);
    expect(wav.length, 44 + 32000);
  });
}
