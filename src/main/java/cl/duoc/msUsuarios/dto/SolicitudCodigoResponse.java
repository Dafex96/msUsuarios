package cl.duoc.msUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SolicitudCodigoResponse(
        String mensaje,
        int minutosExpiracion,
        @Schema(description = "Solo en desarrollo (usuarios.recuperacion.exponer-codigo=true)")
        String codigoDesarrollo) {
}
