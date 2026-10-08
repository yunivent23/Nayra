package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.dtos.DestinatarioDTOs.DestinatarioEncontrado;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.DestinatariosDisponibles;

import java.util.List;

/** Operaciones simuladas (servicio de Negocio, v4 §2.8). Las transferencias todavía no están implementadas. */
public interface IOperacionesService {

    /**
     * Localiza al destinatario de una transferencia por su celular registrado en Nayra (G-1, HU-69).
     * 400 CELULAR_INVALIDO; 409 CUENTAS_IGUALES si es el propio número; 404 DESTINATARIO_NO_ENCONTRADO si no hay una
     * cuenta Nayra activa asociada.
     */
    DestinatarioEncontrado buscarDestinatario(String usuarioId, String celular);

    /** Máximo de celulares por consulta múltiple (decisión de Yuni, 2026-10-08, D-042). */
    int MAX_CELULARES_POR_CONSULTA = 500;

    /**
     * Cuáles de los celulares de la agenda del teléfono pertenecen a destinatarios válidos (D-042, HU-69): mismas
     * condiciones que {@link #buscarDestinatario}, sin el propio usuario y sin repetidos. Devuelve solo las
     * coincidencias. 400 CELULARES_REQUERIDOS si no hay lista; 400 DEMASIADOS_CELULARES si los elementos recibidos
     * superan {@link #MAX_CELULARES_POR_CONSULTA} (antes de filtrar). Los elementos que no son un celular utilizable
     * (formato inválido, fijos, extranjeros) se descartan sin error; si no queda ninguno, o la lista está vacía, se
     * devuelve una lista vacía sin consultar. Los números no se guardan ni se registran.
     */
    DestinatariosDisponibles buscarDestinatarios(String usuarioId, List<String> celulares);
}
