package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DOCUMENTACIÓN PARA EL EQUIPO:
 * Agenda de contactos en memoria.
 *
 * Reglas actuales:
 * - Capacidad máxima mínima = 1 (si se pasa 0 o negativo, se fuerza a 1).
 * - No permite duplicados por (nombre + apellido) ignorando mayúsculas/minúsculas.
 * - Permite eliminar por ID.
 *
 * Nota: Esta clase imprime mensajes a consola (System.out). Eso es “UI” básica.
 * Para apps grandes, conviene usar logging o devolver mensajes (sin imprimir).
 */
public class Agenda {

    // -------------------------
    // 1) Configuración
    // -------------------------

    private static final int DEFAULT_CAPACITY = 10;
    private static final int MIN_CAPACITY = 1;

    private static final String MSG_ADD_NULL = "No se puede añadir: contacto nulo.";
    private static final String MSG_ADD_FULL = "No se puede añadir: la agenda está llena.";
    private static final String MSG_ADD_DUPLICATE = "No se puede añadir: ya existe ese nombre y apellido.";
    private static final String MSG_ADD_OK = "Contacto añadido correctamente.";

    private static final String MSG_DELETE_OK = "Contacto eliminado correctamente.";
    private static final String MSG_DELETE_NOT_FOUND = "No se eliminó: no existe un contacto con ese ID.";

    // -------------------------
    // 2) Estado
    // -------------------------

    private final int capacidadMaxima;
    private final List<Contacto> contactos;

    // -------------------------
    // 3) Lógica principal (constructores)
    // -------------------------

    public Agenda() {
        this(DEFAULT_CAPACITY);
    }

    public Agenda(int capacidadMaxima) {
        this.capacidadMaxima = clampCapacity(capacidadMaxima);
        this.contactos = new ArrayList<>(this.capacidadMaxima);
    }

    // -------------------------
    // 4) Operaciones principales (API pública)
    // -------------------------

    /**
     * Añade un contacto si:
     * - el contacto no es null
     * - hay espacio en la agenda
     * - no existe uno con el mismo (nombre + apellido) ignorando mayúsculas/minúsculas
     *
     * @param contacto Contacto a agregar.
     * @return true si se agregó, false si no se pudo (por reglas anteriores).
     */
    public boolean anadirContacto(Contacto contacto) {
        if (contacto == null) {
            System.out.println(MSG_ADD_NULL);
            return false;
        }
        if (agendaLlena()) {
            System.out.println(MSG_ADD_FULL);
            return false;
        }
        if (existeContacto(contacto.getNombre(), contacto.getApellido())) {
            System.out.println(MSG_ADD_DUPLICATE);
            return false;
        }

        contactos.add(contacto);
        System.out.println(MSG_ADD_OK);
        return true;
    }

    /**
     * Verifica existencia por nombre + apellido (recomendado).
     * Edge cases:
     * - Si nombre o apellido son null -> false
     * - Si están vacíos (""), el método compara contra strings vacíos (comportamiento actual).
     */
    public boolean existeContacto(String nombre, String apellido) {
        if (nombre == null || apellido == null) return false;

        String nombreNormalizado = nombre.trim();
        String apellidoNormalizado = apellido.trim();

        return buscarPorNombreApellido(nombreNormalizado, apellidoNormalizado) != null;
    }

    /**
     * Sobrecarga útil para verificar existencia a partir de un Contacto.
     */
    public boolean existeContacto(Contacto contacto) {
        if (contacto == null) return false;
        return existeContacto(contacto.getNombre(), contacto.getApellido());
    }

    /**
     * Lista inmutable (solo lectura) de contactos.
     * Nota: refleja cambios futuros porque envuelve la misma lista (comportamiento actual).
     */
    public List<Contacto> listarContactos() {
        return Collections.unmodifiableList(contactos);
    }

    /**
     * Busca un contacto por nombre y apellido (ignora mayúsculas/minúsculas).
     *
     * @param nombre   Nombre a buscar.
     * @param apellido Apellido a buscar.
     * @return El contacto si existe, o null si no existe / inputs son null.
     */
    public Contacto buscaContacto(String nombre, String apellido) {
        if (nombre == null || apellido == null) return null;

        String nombreNormalizado = nombre.trim();
        String apellidoNormalizado = apellido.trim();

        return buscarPorNombreApellido(nombreNormalizado, apellidoNormalizado);
    }

    /**
     * Elimina un contacto por ID.
     *
     * @param id ID del contacto.
     * @return true si se eliminó, false si no existía.
     */
    public boolean eliminarContactoPorId(int id) {
        int index = buscarIndicePorId(id);
        if (index == NOT_FOUND_INDEX) {
            System.out.println(MSG_DELETE_NOT_FOUND);
            return false;
        }

        contactos.remove(index);
        System.out.println(MSG_DELETE_OK);
        return true;
    }

    public boolean agendaLlena() {
        return contactos.size() >= capacidadMaxima;
    }

    public int espacioLibres() {
        return capacidadMaxima - contactos.size();
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    // -------------------------
    // 5) Helpers (búsquedas y validaciones internas)
    // -------------------------

    private static int clampCapacity(int valor) {
        return Math.max(MIN_CAPACITY, valor);
    }

    /**
     * Busca un contacto por nombre + apellido (case-insensitive).
     * Asume que los inputs ya vienen normalizados con trim().
     */
    private Contacto buscarPorNombreApellido(String nombreNormalizado, String apellidoNormalizado) {
        for (Contacto contacto : contactos) {
            if (contacto.getNombre().equalsIgnoreCase(nombreNormalizado)
                    && contacto.getApellido().equalsIgnoreCase(apellidoNormalizado)) {
                return contacto;
            }
        }
        return null;
    }

    private static final int NOT_FOUND_INDEX = -1;

    private int buscarIndicePorId(int id) {
        for (int i = 0; i < contactos.size(); i++) {
            if (contactos.get(i).getId() == id) {
                return i;
            }
        }
        return NOT_FOUND_INDEX;
    }
}
