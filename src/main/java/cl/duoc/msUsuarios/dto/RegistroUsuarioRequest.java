package cl.duoc.msUsuarios.dto;

import cl.duoc.msUsuarios.validation.RutValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un usuario nuevo")
public record RegistroUsuarioRequest(
        @Schema(example = "12.345.678-5")
        @NotBlank(message = "Ingresa tu RUT") @RutValido String rut,

        @Schema(example = "Camila Rojas")
        @NotBlank(message = "Ingresa tu nombre")
        @Size(max = 100, message = "El nombre es demasiado largo")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$", message = "El nombre solo puede tener letras y espacios")
        String nombre,

        @Schema(example = "camila@gmail.com")
        @NotBlank(message = "Ingresa tu correo")
        @Size(max = 100, message = "El correo es demasiado largo")
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$", message = "Ingresa un correo válido")
        String correo,

        @Schema(example = "Clave.123", description = "Mínimo 6 caracteres, con mayúscula, minúscula, número y carácter especial")
        @NotBlank(message = "Ingresa una contraseña")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{6,}$",
                message = "La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial, sin espacios")
        String contrasena,

        @Schema(description = "1 CLIENTE, 2 AGENTE, 3 EJECUTIVO. Si no se envía, se registra como CLIENTE", example = "1")
        Long rolId,

        @Size(max = 20, message = "El teléfono es demasiado largo") String telefono,
        @Size(max = 150, message = "La dirección es demasiado larga") String direccion,
        @Size(max = 80, message = "La ciudad es demasiado larga") String ciudad) {
}
