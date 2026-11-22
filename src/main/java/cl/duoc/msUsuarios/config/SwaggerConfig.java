package cl.duoc.msUsuarios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("PagoFlex - Microservicio de Usuarios")
                .version("1.0.0")
                .description("Registro, inicio de sesión, perfil, avatar, cambio y recuperación de contraseña, y roles."));
    }
}
