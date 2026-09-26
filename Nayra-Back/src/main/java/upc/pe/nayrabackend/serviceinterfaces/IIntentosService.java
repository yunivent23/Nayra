package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.Users;

public interface IIntentosService {

    /** Registra un intento fallido; bloquea la cuenta al alcanzar el máximo (D-047). Devuelve los intentos restantes. */
    int registrarFallo(Users usuario);

    void registrarExito(Users usuario);
}
