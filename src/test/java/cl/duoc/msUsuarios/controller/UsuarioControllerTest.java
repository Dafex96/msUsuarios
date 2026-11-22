package cl.duoc.msUsuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.msUsuarios.dto.UsuarioResponse;
import cl.duoc.msUsuarios.exception.ConflictoException;
import cl.duoc.msUsuarios.exception.CredencialesInvalidasException;
import cl.duoc.msUsuarios.exception.GlobalExceptionHandler;
import cl.duoc.msUsuarios.exception.RecursoNoEncontradoException;
import cl.duoc.msUsuarios.exception.SolicitudInvalidaException;
import cl.duoc.msUsuarios.service.UsuarioService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioControllerTest {

    private static final String REGISTRO_VALIDO = """
            {"rut":"12.345.678-5","nombre":"Camila Rojas","correo":"camila@test.cl","contrasena":"Clave.123"}""";

    @Mock
    UsuarioService service;

    MockMvc mvc;

    private final UsuarioResponse respuesta = new UsuarioResponse(1L, "12.345.678-5", "Camila Rojas",
            "camila@test.cl", 1L, "CLIENTE", "", "", "", null);

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new UsuarioController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void registrarDevuelve201ConLocation() throws Exception {
        when(service.registrar(any())).thenReturn(respuesta);
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(REGISTRO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/usuarios/1"))
                .andExpect(jsonPath("$.correo").value("camila@test.cl"));
    }

    @Test
    void registrarConDatosInvalidosDevuelve400ConDetalles() throws Exception {
        String malo = """
                {"rut":"12.345.678-0","nombre":"","correo":"no-es-correo","contrasena":"abc"}""";
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(malo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.rut").exists())
                .andExpect(jsonPath("$.detalles.correo").exists())
                .andExpect(jsonPath("$.detalles.contrasena").exists());
    }

    @Test
    void registrarConJsonMalFormadoDevuelve400() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarDuplicadoDevuelve409() throws Exception {
        when(service.registrar(any())).thenThrow(new ConflictoException("Ya existe una cuenta con ese correo"));
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(REGISTRO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una cuenta con ese correo"));
    }

    @Test
    void loginCorrectoYIncorrecto() throws Exception {
        String body = "{\"identificador\":\"sofia@gmail.com\",\"contrasena\":\"cli123\"}";
        when(service.login(any())).thenReturn(respuesta);
        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));

        when(service.login(any())).thenThrow(new CredencialesInvalidasException("Credenciales incorrectas"));
        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSinCamposDevuelve400() throws Exception {
        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.identificador").exists());
    }

    @Test
    void consultas() throws Exception {
        when(service.listar()).thenReturn(List.of(respuesta));
        when(service.buscar("cam")).thenReturn(List.of(respuesta));
        when(service.listarPorRol(1L)).thenReturn(List.of(respuesta));
        when(service.obtener(1L)).thenReturn(respuesta);

        mvc.perform(get("/api/usuarios")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
        mvc.perform(get("/api/usuarios/buscar").param("texto", "cam")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Camila Rojas"));
        mvc.perform(get("/api/usuarios/rol/1")).andExpect(status().isOk());
        mvc.perform(get("/api/usuarios/1")).andExpect(status().isOk()).andExpect(jsonPath("$.rolNombre").value("CLIENTE"));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));
        mvc.perform(get("/api/usuarios/99")).andExpect(status().isNotFound());
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mvc.perform(get("/api/usuarios/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void actualizarPerfilAvatarYContrasena() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(respuesta);
        when(service.actualizarAvatar(1L, "QUJD")).thenReturn(respuesta);

        String perfil = "{\"rut\":\"12.345.678-5\",\"nombre\":\"Camila R\",\"correo\":\"camila@test.cl\"}";
        mvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON).content(perfil))
                .andExpect(status().isOk());
        mvc.perform(put("/api/usuarios/1/avatar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"avatar\":\"QUJD\"}")).andExpect(status().isOk());
        mvc.perform(put("/api/usuarios/1/avatar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"avatar\":\"\"}")).andExpect(status().isBadRequest());

        String clave = "{\"actual\":\"cli123\",\"nueva\":\"Nueva.123\"}";
        mvc.perform(put("/api/usuarios/1/contrasena").contentType(MediaType.APPLICATION_JSON).content(clave))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").exists());
        mvc.perform(put("/api/usuarios/1/contrasena").contentType(MediaType.APPLICATION_JSON)
                .content("{\"actual\":\"cli123\",\"nueva\":\"debil\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void cambiarContrasenaConActualIncorrectaDevuelve400() throws Exception {
        doThrow(new SolicitudInvalidaException("La contraseña actual no es correcta"))
                .when(service).cambiarContrasena(eq(1L), any());
        mvc.perform(put("/api/usuarios/1/contrasena").contentType(MediaType.APPLICATION_JSON)
                .content("{\"actual\":\"mala\",\"nueva\":\"Nueva.123\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mvc.perform(delete("/api/usuarios/1")).andExpect(status().isNoContent());
        verify(service).eliminar(1L);
    }

    @Test
    void violacionDeIntegridadDevuelve409() throws Exception {
        when(service.obtener(5L)).thenThrow(new DataIntegrityViolationException("duplicado"));
        mvc.perform(get("/api/usuarios/5")).andExpect(status().isConflict());
    }

    @Test
    void errorInesperadoDevuelve500() throws Exception {
        when(service.obtener(6L)).thenThrow(new IllegalStateException("boom"));
        mvc.perform(get("/api/usuarios/6")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado"));
    }
}
