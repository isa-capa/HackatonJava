package app;

/**
 * DOCUMENTACIÓN PARA EL EQUIPO:
 * Punto de entrada de la aplicación.
 *
 * Decisión de diseño:
 * - Delegamos el arranque a AppFx para iniciar JavaFX sin depender de lógica de consola.
 * - Mantener este Main “delgado” facilita cambiar la UI (Swing/JavaFX) sin tocar el resto del proyecto.
 */
public class Main {

    // -------------------------
    // 1) Lógica principal (entrypoint)
    // -------------------------

    public static void main(String[] args) {
        // Arranca la aplicación JavaFX (AppFx extiende Application y llama a launch internamente)
        AppFx.main(args);
    }
}
