package upc.pe.nayrabackend.entities;

/**
 * Registro de identidad simulado (REGISTRO_IDENTIDAD_SIMULADO, 03 §16.3; D-035).
 * Datos ficticios del entorno controlado; no es una API real ni representa personas reales.
 * Consultarlo recupera datos, pero no prueba la identidad (D-052).
 */
public record RegistroIdentidadSimulado(String dni, String nombres, String apellidos) {
}
