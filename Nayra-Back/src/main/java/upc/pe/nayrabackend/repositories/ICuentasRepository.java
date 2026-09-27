package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Cuentas;

import java.util.Optional;

/** Puerto de persistencia de CUENTAS (cuenta financiera simulada). Adaptador: PostgreSQL (D-051). */
public interface ICuentasRepository {
    void guardar(Cuentas cuenta);
    /** Cuenta cuyo titular es ese registro de identidad simulado (FK única, 03 §16.4). */
    Optional<Cuentas> porTitular(String titularId);
    Optional<Cuentas> porPropietario(String usuarioId);
}
