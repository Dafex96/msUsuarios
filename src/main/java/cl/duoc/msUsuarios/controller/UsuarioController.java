package cl.duoc.msUsuarios.controller;

import cl.duoc.msUsuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.msUsuarios.dto.AvatarRequest;
import cl.duoc.msUsuarios.dto.CambiarContrasenaRequest;
import cl.duoc.msUsuarios.dto.LoginRequest;
import cl.duoc.msUsuarios.dto.MensajeResponse;
import cl.duoc.msUsuarios.dto.RegistroUsuarioRequest;
import cl.duoc.msUsuarios.dto.UsuarioResponse;
import cl.duoc.msUsuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Registro, login, perfil y contraseña")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Registrar un usuario nuevo")
    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest req) {
        UsuarioResponse creado = usuarioService.registrar(req);
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.id())).body(creado);
    }

    @Operation(summary = "Iniciar sesión con correo o RUT")
    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest req) {
        return usuarioService.login(req);
    }

    @Operation(summary = "Listar usuarios ordenados por nombre")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @Operation(summary = "Buscar por nombre, correo o RUT")
    @GetMapping("/buscar")
    public List<UsuarioResponse> buscar(@RequestParam String texto) {
        return usuarioService.buscar(texto);
    }

    @Operation(summary = "Listar usuarios de un rol")
    @GetMapping("/rol/{rolId}")
    public List<UsuarioResponse> listarPorRol(@PathVariable Long rolId) {
        return usuarioService.listarPorRol(rolId);
    }

    @Operation(summary = "Obtener un usuario por id")
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @Operation(summary = "Editar el perfil")
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest req) {
        return usuarioService.actualizar(id, req);
    }

    @Operation(summary = "Actualizar la foto de perfil (Base64)")
    @PutMapping("/{id}/avatar")
    public UsuarioResponse actualizarAvatar(@PathVariable Long id, @Valid @RequestBody AvatarRequest req) {
        return usuarioService.actualizarAvatar(id, req.avatar());
    }

    @Operation(summary = "Cambiar la contraseña conociendo la actual")
    @PutMapping("/{id}/contrasena")
    public MensajeResponse cambiarContrasena(@PathVariable Long id, @Valid @RequestBody CambiarContrasenaRequest req) {
        usuarioService.cambiarContrasena(id, req);
        return new MensajeResponse("Contraseña actualizada correctamente");
    }

    @Operation(summary = "Eliminar un usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
