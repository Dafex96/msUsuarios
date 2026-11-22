package cl.duoc.msUsuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.msUsuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.msUsuarios.dto.CambiarContrasenaRequest;
import cl.duoc.msUsuarios.dto.LoginRequest;
import cl.duoc.msUsuarios.dto.RegistroUsuarioRequest;
import cl.duoc.msUsuarios.dto.UsuarioResponse;
import cl.duoc.msUsuarios.exception.ConflictoException;
import cl.duoc.msUsuarios.exception.CredencialesInvalidasException;
import cl.duoc.msUsuarios.exception.RecursoNoEncontradoException;
import cl.duoc.msUsuarios.exception.SolicitudInvalidaException;
import cl.duoc.msUsuarios.model.Rol;
import cl.duoc.msUsuarios.model.Usuario;
import cl.duoc.msUsuarios.repository.RolRepository;
import cl.duoc.msUsuarios.repository.UsuarioRepository;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    RolRepository rolRepository;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, rolRepository, encoder);
        when(rolRepository.findById(1L)).thenReturn(Optional.of(new Rol(1L, "CLIENTE")));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(new Rol(2L, "AGENTE")));
        when(rolRepository.existsById(1L)).thenReturn(true);
        when(rolRepository.existsById(2L)).thenReturn(true);
        when(rolRepository.findAll()).thenReturn(List.of(new Rol(1L, "CLIENTE"), new Rol(2L, "AGENTE")));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Usuario usuario(Long id, String rut, String correo, String clave, Long rolId) {
        return Usuario.builder().id(id).rut(rut).nombre("Persona Prueba").correo(correo)
                .contrasena(encoder.encode(clave)).rolId(rolId).build();
    }

    private RegistroUsuarioRequest registro(Long rolId) {
        return new RegistroUsuarioRequest("12.345.678-5", " Camila Rojas ", "Camila@Test.cl", "Clave.123",
                rolId, " 12345 ", null, " Santiago ");
    }

    @Test
    void registrarGuardaHashYUsaRolClientePorDefecto() {
        UsuarioResponse r = service.registrar(registro(null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("camila@test.cl", guardado.getCorreo());
        assertEquals("Camila Rojas", guardado.getNombre());
        assertEquals("12345", guardado.getTelefono());
        assertEquals("", guardado.getDireccion());
        assertEquals("Santiago", guardado.getCiudad());
        assertTrue(encoder.matches("Clave.123", guardado.getContrasena()));
        assertEquals("CLIENTE", r.rolNombre());
        assertEquals(1L, r.rolId());
    }

    @Test
    void registrarConRolIndicado() {
        UsuarioResponse r = service.registrar(registro(2L));
        assertEquals("AGENTE", r.rolNombre());
    }

    @Test
    void registrarFallaSiCorreoYaExiste() {
        when(usuarioRepository.existsByCorreo("camila@test.cl")).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.registrar(registro(null)));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarFallaSiRutYaExiste() {
        when(usuarioRepository.existsByRut("12.345.678-5")).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.registrar(registro(null)));
    }

    @Test
    void registrarFallaSiRolNoExiste() {
        assertThrows(SolicitudInvalidaException.class, () -> service.registrar(registro(99L)));
    }

    // ---------- login ----------

    @Test
    void loginPorCorreo() {
        when(usuarioRepository.findByCorreo("sofia@gmail.com"))
                .thenReturn(Optional.of(usuario(1L, "20.435.214-5", "sofia@gmail.com", "cli123", 1L)));
        UsuarioResponse r = service.login(new LoginRequest(" Sofia@Gmail.com ", "cli123"));
        assertEquals("sofia@gmail.com", r.correo());
        assertEquals("CLIENTE", r.rolNombre());
    }

    @Test
    void loginPorRut() {
        when(usuarioRepository.findByRut("10.625.244-K"))
                .thenReturn(Optional.of(usuario(2L, "10.625.244-K", "matias@pagoflex.com", "age123", 2L)));
        UsuarioResponse r = service.login(new LoginRequest("10625244k", "age123"));
        assertEquals("AGENTE", r.rolNombre());
    }

    @Test
    void loginFallaConContrasenaIncorrecta() {
        when(usuarioRepository.findByCorreo("sofia@gmail.com"))
                .thenReturn(Optional.of(usuario(1L, "20.435.214-5", "sofia@gmail.com", "cli123", 1L)));
        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("sofia@gmail.com", "otra")));
    }

    @Test
    void loginFallaSiUsuarioNoExiste() {
        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("nadie@gmail.com", "x")));
        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("11.111.111-1", "x")));
    }

    @Test
    void listarBuscarYListarPorRolDevuelvenNombreDeRol() {
        Usuario u = usuario(1L, "12.345.678-5", "a@a.cl", "Clave.123", 2L);
        when(usuarioRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(u));
        when(usuarioRepository.buscar("per")).thenReturn(List.of(u));
        when(usuarioRepository.findByRolIdOrderByNombreAsc(2L)).thenReturn(List.of(u));

        assertEquals("AGENTE", service.listar().get(0).rolNombre());
        assertEquals(1, service.buscar(" per ").size());
        assertEquals(1, service.listarPorRol(2L).size());
    }

    @Test
    void listarConRolDesconocidoDejaNombreVacio() {
        when(usuarioRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(usuario(1L, "12.345.678-5", "a@a.cl", "Clave.123", 99L)));
        assertEquals("", service.listar().get(0).rolNombre());
    }

    @Test
    void obtenerPorId() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario(1L, "12.345.678-5", "a@a.cl", "Clave.123", 1L)));
        assertEquals(1L, service.obtener(1L).id());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(404L));
    }

    private Usuario existente() {
        Usuario u = usuario(1L, "20.435.214-5", "sofia@gmail.com", "cli123", 1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        return u;
    }

    @Test
    void actualizarConservaRutDemoSinValidarlo() {
        existente();
        UsuarioResponse r = service.actualizar(1L, new ActualizarUsuarioRequest(
                "20435214-5", "Sofia P", "SOFIA@gmail.com", null, "999", "Calle 1", "Santiago"));
        assertEquals("20.435.214-5", r.rut());
        assertEquals("Sofia P", r.nombre());
        assertEquals("Santiago", r.ciudad());
        assertEquals(1L, r.rolId());
    }

    @Test
    void actualizarPermiteCambiarRutValidoYRol() {
        existente();
        UsuarioResponse r = service.actualizar(1L, new ActualizarUsuarioRequest(
                "12.345.678-5", "Sofia P", "sofia@gmail.com", 2L, null, null, null));
        assertEquals("12.345.678-5", r.rut());
        assertEquals("AGENTE", r.rolNombre());
    }

    @Test
    void actualizarRechazaRutNuevoInvalido() {
        existente();
        assertThrows(SolicitudInvalidaException.class, () -> service.actualizar(1L, new ActualizarUsuarioRequest(
                "11.111.111-2", "Sofia P", "sofia@gmail.com", null, null, null, null)));
    }

    @Test
    void actualizarRechazaCorreoORutDeOtroUsuario() {
        existente();
        when(usuarioRepository.existsByCorreoAndIdNot("otro@gmail.com", 1L)).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.actualizar(1L, new ActualizarUsuarioRequest(
                "20.435.214-5", "Sofia P", "otro@gmail.com", null, null, null, null)));

        when(usuarioRepository.existsByRutAndIdNot("12.345.678-5", 1L)).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.actualizar(1L, new ActualizarUsuarioRequest(
                "12.345.678-5", "Sofia P", "sofia@gmail.com", null, null, null, null)));
    }

    @Test
    void actualizarRechazaRolInexistenteYUsuarioInexistente() {
        existente();
        assertThrows(SolicitudInvalidaException.class, () -> service.actualizar(1L, new ActualizarUsuarioRequest(
                "20.435.214-5", "Sofia P", "sofia@gmail.com", 99L, null, null, null)));
        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(404L,
                new ActualizarUsuarioRequest("20.435.214-5", "Sofia P", "sofia@gmail.com", null, null, null, null)));
    }

    @Test
    void actualizarAvatar() {
        existente();
        UsuarioResponse r = service.actualizarAvatar(1L, "QUJD");
        assertEquals("QUJD", r.avatar());
    }

    @Test
    void cambiarContrasena() {
        Usuario u = existente();
        service.cambiarContrasena(1L, new CambiarContrasenaRequest("cli123", "Nueva.123"));
        assertTrue(encoder.matches("Nueva.123", u.getContrasena()));
    }

    @Test
    void cambiarContrasenaFallaSiLaActualEsIncorrecta() {
        existente();
        assertThrows(SolicitudInvalidaException.class,
                () -> service.cambiarContrasena(1L, new CambiarContrasenaRequest("mala", "Nueva.123")));
    }

    @Test
    void eliminar() {
        Usuario u = existente();
        service.eliminar(1L);
        verify(usuarioRepository).delete(u);
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(404L));
    }
}
