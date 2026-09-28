/// Celular de Perú en el formato canónico que usa Nayra-Back (G-1, 2026-09-28; V012): 9 dígitos que empiezan por
/// 9, sin el prefijo +51. Se normaliza igual que en el backend para no enviar variantes del mismo número.
class Celular {
  Celular._();

  static const longitud = 9;
  static final _formato = RegExp(r'^9\d{8}$');

  /// Número canónico, o null si no es un celular de Perú. Ignora espacios y guiones y quita "+51".
  static String? normalizar(String texto) {
    var n = texto.replaceAll(RegExp(r'[\s-]'), '');
    if (n.startsWith('+51')) n = n.substring(3);
    return _formato.hasMatch(n) ? n : null;
  }

  /// "987654321" → "987 654 321" (lectura en pantalla).
  static String agrupado(String digitos) {
    final b = StringBuffer();
    for (var i = 0; i < digitos.length; i++) {
      if (i > 0 && i % 3 == 0) b.write(' ');
      b.write(digitos[i]);
    }
    return b.toString();
  }

  /// "987654321" → "9 8 7, 6 5 4, 3 2 1" (lectura dígito por dígito para el lector de pantalla).
  static String hablado(String digitos) =>
      agrupado(digitos).split(' ').map((g) => g.split('').join(' ')).join(', ');
}
