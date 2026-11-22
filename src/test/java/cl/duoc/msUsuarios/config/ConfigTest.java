package cl.duoc.msUsuarios.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.msUsuarios.model.Rol;
import cl.duoc.msUsuarios.model.Usuario;
import cl.duoc.msUsuarios.repository.RolRepository;
import cl.duoc.msUsuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class ConfigTest {

    @Test
    void swaggerConfigDefineElTituloDeLaDocumentacion() {
        assertEquals("PagoFlex - Microservicio de Usuarios", new SwaggerConfig().openApi().getInfo().getTitle());
    }

    @Test
    void passwordEncoderEsBcrypt() {
        PasswordEncoder encoder = new SecurityBeansConfig().passwordEncoder();
        assertTrue(encoder.matches("abc", encoder.encode("abc")));
    }

    @Test
    void dataInitializerCreaRolesYUsuariosSiNoExisten() {
        RolRepository roles = mock(RolRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = new SecurityBeansConfig().passwordEncoder();

        new DataInitializer(roles, usuarios, encoder).run(null);

        verify(roles, times(3)).save(any(Rol.class));
        verify(usuarios, times(2)).save(any(Usuario.class));
    }

    @Test
    void dataInitializerNoDuplicaDatosExistentes() {
        RolRepository roles = mock(RolRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(roles.existsById(any())).thenReturn(true);
        when(usuarios.existsByCorreo(any())).thenReturn(true);

        new DataInitializer(roles, usuarios, new SecurityBeansConfig().passwordEncoder()).run(null);

        verify(roles, never()).save(any());
        verify(usuarios, never()).save(any());
    }
}
