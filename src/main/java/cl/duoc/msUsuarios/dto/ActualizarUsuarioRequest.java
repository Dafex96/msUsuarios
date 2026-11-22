package cl.duoc.msUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos editables del perfil. El RUT solo se valida si cambia")
public record ActualizarUsuarioRequest(
        @Schema(example = "12.345.678-5")
        @NotBlank(message = "Ingresa el RUT") String rut,

        @Schema(example = "Camila Rojas")
        @NotBlank(message = "Ingresa el nombre")
        @Size(max = 100, message = "El nombre es demasiado largo")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$", message = "El nombre solo puede tener letras y espacios")
        String nombre,

        @Schema(example = "camila@gmail.com")
        @NotBlank(message = "Ingresa el correo")
        @Size(max = 100, message = "El correo es demasiado largo")
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$", message = "Ingresa un correo válido")
        String correo,

        @Schema(description = "Si no se envía, se mantiene el rol actual")
        Long rolId,

        @Size(max = 20, message = "El teléfono es demasiado largo") String telefono,
        @Size(max = 150, message = "La dirección es demasiado larga") String direccion,
        @Size(max = 80, message = "La ciudad es demasiado larga") String ciudad) {
}
