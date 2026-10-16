package cl.duoc.bn.usuarios.service;

import cl.duoc.bn.usuarios.exception.ApiException;
import cl.duoc.bn.usuarios.model.Rol;
import cl.duoc.bn.usuarios.model.Usuario;
import cl.duoc.bn.usuarios.repository.RolRepository;
import cl.duoc.bn.usuarios.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private static final String PATRON_EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordHasher hasher;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordHasher hasher) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.hasher = hasher;
    }

    public List<Usuario> listar(String nombreRol) {
        if (nombreRol != null && !nombreRol.isBlank()) {
            return usuarioRepository.findByRolNombre(nombreRol.trim().toUpperCase());
        }
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe un usuario con id " + id));
    }

    public Usuario crear(Usuario usuario) {
        validar(usuario, true);
        String email = usuario.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw ApiException.conflicto("Ya existe un usuario con el email " + email);
        }
        usuario.setId(null);
        usuario.setEmail(email);
        usuario.setRol(resolverRol(usuario.getRol()));
        usuario.setPassword(hasher.hash(usuario.getPassword()));
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datos) {
        validar(datos, false);
        Usuario usuario = buscarPorId(id);
        String email = datos.getEmail().trim().toLowerCase();
        usuarioRepository.findByEmail(email).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw ApiException.conflicto("Ya existe un usuario con el email " + email);
            }
        });
        usuario.setNombre(datos.getNombre().trim());
        usuario.setEmail(email);
        usuario.setRol(resolverRol(datos.getRol()));
        if (datos.getActivo() != null) {
            usuario.setActivo(datos.getActivo());
        }
        // La contraseña es opcional al actualizar: solo se cambia si viene en el JSON
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            usuario.setPassword(hasher.hash(datos.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.delete(buscarPorId(id));
    }

    /** Valida credenciales. Mismo mensaje para email inexistente y clave incorrecta (no revela cuál falló). */
    public Usuario login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw ApiException.solicitudInvalida("Email y password son obligatorios");
        }
        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> ApiException.noAutorizado("Credenciales inválidas"));
        if (!hasher.verificar(password, usuario.getPassword())) {
            throw ApiException.noAutorizado("Credenciales inválidas");
        }
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw ApiException.prohibido("El usuario está desactivado");
        }
        return usuario;
    }

    private Rol resolverRol(Rol rol) {
        if (rol == null || rol.getId() == null) {
            throw ApiException.solicitudInvalida("Debe indicar el rol del usuario: \"rol\": {\"id\": 1}");
        }
        return rolRepository.findById(rol.getId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe un rol con id " + rol.getId()));
    }

    private void validar(Usuario u, boolean passwordObligatoria) {
        if (u.getNombre() == null || u.getNombre().isBlank()) {
            throw ApiException.solicitudInvalida("El nombre es obligatorio");
        }
        if (u.getEmail() == null || !u.getEmail().trim().matches(PATRON_EMAIL)) {
            throw ApiException.solicitudInvalida("El email es obligatorio y debe tener un formato válido");
        }
        if (passwordObligatoria && (u.getPassword() == null || u.getPassword().length() < 6)) {
            throw ApiException.solicitudInvalida("La password es obligatoria y debe tener al menos 6 caracteres");
        }
    }
}