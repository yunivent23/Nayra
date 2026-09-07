package upc.pe.nayrabackend.repositories;

import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upc.pe.nayrabackend.entities.Users;

import java.util.List;

@Repository
public interface IUsersRepository extends JpaRepository<Users, Long> {

    Users findOneByUsername(String username);

    Users findByUsername(String username);

    List<Users> findByUsernameContainingIgnoreCase(String username);

    Users findByDni(String dni);

    Users findByEmail(String email);
}