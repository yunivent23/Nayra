package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import upc.pe.nayrabackend.entities.Users;
import upc.pe.nayrabackend.repositories.IUsersRepository;
import upc.pe.nayrabackend.serviceinterfaces.IUsuarioService;

import java.util.List;

@Service
public class UsuarioServiceImplement implements IUsuarioService {

    @Autowired
    private IUsersRepository repository;

    @Override
    public List<Users> listarTodo() {
        return repository.findAll();
    }

    @Override
    public Users listId(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void insert(Users usuario) {
        repository.save(usuario);
    }

    @Override
    public void delete(Long id) {

        Users usuario = repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Usuario no encontrado"));

        repository.delete(usuario);
    }

    @Override
    public void edit(Users usuario) {
        repository.save(usuario);
    }

    @Override
    public List<Users> buscarPorNombre(String username) {
        return repository.findByUsernameContainingIgnoreCase(username);
    }

    @Override
    public Users buscarPorUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public Users buscarPorDni(String dni) {
        return repository.findByDni(dni);
    }

    @Override
    public Users buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }
}