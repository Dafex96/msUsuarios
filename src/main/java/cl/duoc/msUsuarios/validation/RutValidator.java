package cl.duoc.msUsuarios.validation;

import cl.duoc.msUsuarios.util.RutUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutValidator implements ConstraintValidator<RutValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        return valor == null || valor.isBlank() || RutUtil.esValido(valor);
    }
}
