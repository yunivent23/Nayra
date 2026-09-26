package upc.pe.nayrabackend.serviceinterfaces;

public interface IPinService {

    /** true si el PIN tiene exactamente 6 dígitos (requisito aprobado). */
    boolean formatoValido(String pin);

    String hashear(String pin);

    boolean verificar(String pin, String pinHash);
}
