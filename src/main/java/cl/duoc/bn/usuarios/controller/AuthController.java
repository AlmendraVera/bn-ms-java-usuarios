package cl.duoc.bn.usuarios.controller;

import cl.duoc.bn.usuarios.model.Usuario;
import cl.duoc.bn.usuarios.service.UsuarioService;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Login y permisos: lo consume el API Gateway para autorizar por rol. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(Long id, String nombre, String email, String rol, List<String> permisos) {
    }

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest peticion) {
        Usuario u = usuarioService.login(peticion.email(), peticion.password());
        String permisos = u.getRol().getPermisos();
        List<String> lista = (permisos == null || permisos.isBlank())
                ? List.of()
                : Arrays.stream(permisos.split(",")).map(String::trim).toList();
        return new LoginResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol().getNombre(), lista);
    }
}