package cl.duoc.msUsuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.msUsuarios.dto.MensajeResponse;
import cl.duoc.msUsuarios.dto.SolicitudCodigoResponse;
import cl.duoc.msUsuarios.exception.GlobalExceptionHandler;
import cl.duoc.msUsuarios.exception.SolicitudInvalidaException;
import cl.duoc.msUsuarios.service.RecuperacionContrasenaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecuperacionControllerTest {

    @Mock
    RecuperacionContrasenaService service;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new RecuperacionController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void solicitarCodigo() throws Exception {
        when(service.solicitar("sofia@gmail.com"))
                .thenReturn(new SolicitudCodigoResponse("Enviado", 10, "123456"));
        mvc.perform(post("/api/usuarios/recuperar/solicitar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"sofia@gmail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutosExpiracion").value(10))
                .andExpect(jsonPath("$.codigoDesarrollo").value("123456"));
    }

    @Test
    void solicitarConCorreoInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/usuarios/recuperar/solicitar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"mal\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void verificarCodigo() throws Exception {
        when(service.verificar(any())).thenReturn(new MensajeResponse("Código verificado"));
        mvc.perform(post("/api/usuarios/recuperar/verificar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"sofia@gmail.com\",\"codigo\":\"123456\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Código verificado"));
    }

    @Test
    void verificarCodigoInvalidoDevuelve400() throws Exception {
        when(service.verificar(any())).thenThrow(new SolicitudInvalidaException("El código es inválido o ha expirado"));
        mvc.perform(post("/api/usuarios/recuperar/verificar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"sofia@gmail.com\",\"codigo\":\"000000\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void restablecerContrasena() throws Exception {
        when(service.restablecer(any())).thenReturn(new MensajeResponse("Contraseña restablecida correctamente"));
        mvc.perform(post("/api/usuarios/recuperar/restablecer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"sofia@gmail.com\",\"codigo\":\"123456\",\"nuevaContrasena\":\"Nueva.123\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/usuarios/recuperar/restablecer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"sofia@gmail.com\",\"codigo\":\"123456\",\"nuevaContrasena\":\"debil\"}"))
                .andExpect(status().isBadRequest());
    }
}
