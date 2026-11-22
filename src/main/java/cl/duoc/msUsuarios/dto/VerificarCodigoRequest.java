package cl.duoc.msUsuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record VerificarCodigoRequest(
        @NotBlank(message = "Ingresa tu correo") String correo,
        @NotBlank(message = "Ingresa el código de verificación") String codigo) {
}
