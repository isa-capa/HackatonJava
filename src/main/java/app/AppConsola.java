package app;

import java.util.List;
import java.util.Scanner;

/**
 * DOCUMENTACIÓN PARA EL EQUIPO:
 * Versión por consola de la Agenda de Contactos.
 *
 * Responsabilidad:
 * - Mostrar menú, leer inputs y delegar operaciones a Agenda/Contacto.
 *
 * Nota:
 * - La lógica de negocio (duplicados, capacidad, validaciones de nombre/apellido) vive en Agenda/Contacto.
 * - Aquí mantenemos el flujo de consola y manejo básico de errores.
 */
public class AppConsola {

    // -------------------------
    // 1) Configuración
    // -------------------------

    private static final String APP_TITLE = "=== AGENDA DE CONTACTOS ===";

    private static final int OPTION_ADD = 1;
    private static final int OPTION_LIST = 2;
    private static final int OPTION_SEARCH = 3;
    private static final int OPTION_DELETE = 4;
    private static final int OPTION_STATUS = 5;
    private static final int OPTION_EXIT = 6;

    private static final String MSG_INVALID_OPTION = "Opción inválida.";
    private static final String MSG_EXIT = "Saliendo... 👋";
    private static final String MSG_EMPTY_AGENDA = "Agenda vacía.";
    private static final String MSG_CONTACT_NOT_FOUND = "No existe un contacto con ese nombre y apellido.";
    private static final String MSG_NUMBER_REQUIRED = "Por favor ingresa un número válido.";

    // -------------------------
    // 2) Lógica principal (entrypoint)
    // -------------------------

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println(APP_TITLE);

            Agenda agenda = crearAgendaDesdeInput(scanner);
            ejecutarLoopMenu(scanner, agenda);
        }
    }

    // -------------------------
    // 3) Menú y flujo principal
    // -------------------------

    private static void ejecutarLoopMenu(Scanner scanner, Agenda agenda) {
        boolean shouldExit = false;

        while (!shouldExit) {
            imprimirMenu();

            int opcion = leerEntero(scanner, "Elige una opción: ");
            shouldExit = manejarOpcion(opcion, scanner, agenda);
        }
    }

    private static boolean manejarOpcion(int opcion, Scanner scanner, Agenda agenda) {
        switch (opcion) {
            case OPTION_ADD -> agregarContacto(scanner, agenda);
            case OPTION_LIST -> listarContactos(agenda);
            case OPTION_SEARCH -> buscarContacto(scanner, agenda);
            case OPTION_DELETE -> eliminarContacto(scanner, agenda);
            case OPTION_STATUS -> mostrarEstado(agenda);
            case OPTION_EXIT -> {
                System.out.println(MSG_EXIT);
                return true;
            }
            default -> System.out.println(MSG_INVALID_OPTION);
        }
        return false;
    }

    private static void imprimirMenu() {
        System.out.println("\n--- MENÚ ---");
        System.out.println("1) Añadir contacto");
        System.out.println("2) Listar contactos");
        System.out.println("3) Buscar contacto por nombre y apellido");
        System.out.println("4) Eliminar contacto por ID");
        System.out.println("5) Estado (llena / espacios libres)");
        System.out.println("6) Salir");
    }

    // -------------------------
    // 4) Acciones del menú
    // -------------------------

    /**
     * Crea agenda con tamaño por defecto o tamaño indicado.
     * Mantiene el comportamiento original: cualquier opción distinta de 2 => agenda por defecto.
     */
    private static Agenda crearAgendaDesdeInput(Scanner scanner) {
        System.out.println("¿Cómo quieres crear la agenda?");
        System.out.println("1) Tamaño por defecto (10)");
        System.out.println("2) Indicar tamaño");

        int opcion = leerEntero(scanner, "Opción: ");
        if (opcion != 2) {
            return new Agenda();
        }

        int tamanio = leerEntero(scanner, "Tamaño de agenda: ");
        return new Agenda(tamanio);
    }

    private static void agregarContacto(Scanner scanner, Agenda agenda) {
        try {
            String nombre = leerTexto(scanner, "Nombre (1 palabra): ");
            String apellido = leerTexto(scanner, "Apellido (1 palabra): ");
            String telefono = leerTexto(scanner, "Teléfono: ");

            Contacto contacto = new Contacto(nombre, apellido, telefono);

            // Mantenemos funcionalidad: Agenda decide si se añade o no y lo imprime a consola.
            agenda.anadirContacto(contacto);

        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void listarContactos(Agenda agenda) {
        List<Contacto> contactos = agenda.listarContactos();

        if (contactos.isEmpty()) {
            System.out.println(MSG_EMPTY_AGENDA);
            return;
        }

        System.out.println("Contactos:");
        for (Contacto contacto : contactos) {
            System.out.println("- " + contacto);
        }
    }

    private static void buscarContacto(Scanner scanner, Agenda agenda) {
        String nombre = leerTexto(scanner, "Nombre: ");
        String apellido = leerTexto(scanner, "Apellido: ");

        Contacto encontrado = agenda.buscaContacto(nombre, apellido);
        if (encontrado == null) {
            System.out.println(MSG_CONTACT_NOT_FOUND);
            return;
        }

        System.out.println("Encontrado: " + encontrado);
    }

    private static void eliminarContacto(Scanner scanner, Agenda agenda) {
        int id = leerEntero(scanner, "ID a eliminar: ");

        // Mantenemos funcionalidad: Agenda imprime si se eliminó o no.
        agenda.eliminarContactoPorId(id);
    }

    private static void mostrarEstado(Agenda agenda) {
        System.out.println("¿Agenda llena? " + (agenda.agendaLlena() ? "Sí" : "No"));
        System.out.println("Espacios libres: " + agenda.espacioLibres() + " / " + agenda.getCapacidadMaxima());
    }

    // -------------------------
    // 5) Helpers (entrada robusta)
    // -------------------------

    private static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            String input = leerTexto(scanner, mensaje);

            Integer numero = parsearEnteroSeguro(input);
            if (numero != null) {
                return numero;
            }

            System.out.println(MSG_NUMBER_REQUIRED);
        }
    }

    private static String leerTexto(Scanner scanner, String mensaje) {
        System.out.print(mensaje);
        String input = scanner.nextLine();
        return input == null ? "" : input.trim();
    }

    private static Integer parsearEnteroSeguro(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

