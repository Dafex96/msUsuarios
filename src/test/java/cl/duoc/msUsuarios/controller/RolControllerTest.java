package cl.duoc.msUsuarios.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.msUsuarios.model.Rol;
import cl.duoc.msUsuarios.repository.RolRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RolControllerTest {

    @Mock
    RolRepository rolRepository;

    @Test
    void listaLosRoles() throws Exception {
        when(rolRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(new Rol(1L, "CLIENTE"), new Rol(2L, "AGENTE")));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new RolController(rolRepository)).build();

        mvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].nombre").value("AGENTE"));
    }
}
