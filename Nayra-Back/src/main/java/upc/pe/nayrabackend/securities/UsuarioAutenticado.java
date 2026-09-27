package upc.pe.nayrabackend.securities;

import upc.pe.nayrabackend.entities.Rol;

/** Identidad de la petición autenticada, resuelta a partir de la sesión (PROVISIONAL, D-018). */
public record UsuarioAutenticado(String usuarioId, String sesionId, String dispositivoId, Rol rol) {
}
