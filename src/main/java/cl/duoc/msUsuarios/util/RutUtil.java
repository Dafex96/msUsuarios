package cl.duoc.msUsuarios.util;

public final class RutUtil {

    private RutUtil() {
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().toUpperCase().replace(".", "").replace("-", "").replace(" ", "");
    }

    public static boolean esValido(String valor) {
        String rut = normalizar(valor);
        if (!rut.matches("[0-9]{1,8}[0-9K]")) {
            return false;
        }
        String cuerpo = rut.substring(0, rut.length() - 1);
        if (cuerpo.charAt(0) == '0') {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < cuerpo.length(); i++) {
            int digito = cuerpo.charAt(cuerpo.length() - 1 - i) - '0';
            suma += digito * (2 + i % 6);
        }
        int resto = 11 - suma % 11;
        char verificador = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return rut.charAt(rut.length() - 1) == verificador;
    }

    public static String formatear(String valor) {
        String rut = normalizar(valor);
        if (rut.length() < 2) {
            return rut;
        }
        String cuerpo = rut.substring(0, rut.length() - 1);
        StringBuilder sb = new StringBuilder();
        int contador = 0;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            if (contador > 0 && contador % 3 == 0) {
                sb.append('.');
            }
            sb.append(cuerpo.charAt(i));
            contador++;
        }
        return sb.reverse() + "-" + rut.charAt(rut.length() - 1);
    }
}
