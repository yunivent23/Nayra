import 'package:flutter_contacts/flutter_contacts.dart';

import '../api/destinatario_api.dart';
import '../modelos/billetera.dart';
import '../modelos/celular.dart';

/// Contacto leído de la agenda del teléfono: solo el nombre y sus números. Vive en memoria mientras dura la
/// consulta; la agenda nunca se guarda ni se registra en logs (D-042).
class ContactoAgenda {
  const ContactoAgenda(this.nombre, this.telefonos);
  final String nombre;
  final List<String> telefonos;
}

/// La persona no dio (o retiró) el permiso de contactos. La búsqueda manual sigue disponible.
class AgendaSinPermiso implements Exception {
  const AgendaSinPermiso();
}

/// Agenda del teléfono como fuente de números para transferir (D-042, 2026-10-08). [leer] pide el permiso de
/// contactos solo cuando se llama, y lanza [AgendaSinPermiso] si la persona no lo da.
abstract class AgendaContactos {
  Future<List<ContactoAgenda>> leer();
}

/// Agenda real con flutter_contacts: pide READ_CONTACTS y lee solo nombre y teléfonos.
class AgendaDispositivo implements AgendaContactos {
  @override
  Future<List<ContactoAgenda>> leer() async {
    final permiso = await FlutterContacts.permissions.request(PermissionType.read);
    if (permiso != PermissionStatus.granted && permiso != PermissionStatus.limited) throw const AgendaSinPermiso();
    final contactos = await FlutterContacts.getAll(properties: {ContactProperty.name, ContactProperty.phone});
    return [
      for (final c in contactos)
        ContactoAgenda((c.displayName ?? '').trim(), [for (final t in c.phones) t.number]),
    ];
  }
}

/// Contacto de la agenda cuyo número confirmó Nayra-Back: se muestra con su nombre de la agenda y el nombre parcial
/// que devuelve Nayra.
class ContactoDisponible {
  const ContactoDisponible(this.nombreAgenda, this.destinatario);
  final String nombreAgenda;
  final Destinatario destinatario;
}

/// Cruce de la agenda con la respuesta de `POST /api/v1/destinatarios/busqueda-multiple`.
class CruceAgenda {
  CruceAgenda._();

  /// Celular canónico de un número de la agenda, o null si no se puede usar. Además de lo que ignora
  /// [Celular.normalizar], quita paréntesis y puntos, comunes en la agenda.
  static String? celular(String numero) => Celular.normalizar(numero.replaceAll(RegExp(r'[().]'), ''));

  /// Celulares que se consultan: primero se normalizan, luego se quitan los repetidos conservando el orden de la
  /// agenda y recién después se toman como máximo los primeros [DestinatarioApi.maxPorConsulta] (500 en este MVP,
  /// en una sola consulta). Los números que quedan fuera no se guardan. [recortada] indica si quedaron fuera.
  static ({List<String> celulares, bool recortada}) celulares(List<ContactoAgenda> agenda) {
    final todos = {
      for (final c in agenda)
        for (final t in c.telefonos) ?celular(t),
    };
    return (
      celulares: todos.take(DestinatarioApi.maxPorConsulta).toList(),
      recortada: todos.length > DestinatarioApi.maxPorConsulta,
    );
  }

  /// Solo los contactos con un número confirmado por el backend, en el orden de la agenda. Un contacto con varios
  /// números registrados aparece una vez, con el primero; un número ya mostrado no se repite en otro contacto.
  static List<ContactoDisponible> cruzar(List<ContactoAgenda> agenda, List<Destinatario> registrados) {
    final porCelular = {for (final d in registrados) d.celular: d};
    final usados = <String>{};
    final disponibles = <ContactoDisponible>[];
    for (final c in agenda) {
      for (final t in c.telefonos) {
        final n = celular(t);
        final d = porCelular[n];
        if (d == null || !usados.add(d.celular)) continue;
        disponibles.add(ContactoDisponible(c.nombre.isEmpty ? d.nombreVisible : c.nombre, d));
        break;
      }
    }
    return disponibles;
  }
}
