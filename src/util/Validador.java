package util;

/**
 * Clase de utilidad con métodos estáticos de validación de datos de entrada.
 * Centraliza las reglas de validación para reutilizarlas en los formularios de
 * registro, alta y edición, evitando repetir código (principio DRY).
 *
 * Es una clase "final" con constructor privado porque solo contiene métodos
 * estáticos: no tiene sentido crear instancias de ella.
 */
public final class Validador {

    // Expresiones regulares reutilizables para validar formatos
    private static final String REGEX_EMAIL    = "^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$";
    private static final String REGEX_DNI      = "^\\d{8}[A-Za-z]$"; // 8 números + 1 letra
    private static final String REGEX_TELEFONO = "^\\d{9}$";          // exactamente 9 dígitos

    /** Constructor privado: impide instanciar una clase puramente de utilidad. */
    private Validador() {
    }

    /** Comprueba que el texto no es nulo ni está vacío (ignorando espacios). */
    public static boolean noVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /** Comprueba que el texto tiene al menos 'min' caracteres (sin contar espacios). */
    public static boolean longitudMinima(String texto, int min) {
        return texto != null && texto.trim().length() >= min;
    }

    /** Comprueba que el email tiene un formato válido (texto@dominio.ext). */
    public static boolean esEmail(String email) {
        return email != null && email.matches(REGEX_EMAIL);
    }

    /** Comprueba que el DNI son 8 números seguidos de una letra (ej: 12345678A). */
    public static boolean esDni(String dni) {
        return dni != null && dni.matches(REGEX_DNI);
    }

    /** Comprueba que el teléfono son exactamente 9 dígitos. */
    public static boolean esTelefono(String telefono) {
        return telefono != null && telefono.matches(REGEX_TELEFONO);
    }

    /**
     * Valida los campos comunes a todo usuario (tabla usuarios y sus hijas).
     * Devuelve un mensaje de error descriptivo, o {@code null} si todo es válido.
     *
     * @param passwordObligatoria {@code true} al crear (la contraseña es obligatoria);
     *                            {@code false} al editar (vacía = mantener la actual).
     */
    public static String validarUsuario(String username, String password, String email,
            String nombre, String apellidos, String dni, boolean passwordObligatoria) {

        if (!noVacio(username) || !noVacio(email) || !noVacio(nombre)
                || !noVacio(apellidos) || !noVacio(dni)) {
            return "Rellena todos los campos obligatorios.";
        }
        if (!longitudMinima(username, 3)) {
            return "El nombre de usuario debe tener al menos 3 caracteres.";
        }
        // Al crear la contraseña es obligatoria; al editar solo se valida si se escribe una nueva
        if (passwordObligatoria && !longitudMinima(password, 4)) {
            return "La contraseña debe tener al menos 4 caracteres.";
        }
        if (!passwordObligatoria && noVacio(password) && !longitudMinima(password, 4)) {
            return "La nueva contraseña debe tener al menos 4 caracteres.";
        }
        if (!esEmail(email)) {
            return "El email no es válido. Ejemplo: nombre@dominio.com";
        }
        if (!esDni(dni)) {
            return "El DNI debe tener 8 números seguidos de una letra. Ejemplo: 12345678A";
        }
        return null; // sin errores
    }

    /**
     * Valida los datos de una incidencia.
     * Devuelve un mensaje de error descriptivo, o {@code null} si todo es válido.
     */
    public static String validarIncidencia(String titulo, String descripcion) {
        if (!noVacio(titulo)) {
            return "El título es obligatorio.";
        }
        if (!longitudMinima(titulo, 3)) {
            return "El título debe tener al menos 3 caracteres.";
        }
        if (!noVacio(descripcion)) {
            return "La descripción es obligatoria.";
        }
        return null; // sin errores
    }
}
