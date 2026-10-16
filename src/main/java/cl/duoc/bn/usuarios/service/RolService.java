package cl.duoc.bn.usuarios.service;

import cl.duoc.bn.usuarios.exception.ApiException;
import cl.duoc.bn.usuarios.model.Rol;
import cl.duoc.bn.usuarios.repository.RolRepository;
import cl.duoc.bn.usuarios.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    public RolService(RolRepository rolRepository, UsuarioRepository usuarioRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    public Rol buscarPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe un rol con id " + id));
    }

    public Rol crear(Rol rol) {
        validar(rol);
        String nombre = rol.getNombre().trim().toUpperCase();
        if (rolRepository.existsByNombre(nombre)) {
            throw ApiException.conflicto("Ya existe un rol con el nombre " + nombre);
        }
        rol.setId(null);
        rol.setNombre(nombre);
        return rolRepository.save(rol);
    }

    public Rol actualizar(Long id, Rol datos) {
        validar(datos);
        Rol rol = buscarPorId(id);
        String nombre = datos.getNombre().trim().toUpperCase();
        rolRepository.findByNombre(nombre).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw ApiException.conflicto("Ya existe un rol con el nombre " + nombre);
            }
        });
        rol.setNombre(nombre);
        rol.setPermisos(datos.getPermisos());
        return rolRepository.save(rol);
    }

    public void eliminar(Long id) {
        Rol rol = buscarPorId(id);
        if (usuarioRepository.countByRolId(id) > 0) {
            throw ApiException.conflicto("No se puede eliminar el rol " + rol.getNombre()
                    + " porque tiene usuarios asociados");
        }
        rolRepository.delete(rol);
    }

    private void validar(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().isBlank()) {
            throw ApiException.solicitudInvalida("El nombre del rol es obligatorio");
        }
    }
}