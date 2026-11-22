package cl.duoc.msUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarContrasenaRequest(
        @NotBlank(message = "Ingresa tu contraseña actual") String actual,

        @Schema(example = "NuevaClave.1")
        @NotBlank(message = "Ingresa la nueva contraseña")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{6,}$",
                message = "La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial, sin espacios")
        String nueva) {
}
