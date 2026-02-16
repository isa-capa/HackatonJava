package app;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DOCUMENTACIÓN PARA EL EQUIPO
 *
 * Representa un contacto dentro de la agenda.
 *
 * Reglas de negocio actuales:
 * - nombre y apellido deben ser una sola palabra (sin espacios) y no pueden estar vacíos.
 * - teléfono es opcional (si llega null se guarda como string vacío).
 * - el ID se autogenera de forma incremental (inicia en 1).
 *
 * Nota: equals/hashCode están basados SOLO en el id (identidad del contacto).
 */
public class Contacto {

    // -------------------------
    // 1) Configuración
    // -------------------------

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    private static final String EMPTY_PHONE = "";

    // Mensajes centralizados para evitar “magical strings”
    private static final String MSG_EMPTY_FIELD = "El %s no puede estar vacío.";
    private static final String MSG_SINGLE_WORD = "El %s debe ser una sola palabra (sin espacios).";

    // -------------------------
    // 2) Estado (inmutable)
    // -------------------------

    private final int id;
    private final String nombre;
    private final String apellido;
    private final String telefono;

    // -------------------------
    // 3) Lógica principal (constructor)
    // -------------------------

    /**
     * Crea un Contacto con ID autogenerado.
     *
     * @param nombre   Nombre del contacto (obligatorio, 1 palabra).
     * @param apellido Apellido del contacto (obligatorio, 1 palabra).
     * @param telefono Teléfono del contacto (opcional, puede ser null).
     * @throws IllegalArgumentException si nombre o apellido son null/vacíos o contienen espacios.
     */
    public Contacto(String nombre, String apellido, String telefono) {
        String nombreNormalizado = normalizarPalabraUnica(nombre, "nombre");
        String apellidoNormalizado = normalizarPalabraUnica(apellido, "apellido");

        this.id = ID_GENERATOR.getAndIncrement();
        this.nombre = nombreNormalizado;
        this.apellido = apellidoNormalizado;
        this.telefono = normalizarTelefono(telefono);
    }

    // -------------------------
    // 4) Getters (API pública)
    // -------------------------

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    // -------------------------
    // 5) Igualdad / Representación
    // -------------------------

    // equals por ID (identidad)
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Contacto)) return false;
        Contacto contacto = (Contacto) other;
        return id == contacto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ID: " + id + " | " + nombre + " " + apellido + " | Tel: " + telefono;
    }

    // -------------------------
    // 6) Helpers (validación/normalización)
    // -------------------------

    /**
     * Valida que el texto sea una sola palabra (sin espacios) y lo regresa normalizado (trim).
     * Usa early returns para fallar rápido con mensajes claros.
     */
    private static String normalizarPalabraUnica(String valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException(String.format(MSG_EMPTY_FIELD, campo));
        }

        String trimmed = valor.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(String.format(MSG_EMPTY_FIELD, campo));
        }

        // Mantengo tu regla original: si contiene espacio, se rechaza.
        // (Ojo: esto también rechaza "De la" o "San José", lo cual es intencional según tu versión.)
        if (trimmed.contains(" ")) {
            throw new IllegalArgumentException(String.format(MSG_SINGLE_WORD, campo));
        }

        return trimmed;
    }

    /**
     * Normaliza el teléfono.
     * Regla actual: null => "", si no => trim(). No valida formato para no cambiar funcionalidad.
     */
    private static String normalizarTelefono(String telefono) {
        return telefono == null ? EMPTY_PHONE : telefono.trim();
    }
}
