package cl.duoc.msUsuarios.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.duoc.msUsuarios.validation.RutValidator;
import org.junit.jupiter.api.Test;

class RutUtilTest {

    @Test
    void normalizaQuitandoSeparadores() {
        assertEquals("123456785", RutUtil.normalizar(" 12.345.678-5 "));
        assertEquals("10625244K", RutUtil.normalizar("10.625.244-k"));
        assertEquals("", RutUtil.normalizar(null));
    }

    @Test
    void validaDigitoVerificador() {
        assertTrue(RutUtil.esValido("12.345.678-5"));
        assertTrue(RutUtil.esValido("11.111.111-1"));
        assertFalse(RutUtil.esValido("12.345.678-9"));
        assertFalse(RutUtil.esValido("20.435.214-5"));
        assertFalse(RutUtil.esValido("abc"));
        assertFalse(RutUtil.esValido(""));
        assertFalse(RutUtil.esValido("0-0"));
        assertFalse(RutUtil.esValido("012345678"));
    }

    @Test
    void validaRutConDigitoK() {
        assertTrue(RutUtil.esValido("1.000.005-K"));
        assertTrue(RutUtil.esValido("1000005k"));
        assertFalse(RutUtil.esValido("12345678-K"));
    }

    @Test
    void formateaConPuntosYGuion() {
        assertEquals("12.345.678-5", RutUtil.formatear("123456785"));
        assertEquals("1-9", RutUtil.formatear("19"));
        assertEquals("5", RutUtil.formatear("5"));
        assertEquals("", RutUtil.formatear(null));
    }

    @Test
    void validadorAceptaVaciosYRechazaInvalidos() {
        RutValidator validador = new RutValidator();
        assertTrue(validador.isValid(null, null));
        assertTrue(validador.isValid("  ", null));
        assertTrue(validador.isValid("12.345.678-5", null));
        assertFalse(validador.isValid("12.345.678-0", null));
    }
}
