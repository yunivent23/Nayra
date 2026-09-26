package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Users;

import java.util.Optional;
import java.util.UUID;

public interface IDispositivoService {

    String ACTIVO = "ACTIVO";
    String REVOCADO = "REVOCADO";

    /**
     * Registra la clave pública del nuevo dispositivo tras verificar la prueba de posesión.
     * Revoca el dispositivo anterior y cierra sus sesiones (D-041: un dispositivo activo por usuario).
     */
    Dispositivos registrar(Users usuario, String clavePublica, String plataforma, String nombre,
                           UUID desafioId, String firma);

    Optional<Dispositivos> buscarActivo(UUID idPublico);

    void revocar(Users usuario);
}
