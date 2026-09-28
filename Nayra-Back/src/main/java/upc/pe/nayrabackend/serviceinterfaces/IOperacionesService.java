package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.dtos.DestinatarioDTOs.DestinatarioEncontrado;

/** Operaciones simuladas (servicio de Negocio, v4 §2.8). Las transferencias todavía no están implementadas. */
public interface IOperacionesService {

    /**
     * Localiza al destinatario de una transferencia por su celular registrado en Nayra (G-1, HU-69).
     * 400 CELULAR_INVALIDO; 409 CUENTAS_IGUALES si es el propio número; 404 DESTINATARIO_NO_ENCONTRADO si no hay una
     * cuenta Nayra activa asociada.
     */
    DestinatarioEncontrado buscarDestinatario(String usuarioId, String celular);
}
