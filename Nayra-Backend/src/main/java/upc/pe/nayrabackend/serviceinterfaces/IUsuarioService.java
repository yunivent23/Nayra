package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.Users;

import java.util.List;

public interface IUsuarioService {

    List<Users> listarTodo();

    Users listId(Long id);

    void insert(Users usuario);

    void delete(Long id);

    void edit(Users usuario);

    List<Users> buscarPorNombre(String username);

    Users buscarPorUsername(String username);

    Users buscarPorDni(String dni);

    Users buscarPorEmail(String email);
}