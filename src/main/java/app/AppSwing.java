package app;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * DOCUMENTACIÓN PARA EL EQUIPO
 * UI principal de la Agenda de Contactos usando Swing.
 * Responsabilidad: construir la interfaz y orquestar acciones (agregar, buscar, eliminar, listar, limpiar).
 * Nota: La lógica de negocio vive en Agenda/Contacto; aquí solo coordinamos la UI.
 */
public class AppSwing {

    // -------------------------
    // 1) Configuración
    // -------------------------

    private static final String APP_TITLE = "Agenda de Contactos (Swing)";
    private static final int WINDOW_WIDTH = 820;
    private static final int WINDOW_HEIGHT = 520;

    private static final int ROOT_GAP = 12;
    private static final int FORM_PADDING = 12;

    private static final int FIELD_COLUMNS_NAME = 12;
    private static final int FIELD_COLUMNS_ID = 8;

    private static final String LABEL_NOMBRE = "Nombre (1 palabra):";
    private static final String LABEL_APELLIDO = "Apellido (1 palabra):";
    private static final String LABEL_TELEFONO = "Teléfono:";
    private static final String LABEL_ID = "ID (para eliminar):";

    private static final String EMOJI_OK = "✅ ";
    private static final String EMOJI_ERROR = "❌ ";
    private static final String EMOJI_SEARCH = "🔎 ";
    private static final String EMOJI_DELETE = "🗑️ ";
    private static final String EMOJI_EMPTY = "📭 ";
    private static final String EMOJI_BOOK = "📒 ";

    private static final String MSG_FALTAN_NOMBRE_APELLIDO = "Escribe nombre y apellido para buscar.";
    private static final String MSG_FALTA_ID = "Escribe un ID para eliminar.";
    private static final String MSG_ID_INVALIDO = "El ID debe ser un número.";

    // -------------------------
    // 2) Estado / Dependencias
    // -------------------------

    private final Agenda agenda;

    private JFrame frame;

    private JTextField nombreField;
    private JTextField apellidoField;
    private JTextField telefonoField;
    private JTextField idField;

    private JTextArea salidaArea;
    private JLabel estadoLabel;

    public AppSwing(Agenda agenda) {
        this.agenda = agenda;
        inicializarUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Agenda agenda = new Agenda(); // o new Agenda(20)
            new AppSwing(agenda).mostrar();
        });
    }

    // -------------------------
    // 3) Lógica principal (UI bootstrap)
    // -------------------------

    private void inicializarUI() {
        frame = new JFrame(APP_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(ROOT_GAP, ROOT_GAP));
        root.setBorder(BorderFactory.createEmptyBorder(FORM_PADDING, FORM_PADDING, FORM_PADDING, FORM_PADDING));

        root.add(crearPanelFormulario(), BorderLayout.NORTH);
        root.add(crearPanelSalida(), BorderLayout.CENTER);
        root.add(crearPanelEstado(), BorderLayout.SOUTH);

        frame.setContentPane(root);
        actualizarEstado();
    }

    public void mostrar() {
        frame.setVisible(true);
        escribirSalida("Agenda creada. Capacidad: " + agenda.getCapacidadMaxima());
    }

    // -------------------------
    // 4) Construcción UI
    // -------------------------

    private JPanel crearPanelFormulario() {
        // 4.1) Campos
        nombreField = new JTextField(FIELD_COLUMNS_NAME);
        apellidoField = new JTextField(FIELD_COLUMNS_NAME);
        telefonoField = new JTextField(FIELD_COLUMNS_NAME);
        idField = new JTextField(FIELD_COLUMNS_ID);

        // 4.2) Layout con GridBag (flexible y estándar en Swing)
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = crearGbcBase();

        // Fila 0: labels + fields
        gbc.gridy = 0;
        agregarCampo(panel, gbc, 0, LABEL_NOMBRE, nombreField);
        agregarCampo(panel, gbc, 2, LABEL_APELLIDO, apellidoField);
        agregarCampo(panel, gbc, 4, LABEL_TELEFONO, telefonoField);
        agregarCampo(panel, gbc, 6, LABEL_ID, idField);

        // Fila 1: botones
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 8;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        panel.add(crearPanelBotones(), gbc);

        return panel;
    }

    private JComponent crearPanelSalida() {
        salidaArea = new JTextArea();
        salidaArea.setEditable(false);
        salidaArea.setLineWrap(true);
        salidaArea.setWrapStyleWord(true);

        return new JScrollPane(salidaArea);
    }

    private JComponent crearPanelEstado() {
        estadoLabel = new JLabel();
        return estadoLabel;
    }

    private JPanel crearPanelBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        botones.add(crearBoton("Añadir", this::onAgregar));
        botones.add(crearBoton("Buscar", this::onBuscar));
        botones.add(crearBoton("Eliminar por ID", this::onEliminarPorId));
        botones.add(crearBoton("Listar", this::onListar));
        botones.add(crearBoton("Limpiar", this::onLimpiar));

        return botones;
    }

    // -------------------------
    // 5) Acciones (event handlers)
    // -------------------------

    /**
     * Agrega un contacto usando nombre, apellido y teléfono.
     * Edge cases:
     * - Validaciones profundas (p.ej. formato de teléfono, nombre vacío) suelen vivir en Contacto/Agenda.
     * - Duplicado o agenda llena: agenda.anadirContacto devuelve false.
     */
    private void onAgregar() {
        String nombre = leerTexto(nombreField);
        String apellido = leerTexto(apellidoField);
        String telefono = leerTexto(telefonoField);

        try {
            Contacto contacto = new Contacto(nombre, apellido, telefono);
            boolean agregado = agenda.anadirContacto(contacto);

            escribirSalida(agregado
                    ? EMOJI_OK + "Añadido: " + contacto
                    : EMOJI_ERROR + "No se pudo añadir (revisar consola para motivo)."
            );

            actualizarEstado();

        } catch (IllegalArgumentException ex) {
            mostrarDialogo("Error: " + ex.getMessage());
        }
    }

    /**
     * Busca un contacto por nombre + apellido y, si existe, rellena teléfono e ID.
     */
    private void onBuscar() {
        String nombre = leerTexto(nombreField);
        String apellido = leerTexto(apellidoField);

        if (nombre.isEmpty() || apellido.isEmpty()) {
            mostrarDialogo(MSG_FALTAN_NOMBRE_APELLIDO);
            return;
        }

        Contacto encontrado = agenda.buscaContacto(nombre, apellido);
        if (encontrado == null) {
            escribirSalida(EMOJI_SEARCH + "No encontrado: " + nombre + " " + apellido);
            return;
        }

        escribirSalida(EMOJI_SEARCH + "Encontrado: " + encontrado);
        telefonoField.setText(encontrado.getTelefono());
        idField.setText(String.valueOf(encontrado.getId()));
    }

    /**
     * Elimina un contacto por ID.
     * Edge cases:
     * - ID vacío o no numérico: se notifica.
     */
    private void onEliminarPorId() {
        String textoId = leerTexto(idField);

        if (textoId.isEmpty()) {
            mostrarDialogo(MSG_FALTA_ID);
            return;
        }

        Integer id = parsearEnteroSeguro(textoId);
        if (id == null) {
            mostrarDialogo(MSG_ID_INVALIDO);
            return;
        }

        boolean eliminado = agenda.eliminarContactoPorId(id);

        escribirSalida(eliminado
                ? EMOJI_DELETE + "Eliminado ID: " + id
                : EMOJI_ERROR + "No existe ID: " + id
        );

        actualizarEstado();
    }

    /**
     * Lista todos los contactos en el área de salida.
     */
    private void onListar() {
        List<Contacto> contactos = agenda.listarContactos();

        if (contactos.isEmpty()) {
            escribirSalida(EMOJI_EMPTY + "Agenda vacía.");
            return;
        }

        escribirSalida(construirListadoContactos(contactos));
    }

    /**
     * Limpia inputs, salida y actualiza el estado.
     */
    private void onLimpiar() {
        limpiarTextField(nombreField);
        limpiarTextField(apellidoField);
        limpiarTextField(telefonoField);
        limpiarTextField(idField);

        salidaArea.setText("");
        actualizarEstado();
    }

    // -------------------------
    // 6) Helpers UI (estado/salida)
    // -------------------------

    private void actualizarEstado() {
        estadoLabel.setText(
                "Espacios libres: " + agenda.espacioLibres()
                        + " / " + agenda.getCapacidadMaxima()
                        + " | ¿Llena? " + (agenda.agendaLlena() ? "Sí" : "No")
        );
    }

    private void escribirSalida(String mensaje) {
        salidaArea.append(mensaje + "\n");
    }

    private void mostrarDialogo(String mensaje) {
        JOptionPane.showMessageDialog(frame, mensaje, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    // -------------------------
    // 7) Helpers de construcción (DRY)
    // -------------------------

    private GridBagConstraints crearGbcBase() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    /**
     * Agrega un label + field en GridBag siguiendo el patrón (label en col X, field en col X+1).
     */
    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int col, String labelText, JTextField field) {
        gbc.gridx = col;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = col + 1;
        panel.add(field, gbc);
    }

    private JButton crearBoton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private String leerTexto(JTextField field) {
        String text = field.getText();
        return text == null ? "" : text.trim();
    }

    private Integer parsearEnteroSeguro(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String construirListadoContactos(List<Contacto> contactos) {
        StringBuilder sb = new StringBuilder(EMOJI_BOOK).append("Contactos:\n");
        for (Contacto c : contactos) {
            sb.append("- ").append(c).append("\n");
        }
        return sb.toString();
    }

    private void limpiarTextField(JTextField field) {
        field.setText("");
    }
}

