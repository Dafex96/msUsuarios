package cl.duoc.msUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario sin datos sensibles (nunca incluye la contraseña)")
public record UsuarioResponse(
        Long id,
        String rut,
        String nombre,
        String correo,
        Long rolId,
        String rolNombre,
        String telefono,
        String direccion,
        String ciudad,
        @Schema(description = "Foto de perfil en Base64") String avatar) {
}
