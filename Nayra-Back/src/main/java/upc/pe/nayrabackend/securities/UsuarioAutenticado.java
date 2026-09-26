package upc.pe.nayrabackend.securities;

/** Principal de la sesión autenticada. No contiene el token ni datos sensibles. */
public record UsuarioAutenticado(Long usuarioId, Long sesionId) {
}
