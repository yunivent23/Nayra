package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.DesafioAutenticacion;

import java.util.UUID;

public interface IAutenticacionService {

    /** Emite el desafío de inicio de sesión para el dispositivo indicado (04_API.md §2.1). */
    DesafioAutenticacion emitirDesafioLogin(UUID dispositivoId);

    /** Autentica con firma del dispositivo → PIN → voz (D-046) y devuelve el token de sesión. */
    String autenticar(UUID desafioId, String firma, String pin, byte[] audio);
}
