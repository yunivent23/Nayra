package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.DesafioAutenticacion;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Users;

import java.util.List;
import java.util.UUID;

public interface IDesafioService {

    String LOGIN = "LOGIN";
    String REGISTRO_DISPOSITIVO = "REGISTRO_DISPOSITIVO";
    String REAUTENTICACION = "REAUTENTICACION";

    /** D-037: palabra + 3 dígitos + palabra, generado con SecureRandom. */
    List<String> generarElementos();

    /** Emite y persiste un desafío/nonce de un solo uso (D-048). */
    DesafioAutenticacion emitir(String proposito, Users usuario, Dispositivos dispositivo, boolean conVoz);

    /** Consume el desafío de forma atómica; lanza DESAFIO_INVALIDO si no existe, se usó o expiró. */
    DesafioAutenticacion consumir(UUID idPublico, String proposito);
}
