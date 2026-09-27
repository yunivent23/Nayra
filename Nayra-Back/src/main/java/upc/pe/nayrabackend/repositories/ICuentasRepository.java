package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Cuentas;

import java.util.Optional;

/** Puerto de persistencia de CUENTAS (cuenta financiera simulada). Adaptador actual: en memoria (PROVISIONAL, D-051). */
public interface ICuentasRepository {
    void guardar(Cuentas cuenta);
    Optional<Cuentas> porTitularDni(String dni);
    Optional<Cuentas> porPropietario(String usuarioId);
}
