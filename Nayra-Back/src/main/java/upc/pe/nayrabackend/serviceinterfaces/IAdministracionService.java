package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.dtos.AdministracionDTOs.DispositivoResumen;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.EventoAuditoriaDTO;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.UsuarioDetalle;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.UsuarioResumen;

import java.util.List;
import java.util.Optional;

/**
 * Funciones del administrador (D-041). No modifica saldos ni operaciones; cada acción se audita
 * con el administrador como actor. No expone el hash del PIN ni datos biométricos.
 */
public interface IAdministracionService {

    /** HU-20 (listar) y HU-21 (buscar por DNI o por texto en nombres y apellidos). */
    List<UsuarioResumen> buscarUsuarios(String adminId, String dni, String texto);

    /** HU-22 (información) y HU-101 (estado). */
    UsuarioDetalle detalleUsuario(String adminId, String usuarioId);

    /** HU-19. */
    void bloquearUsuario(String adminId, String usuarioId);

    /** HU-19. */
    void desbloquearUsuario(String adminId, String usuarioId);

    /** HU-125: dispositivo activo del usuario. */
    Optional<DispositivoResumen> dispositivoActivo(String adminId, String usuarioId);

    /** HU-125: revoca el dispositivo activo y las sesiones del usuario (D-040). */
    void revocarDispositivo(String adminId, String usuarioId);

    /** HU-89: registros de auditoría, opcionalmente de un usuario. */
    List<EventoAuditoriaDTO> auditoria(String adminId, String usuarioId);

    /** HU-80: intentos fallidos de autenticación. */
    List<EventoAuditoriaDTO> intentosFallidos(String adminId, String usuarioId);

    /** HU-94: acciones administrativas. */
    List<EventoAuditoriaDTO> accionesAdministrativas(String adminId);
}
