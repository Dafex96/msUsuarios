package cl.duoc.msUsuarios.config;

import cl.duoc.msUsuarios.model.Rol;
import cl.duoc.msUsuarios.model.Usuario;
import cl.duoc.msUsuarios.repository.RolRepository;
import cl.duoc.msUsuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "usuarios.datos-prueba", name = "habilitado", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        cargarRol(1L, "CLIENTE");
        cargarRol(2L, "AGENTE");
        cargarRol(3L, "EJECUTIVO");

        cargarUsuario("20.435.214-5", "Sofia Perez", "sofia@gmail.com", "cli123", 1L);
        cargarUsuario("10.625.244-K", "Matias Sanchez", "matias@pagoflex.com", "age123", 2L);
    }

    private void cargarRol(Long id, String nombre) {
        if (!rolRepository.existsById(id)) {
            rolRepository.save(new Rol(id, nombre));
            log.info("Rol de prueba creado: {}", nombre);
        }
    }

    private void cargarUsuario(String rut, String nombre, String correo, String clave, Long rolId) {
        if (usuarioRepository.existsByCorreo(correo) || usuarioRepository.existsByRut(rut)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .rut(rut)
                .nombre(nombre)
                .correo(correo)
                .contrasena(passwordEncoder.encode(clave))
                .rolId(rolId)
                .build());
        log.info("Usuario de prueba creado: {}", correo);
    }
}
