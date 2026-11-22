package cl.duoc.msUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AvatarRequest(
        @Schema(description = "Imagen en Base64 (sin prefijo data:)")
        @NotBlank(message = "Envía la imagen en Base64")
        @Size(max = 3_000_000, message = "La imagen es demasiado grande")
        String avatar) {
}
