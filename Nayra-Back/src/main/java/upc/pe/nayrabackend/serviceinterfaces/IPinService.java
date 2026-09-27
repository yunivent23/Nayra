package upc.pe.nayrabackend.serviceinterfaces;

/** PIN de 6 dígitos como credencial de conocimiento (D-061, modifica D-037). No es una muestra biométrica. */
public interface IPinService {

    boolean formatoValido(String pin);

    String hashear(String pin);

    boolean coincide(String pin, String hash);
}
