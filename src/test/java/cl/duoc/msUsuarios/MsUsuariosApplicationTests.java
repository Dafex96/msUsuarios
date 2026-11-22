package cl.duoc.msUsuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.duoc.msUsuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.msUsuarios.dto.LoginRequest;
import cl.duoc.msUsuarios.dto.RegistroUsuarioRequest;
import cl.duoc.msUsuarios.dto.RestablecerContrasenaRequest;
import cl.duoc.msUsuarios.dto.SolicitudCodigoResponse;
import cl.duoc.msUsuarios.dto.UsuarioResponse;
import cl.duoc.msUsuarios.dto.VerificarCodigoRequest;
import cl.duoc.msUsuarios.exception.ConflictoException;
import cl.duoc.msUsuarios.exception.CredencialesInvalidasException;
import cl.duoc.msUsuarios.exception.SolicitudInvalidaException;
import cl.duoc.msUsuarios.service.RecuperacionContrasenaService;
import cl.duoc.msUsuarios.service.UsuarioService;
import cl.duoc.msUsuarios.util.RutUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MsUsuariosApplicationTests {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    RecuperacionContrasenaService recuperacionService;

    @Test
    void contextLoads() {
    }

    @Test
    void datosDePruebaPermitenIniciarSesion() {
        UsuarioResponse sofia = usuarioService.login(new LoginRequest("sofia@gmail.com", "cli123"));
        assertEquals("CLIENTE", sofia.rolNombre());
        UsuarioResponse matias = usuarioService.login(new LoginRequest("10.625.244-K", "age123"));
        assertEquals("AGENTE", matias.rolNombre());
        assertThrows(CredencialesInvalidasException.class,
                () -> usuarioService.login(new LoginRequest("sofia@gmail.com", "mala")));
    }

    @Test
    void registroYPerfil() {
        UsuarioResponse nuevo = usuarioService.registrar(new RegistroUsuarioRequest(
                "12.345.678-5", "Camila Rojas", "Camila@Test.cl", "Clave.123", null, null, null, null));
        assertNotNull(nuevo.id());
        assertEquals("camila@test.cl", nuevo.correo());
        assertEquals("CLIENTE", nuevo.rolNombre());

        assertThrows(ConflictoException.class, () -> usuarioService.registrar(new RegistroUsuarioRequest(
                "12.345.678-5", "Otra Persona", "otra@test.cl", "Clave.123", null, null, null, null)));

        UsuarioResponse editado = usuarioService.actualizar(nuevo.id(), new ActualizarUsuarioRequest(
                "12.345.678-5", "Camila R", "camila@test.cl", null, "+56911111111", "Calle 1", "Santiago"));
        assertEquals("Santiago", editado.ciudad());
    }

    @Test
    void recuperacionDeContrasena() {
        usuarioService.registrar(new RegistroUsuarioRequest(
                "11.111.111-1", "Luis Recupera", "luis@test.cl", "Clave.123", null, null, null, null));
        SolicitudCodigoResponse r = recuperacionService.solicitar("luis@test.cl");
        assertNotNull(r.codigoDesarrollo());
        assertThrows(SolicitudInvalidaException.class, () -> recuperacionService
                .verificar(new VerificarCodigoRequest("luis@test.cl", "000000x")));
        recuperacionService.verificar(new VerificarCodigoRequest("luis@test.cl", r.codigoDesarrollo()));
        recuperacionService.restablecer(new RestablecerContrasenaRequest(
                "luis@test.cl", r.codigoDesarrollo(), "Nueva.123"));
        assertEquals("CLIENTE", usuarioService.login(new LoginRequest("luis@test.cl", "Nueva.123")).rolNombre());
    }

    @Test
    void rutUtil() {
        assertTrue(RutUtil.esValido("12.345.678-5"));
        assertEquals("12.345.678-5", RutUtil.formatear("123456785"));
    }
}
