package cl.duoc.msUsuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.msUsuarios.dto.RestablecerContrasenaRequest;
import cl.duoc.msUsuarios.dto.SolicitudCodigoResponse;
import cl.duoc.msUsuarios.dto.VerificarCodigoRequest;
import cl.duoc.msUsuarios.exception.SolicitudInvalidaException;
import cl.duoc.msUsuarios.model.CodigoRecuperacion;
import cl.duoc.msUsuarios.model.Usuario;
import cl.duoc.msUsuarios.repository.CodigoRecuperacionRepository;
import cl.duoc.msUsuarios.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecuperacionContrasenaServiceTest {

    private static final String CORREO = "sofia@gmail.com";

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    CodigoRecuperacionRepository codigoRepository;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    RecuperacionContrasenaService service;

    @BeforeEach
    void setUp() {
        service = new RecuperacionContrasenaService(usuarioRepository, codigoRepository, encoder);
        ReflectionTestUtils.setField(service, "minutosExpiracion", 10);
        ReflectionTestUtils.setField(service, "maxIntentos", 3);
        ReflectionTestUtils.setField(service, "exponerCodigo", true);
    }

    private CodigoRecuperacion codigo(String plano, int intentos, LocalDateTime expiracion) {
        CodigoRecuperacion c = CodigoRecuperacion.builder().correo(CORREO).codigoHash(encoder.encode(plano))
                .expiracion(expiracion).intentos(intentos).build();
        when(codigoRepository.findTopByCorreoAndUsadoFalseOrderByIdDesc(CORREO)).thenReturn(Optional.of(c));
        return c;
    }

    @Test
    void solicitarGeneraCodigoParaCorreoRegistrado() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(new Usuario()));
        SolicitudCodigoResponse r = service.solicitar(" Sofia@Gmail.com ");

        assertNotNull(r.codigoDesarrollo());
        assertTrue(r.codigoDesarrollo().matches("\\d{6}"));
        assertEquals(10, r.minutosExpiracion());
        verify(codigoRepository).deleteByCorreo(CORREO);
        ArgumentCaptor<CodigoRecuperacion> captor = ArgumentCaptor.forClass(CodigoRecuperacion.class);
        verify(codigoRepository).save(captor.capture());
        assertTrue(encoder.matches(r.codigoDesarrollo(), captor.getValue().getCodigoHash()));
        assertTrue(captor.getValue().getExpiracion().isAfter(LocalDateTime.now()));
    }

    @Test
    void solicitarNoExponeElCodigoSiEstaDesactivado() {
        ReflectionTestUtils.setField(service, "exponerCodigo", false);
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(new Usuario()));
        assertNull(service.solicitar(CORREO).codigoDesarrollo());
        verify(codigoRepository).save(any(CodigoRecuperacion.class));
    }

    @Test
    void solicitarRespondeIgualSiElCorreoNoExiste() {
        SolicitudCodigoResponse r = service.solicitar("nadie@gmail.com");
        assertNull(r.codigoDesarrollo());
        assertNotNull(r.mensaje());
        verify(codigoRepository, never()).save(any());
    }

    @Test
    void verificarAceptaCodigoCorrecto() {
        codigo("123456", 0, LocalDateTime.now().plusMinutes(5));
        assertEquals("Código verificado", service.verificar(new VerificarCodigoRequest(CORREO, " 123456 ")).mensaje());
    }

    @Test
    void verificarRechazaCodigoIncorrectoYSumaIntento() {
        CodigoRecuperacion c = codigo("123456", 0, LocalDateTime.now().plusMinutes(5));
        assertThrows(SolicitudInvalidaException.class,
                () -> service.verificar(new VerificarCodigoRequest(CORREO, "000000")));
        assertEquals(1, c.getIntentos());
        verify(codigoRepository).save(c);
    }

    @Test
    void verificarRechazaCodigoExpirado() {
        codigo("123456", 0, LocalDateTime.now().minusMinutes(1));
        assertThrows(SolicitudInvalidaException.class,
                () -> service.verificar(new VerificarCodigoRequest(CORREO, "123456")));
    }

    @Test
    void verificarRechazaSiSeAgotaronLosIntentos() {
        codigo("123456", 3, LocalDateTime.now().plusMinutes(5));
        assertThrows(SolicitudInvalidaException.class,
                () -> service.verificar(new VerificarCodigoRequest(CORREO, "123456")));
    }

    @Test
    void verificarRechazaSiNoHayCodigoSolicitado() {
        assertThrows(SolicitudInvalidaException.class,
                () -> service.verificar(new VerificarCodigoRequest(CORREO, "123456")));
    }

    @Test
    void restablecerCambiaLaContrasenaYMarcaElCodigoComoUsado() {
        CodigoRecuperacion c = codigo("123456", 0, LocalDateTime.now().plusMinutes(5));
        Usuario u = Usuario.builder().correo(CORREO).contrasena("vieja").build();
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(u));

        service.restablecer(new RestablecerContrasenaRequest(CORREO, "123456", "Nueva.123"));

        assertTrue(encoder.matches("Nueva.123", u.getContrasena()));
        assertTrue(c.isUsado());
        verify(usuarioRepository).save(u);
        verify(codigoRepository).save(c);
    }

    @Test
    void restablecerFallaSiElUsuarioYaNoExiste() {
        codigo("123456", 0, LocalDateTime.now().plusMinutes(5));
        assertThrows(SolicitudInvalidaException.class,
                () -> service.restablecer(new RestablecerContrasenaRequest(CORREO, "123456", "Nueva.123")));
    }

    @Test
    void restablecerFallaConCodigoIncorrecto() {
        codigo("123456", 0, LocalDateTime.now().plusMinutes(5));
        assertThrows(SolicitudInvalidaException.class,
                () -> service.restablecer(new RestablecerContrasenaRequest(CORREO, "999999", "Nueva.123")));
        verify(usuarioRepository, never()).save(any());
    }
}
