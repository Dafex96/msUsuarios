package cl.duoc.msUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Correo o RUT", example = "sofia@gmail.com")
        @NotBlank(message = "Ingresa tu correo o RUT") String identificador,

        @Schema(example = "cli123")
        @NotBlank(message = "Ingresa tu contraseña") String contrasena) {
}
