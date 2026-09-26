package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Sesiones;
import upc.pe.nayrabackend.entities.Users;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ISesionesService {

    String LOGOUT = "LOGOUT";
    String INACTIVIDAD = "INACTIVIDAD";
    String DURACION_MAXIMA = "DURACION_MAXIMA";
    String DISPOSITIVO_REVOCADO = "DISPOSITIVO_REVOCADO";

    /** Crea la sesión y devuelve el token opaco en claro; solo su hash queda guardado (D-042). */
    String crear(Users usuario, Dispositivos dispositivo);

    /** Valida el token: vigente, sin 5 min de inactividad ni duración máxima superada. Renueva el último acceso. */
    Optional<Sesiones> validar(String token);

    void cerrar(Long sesionId, String motivo);

    List<Sesiones> listarAbiertas(Long usuarioId);

    boolean cerrarPropia(UUID idPublico, Long usuarioId);
}
