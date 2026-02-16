package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * COMENTARIOS PARA EL EQUIPO
 * UI principal de la Agenda de Contactos usando JavaFX.
 * Responsabilidad: construir la interfaz y orquestar acciones (agregar, buscar, eliminar, listar, limpiar).
 * Nota: La lógica de negocio vive en Agenda/Contacto; aquí solo coordinamos la UI.
 */
public class AppFx extends Application {

    // -------------------------
    // 1) Configuración
    // -------------------------

    private static final String APP_TITLE = "Agenda de Contactos (JavaFX)";
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 550;

    private static final String PROMPT_NOMBRE = "Nombre (1 palabra)";
    private static final String PROMPT_APELLIDO = "Apellido (1 palabra)";
    private static final String PROMPT_TELEFONO = "Teléfono";
    private static final String PROMPT_ID = "ID (eliminar)";

    private static final String EMOJI_OK = "✅ ";
    private static final String EMOJI_ERROR = "❌ ";
    private static final String EMOJI_SEARCH = "🔎 ";
    private static final String EMOJI_DELETE = "🗑️ ";
    private static final String EMOJI_EMPTY = "📭 ";
    private static final String EMOJI_BOOK = "📒 ";

    private final Agenda agenda = new Agenda(); // o new Agenda(20)

    // -------------------------
    // 2) Componentes UI (estado)
    // -------------------------

    private TextField nombreField;
    private TextField apellidoField;
    private TextField telefonoField;
    private TextField idField;

    private TextArea salidaArea;
    private Label estadoLabel;

    public static void main(String[] args) {
        launch(args);
    }

    // -------------------------
    // 3) Lógica principal (inicio)
    // -------------------------

    @Override
    public void start(Stage stage) {
        stage.setTitle(APP_TITLE);

        BorderPane root = construirLayoutPrincipal();
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setScene(scene);
        stage.show();

        actualizarEstado();
        escribirSalida("Agenda creada. Capacidad: " + agenda.getCapacidadMaxima());
    }

    // -------------------------
    // 4) Construcción UI
    // -------------------------

    /**
     * Construye el layout principal (top: formulario, center: salida, bottom: estado).
     */
    private BorderPane construirLayoutPrincipal() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        root.setTop(crearFormulario());
        root.setCenter(crearSalida());
        root.setBottom(crearEstado());

        return root;
    }

    private Pane crearFormulario() {
        // 4.1) Inputs
        nombreField = crearTextField(PROMPT_NOMBRE);
        apellidoField = crearTextField(PROMPT_APELLIDO);
        telefonoField = crearTextField(PROMPT_TELEFONO);
        idField = crearTextField(PROMPT_ID);

        // 4.2) Layout de campos
        GridPane grid = crearGridFormulario();
        agregarFila(grid, 0, "Nombre:", nombreField, "Apellido:", apellidoField, "Teléfono:", telefonoField, "ID:", idField);

        // 4.3) Botones
        Button agregarBtn = crearBoton("Añadir", this::onAgregar);
        Button buscarBtn = crearBoton("Buscar", this::onBuscar);
        Button eliminarBtn = crearBoton("Eliminar por ID", this::onEliminarPorId);
        Button listarBtn = crearBoton("Listar", this::onListar);
        Button limpiarBtn = crearBoton("Limpiar", this::onLimpiar);

        HBox botones = new HBox(10, agregarBtn, buscarBtn, eliminarBtn, listarBtn, limpiarBtn);
        botones.setPadding(new Insets(10, 0, 0, 0));

        // 4.4) Contenedor final + atajos
        VBox panel = new VBox(10, grid, botones);
        configurarAtajosTeclado(panel);

        return panel;
    }

    private Pane crearSalida() {
        salidaArea = new TextArea();
        salidaArea.setEditable(false);
        salidaArea.setWrapText(true);
        salidaArea.setPrefRowCount(18);

        VBox box = new VBox(10, new Label("Salida:"), salidaArea);
        VBox.setVgrow(salidaArea, Priority.ALWAYS);
        return box;
    }

    private Pane crearEstado() {
        estadoLabel = new Label();

        BorderPane wrapper = new BorderPane();
        wrapper.setPadding(new Insets(10, 0, 0, 0));
        wrapper.setLeft(estadoLabel);

        return wrapper;
    }

    // -------------------------
    // 5) Acciones (event handlers)
    // -------------------------

    /**
     * Agrega un contacto usando nombre, apellido y teléfono.
     * Edge cases:
     * - Campos vacíos: se delega a Contacto/Agenda (si allí validan) o se captura IllegalArgumentException.
     * - Duplicado o agenda llena: agenda.anadirContacto devuelve false.
     */
    private void onAgregar() {
        String nombre = leerTexto(nombreField);
        String apellido = leerTexto(apellidoField);
        String telefono = leerTexto(telefonoField);

        try {
            Contacto nuevoContacto = new Contacto(nombre, apellido, telefono);
            boolean agregado = agenda.anadirContacto(nuevoContacto);

            if (!agregado) {
                escribirSalida(EMOJI_ERROR + "No se pudo añadir (duplicado o agenda llena).");
                actualizarEstado();
                return;
            }

            escribirSalida(EMOJI_OK + "Añadido: " + nuevoContacto);
            actualizarEstado();

            // UX: si se agregó correctamente, reflejamos el ID en pantalla
            idField.setText(String.valueOf(nuevoContacto.getId()));

        } catch (IllegalArgumentException ex) {
            mostrarAlertaInformativa("Error al añadir", ex.getMessage());
        }
    }

    /**
     * Busca un contacto por nombre + apellido y, si existe, rellena teléfono e ID.
     */
    private void onBuscar() {
        String nombre = leerTexto(nombreField);
        String apellido = leerTexto(apellidoField);

        if (nombre.isEmpty() || apellido.isEmpty()) {
            mostrarAlertaInformativa("Faltan datos", "Escribe nombre y apellido para buscar.");
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
            mostrarAlertaInformativa("Faltan datos", "Escribe un ID para eliminar.");
            return;
        }

        Integer id = parsearEnteroSeguro(textoId);
        if (id == null) {
            mostrarAlertaInformativa("ID inválido", "El ID debe ser un número.");
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

        String listado = construirListadoContactos(contactos);
        escribirSalida(listado);
    }

    /**
     * Limpia inputs, salida y actualiza el estado.
     */
    private void onLimpiar() {
        nombreField.clear();
        apellidoField.clear();
        telefonoField.clear();
        idField.clear();

        salidaArea.clear();
        actualizarEstado();
    }

    // -------------------------
    // 6) Helpers UI
    // -------------------------

    /**
     * Refresca el texto de estado inferior (capacidad y espacios).
     */
    private void actualizarEstado() {
        String estado = "Espacios libres: " + agenda.espacioLibres()
                + " / " + agenda.getCapacidadMaxima()
                + " | ¿Llena? " + (agenda.agendaLlena() ? "Sí" : "No");

        estadoLabel.setText(estado);
    }

    /**
     * Escribe un mensaje en el área de salida (modo "log" simple).
     */
    private void escribirSalida(String mensaje) {
        salidaArea.appendText(mensaje + "\n");
    }

    /**
     * Muestra un Alert informativo (mantiene tu comportamiento actual).
     */
    private void mostrarAlertaInformativa(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Agenda");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // -------------------------
    // 7) Helpers de construcción (DRY)
    // -------------------------

    private TextField crearTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        return field;
    }

    private Button crearBoton(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.setOnAction(e -> accion.run());
        return boton;
    }

    private GridPane crearGridFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        return grid;
    }

    /**
     * Agrega una fila de pares Label + Control al GridPane.
     * Mantiene el layout original: (label, field) repetido.
     */
    private void agregarFila(GridPane grid, int row, Object... labelAndControlPairs) {
        // Esperamos pares: "Label:", Node, "Label:", Node, ...
        for (int i = 0; i < labelAndControlPairs.length; i += 2) {
            String labelText = (String) labelAndControlPairs[i];
            Control control = (Control) labelAndControlPairs[i + 1];

            int col = i; // 0,2,4,6...
            grid.add(new Label(labelText), col, row);
            grid.add(control, col + 1, row);
        }
    }

    private void configurarAtajosTeclado(Pane panel) {
        // Nota: aquí mantengo tu comportamiento actual (solo ESC limpia).
        // Si luego quieres ENTER para buscar, lo agregamos sin romper nada.
        panel.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) {
                onLimpiar();
            }
        });
    }

    private String leerTexto(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
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
        for (Contacto contacto : contactos) {
            sb.append("- ").append(contacto).append("\n");
        }
        return sb.toString();
    }
}

