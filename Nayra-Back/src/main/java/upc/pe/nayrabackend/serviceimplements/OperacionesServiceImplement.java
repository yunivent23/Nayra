package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.DestinatarioEncontrado;
import upc.pe.nayrabackend.entities.Celular;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.serviceinterfaces.IOperacionesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Operaciones simuladas (v4 §2.8). Por ahora solo la búsqueda del destinatario por celular (G-1, 2026-09-28): la
 * ejecución de la transferencia (HU-70 a HU-72) todavía no existe.
 *
 * El celular solo localiza la cuenta Nayra asociada; no prueba la titularidad de la línea. El ID interno de la
 * cuenta destino sigue siendo interno y no se devuelve.
 *
 * Enumeración de números: la búsqueda exige sesión y la respuesta no distingue entre un número sin cuenta y una
 * cuenta que no puede recibir (bloqueada, inactiva o sin cuenta financiera activa). Límite de búsquedas y
 * auditoría de búsquedas quedan PENDIENTES de decisión.
 */
@Service
public class OperacionesServiceImplement implements IOperacionesService {

    private static final String NO_ENCONTRADO = "DESTINATARIO_NO_ENCONTRADO";

    /** Partículas que forman parte del primer apellido: "De la Cruz", "Del Río", "De Los Santos". */
    private static final Set<String> PARTICULAS = Set.of("de", "del", "la", "las", "los");

    /** Letras (sin contar espacios) que se muestran del primer apellido cuando se acorta. */
    private static final int LETRAS_VISIBLES = 4;

    private final IUsuariosRepository usuarios;
    private final ICuentasRepository cuentas;

    public OperacionesServiceImplement(IUsuariosRepository usuarios, ICuentasRepository cuentas) {
        this.usuarios = usuarios;
        this.cuentas = cuentas;
    }

    @Override
    public DestinatarioEncontrado buscarDestinatario(String usuarioId, String celular) {
        Celular numero = Celular.leer(celular);
        Usuario destino = usuarios.porCelular(numero.numero()).orElseThrow(() -> NayraException.noEncontrado(NO_ENCONTRADO));
        if (destino.getId().equals(usuarioId)) {
            throw NayraException.conflicto("CUENTAS_IGUALES");
        }
        boolean puedeRecibir = destino.getEstado() == Usuario.Estado.ACTIVO
                && cuentas.porPropietario(destino.getId()).map(Cuentas::getEstado).orElse(null) == Cuentas.Estado.ACTIVA;
        if (!puedeRecibir) {
            throw NayraException.noEncontrado(NO_ENCONTRADO);
        }
        return new DestinatarioEncontrado(nombreVisible(destino.getNombres(), destino.getApellidos()));
    }

    /**
     * Primer nombre y el primer apellido parcial, sin el segundo apellido.
     *
     * El primer apellido incluye sus partículas iniciales (De, Del, La, Las, Los) y la palabra que las sigue. Se
     * muestran sus primeras cuatro letras sin contar espacios; si el corte cae dentro de una partícula, la partícula
     * se muestra completa. Un apellido de cinco letras o menos se muestra entero, porque acortarlo ocultaría una
     * letra como mucho. Ejemplos: "Pérez" → "Pérez", "Salazar" → "Sala...", "De la Cruz" → "De la...",
     * "Del Río" → "Del R...", "De Los Santos" → "De Los...".
     */
    public static String nombreVisible(String nombres, String apellidos) {
        String nombre = primeraPalabra(nombres);
        String apellido = apellidoParcial(palabras(apellidos));
        return apellido.isEmpty() ? nombre : nombre + " " + apellido;
    }

    private static String apellidoParcial(String[] palabras) {
        int fin = 0;
        while (fin < palabras.length - 1 && esParticula(palabras[fin])) {
            fin++;
        }
        List<String> primerApellido = Arrays.asList(palabras).subList(0, Math.min(fin + 1, palabras.length));
        int letras = primerApellido.stream().mapToInt(p -> p.codePointCount(0, p.length())).sum();
        if (letras <= LETRAS_VISIBLES + 1) {
            return String.join(" ", primerApellido);
        }
        List<String> visibles = new ArrayList<>();
        int restantes = LETRAS_VISIBLES;
        for (String palabra : primerApellido) {
            if (restantes <= 0) {
                break;
            }
            int largo = palabra.codePointCount(0, palabra.length());
            if (largo <= restantes || esParticula(palabra)) {
                visibles.add(palabra);
            } else {
                visibles.add(palabra.substring(0, palabra.offsetByCodePoints(0, restantes)));
            }
            restantes -= largo;
        }
        return String.join(" ", visibles) + "...";
    }

    private static boolean esParticula(String palabra) {
        return PARTICULAS.contains(palabra.toLowerCase(Locale.ROOT));
    }

    private static String primeraPalabra(String texto) {
        String[] p = palabras(texto);
        return p.length == 0 ? "" : p[0];
    }

    private static String[] palabras(String texto) {
        String t = texto == null ? "" : texto.strip();
        return t.isEmpty() ? new String[0] : t.split("\\s+");
    }
}
