package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;

import java.util.Optional;

/** REGISTRO_IDENTIDAD_SIMULADO (D-035). Adaptador actual: en memoria (PROVISIONAL, D-051). */
public interface IRegistroIdentidadRepository {
    void guardar(RegistroIdentidadSimulado registro);
    Optional<RegistroIdentidadSimulado> porDni(String dni);
}
