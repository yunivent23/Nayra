package upc.pe.nayrabackend.repositories;

import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upc.pe.nayrabackend.entities.Users;

import java.util.List;

@Repository
public interface IUsersRepository extends JpaRepository<Users, Long> {

    Users findOneByUsername(String username);

    Users findByUsername(String username);

    List<Users> findByUsernameContainingIgnoreCase(String username);

    Users findByDni(String dni);

    Users findByEmail(String email);

    // D-047: incremento atómico del contador de intentos fallidos
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Users u set u.intentosFallidos = u.intentosFallidos + 1 where u.id = :id")
    int incrementarIntentosFallidos(@Param("id") Long id);
}