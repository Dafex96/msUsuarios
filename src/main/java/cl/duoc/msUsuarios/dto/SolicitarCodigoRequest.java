package cl.duoc.msUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SolicitarCodigoRequest(
        @Schema(example = "sofia@gmail.com")
        @NotBlank(message = "Ingresa tu correo")
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$", message = "Ingresa un correo válido")
        String correo) {
}
