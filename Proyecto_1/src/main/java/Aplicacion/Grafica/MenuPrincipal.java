package Aplicacion.Grafica;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class MenuPrincipal {

    private ComboBox<String> cbHoteles;
    private TableView<ObservableList<String>> tablaHabitaciones;
    private TextField txtNombre, txtUbicacion, txtCodigo;
    private TextArea txtResultado;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public void mostrarMenu(Stage primaryStage) {
        conectar();

        // Sección: Encabezado
        Label lblEncabezado = new Label("Sistema de Gestión Hotelera");
        lblEncabezado.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        HBox encabezado = new HBox(lblEncabezado);
        encabezado.setPadding(new Insets(10));

        // Sección: ComboBox y botón
        cbHoteles = new ComboBox<>();
        Button btnVerHabitaciones = new Button("Ver Habitaciones");
        btnVerHabitaciones.setOnAction(e -> listarHabitaciones());
        HBox boxSeleccionHotel = new HBox(10, new Label("Hotel:"), cbHoteles, btnVerHabitaciones);
        boxSeleccionHotel.setPadding(new Insets(10));

        // Sección: Tabla de habitaciones
        tablaHabitaciones = new TableView<>();
        HBox tablaBox = new HBox(tablaHabitaciones);
        tablaBox.setPadding(new Insets(10));
        tablaBox.setPrefHeight(250);

        // Sección: CRUD de Hotel
        txtCodigo = new TextField(); txtCodigo.setPromptText("Código");
        txtNombre = new TextField(); txtNombre.setPromptText("Nombre");
        txtUbicacion = new TextField(); txtUbicacion.setPromptText("Ubicación");

        Button btnCrear = new Button("Crear");
        Button btnModificar = new Button("Modificar");
        Button btnEliminar = new Button("Eliminar");
        Button btnListar = new Button("Listar");

        btnCrear.setOnAction(e -> registrarHotel());
        btnModificar.setOnAction(e -> modificarHotel());
        btnEliminar.setOnAction(e -> eliminarHotel());
        btnListar.setOnAction(e -> listarHoteles());

        HBox botones = new HBox(10, btnCrear, btnModificar, btnEliminar, btnListar);
        VBox crudBox = new VBox(10, txtCodigo, txtNombre, txtUbicacion, botones);
        crudBox.setPadding(new Insets(10));
        crudBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");

        // Sección: Resultado
        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setWrapText(true);
        txtResultado.setPrefHeight(120);

        VBox resultadoBox = new VBox(10, new Label("Resultado:"), txtResultado);
        resultadoBox.setPadding(new Insets(10));

        // Root Layout
        VBox root = new VBox(10, encabezado, boxSeleccionHotel, tablaBox, crudBox, resultadoBox);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Gestión de Hoteles");
        primaryStage.setScene(scene);
        primaryStage.show();

        listarHoteles(); // inicializar
    }

    private void conectar() {
        try {
            socket = new Socket("localhost", 9999);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    private void listarHoteles() {
        JSONObject request = new JSONObject();
        request.put("operacion", "listarHoteles");
        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                JSONArray hoteles = respuesta.getJSONArray("hoteles");
                cbHoteles.getItems().clear();
                txtResultado.appendText("Lista de Hoteles:\n");
                for (int i = 0; i < hoteles.length(); i++) {
                    JSONObject hotel = hoteles.getJSONObject(i);
                    cbHoteles.getItems().add(hotel.getString("codigo"));
                    txtResultado.appendText(hotel.getString("codigo") + " - " + hotel.getString("nombre") + " (" + hotel.getString("ubicacion") + ")\n");
                }
            } else {
                txtResultado.appendText("Error: " + respuesta.getString("mensaje") + "\n");
            }

        } catch (IOException e) {
            txtResultado.appendText("Error al listar hoteles: " + e.getMessage() + "\n");
        }
    }

    private void listarHabitaciones() {
        String codigo = cbHoteles.getValue();
        if (codigo == null) {
            txtResultado.appendText("Seleccione un hotel.\n");
            return;
        }

        JSONObject request = new JSONObject();
        request.put("operacion", "listarHabitaciones");
        request.put("codigoHotel", codigo);

        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                JSONArray habitaciones = respuesta.getJSONArray("habitaciones");

                tablaHabitaciones.getItems().clear();
                tablaHabitaciones.getColumns().clear();

                if (habitaciones.length() > 0) {
                    JSONObject first = habitaciones.getJSONObject(0);
                    for (String key : first.keySet()) {
                        TableColumn<ObservableList<String>, String> col = new TableColumn<>(key);
                        final int colIndex = new java.util.ArrayList<>(first.keySet()).indexOf(key);
                        col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(colIndex)));
                        tablaHabitaciones.getColumns().add(col);
                    }

                    for (int i = 0; i < habitaciones.length(); i++) {
                        JSONObject habitacion = habitaciones.getJSONObject(i);
                        ObservableList<String> fila = FXCollections.observableArrayList();
                        for (String key : habitacion.keySet()) {
                            fila.add(habitacion.get(key).toString());
                        }
                        tablaHabitaciones.getItems().add(fila);
                    }
                } else {
                    txtResultado.appendText("No hay habitaciones para este hotel.\n");
                }

            } else {
                txtResultado.appendText("Error: " + respuesta.getString("mensaje") + "\n");
            }

        } catch (IOException e) {
            txtResultado.appendText("Error al obtener habitaciones: " + e.getMessage() + "\n");
        }
    }

    private void registrarHotel() {
        String nombre = txtNombre.getText().trim();
        String ubicacion = txtUbicacion.getText().trim();

        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            txtResultado.appendText("Campos nombre y ubicación obligatorios.\n");
            return;
        }

        JSONObject hotel = new JSONObject();
        hotel.put("nombre", nombre);
        hotel.put("ubicacion", ubicacion);

        JSONObject request = new JSONObject();
        request.put("operacion", "crearHotel");
        request.put("hotel", hotel);

        enviarPeticionYMostrarRespuesta(request);
    }

    private void modificarHotel() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String ubicacion = txtUbicacion.getText().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || ubicacion.isEmpty()) {
            txtResultado.appendText("Debe completar código, nombre y ubicación.\n");
            return;
        }

        JSONObject hotel = new JSONObject();
        hotel.put("codigo", codigo);
        hotel.put("nombre", nombre);
        hotel.put("ubicacion", ubicacion);

        JSONObject request = new JSONObject();
        request.put("operacion", "modificarHotel");
        request.put("hotel", hotel);

        enviarPeticionYMostrarRespuesta(request);
    }

    private void eliminarHotel() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            txtResultado.appendText("Debe ingresar el código del hotel a eliminar.\n");
            return;
        }

        JSONObject request = new JSONObject();
        request.put("operacion", "eliminarHotel");
        request.put("codigo", codigo);

        enviarPeticionYMostrarRespuesta(request);
    }

    private void enviarPeticionYMostrarRespuesta(JSONObject request) {
        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                txtResultado.appendText("✔ " + respuesta.getString("mensaje") + "\n");
                txtCodigo.clear(); txtNombre.clear(); txtUbicacion.clear();
                listarHoteles();
            } else {
                txtResultado.appendText("✘ Error: " + respuesta.getString("mensaje") + "\n");
            }

        } catch (IOException e) {
            txtResultado.appendText("Error de comunicación: " + e.getMessage() + "\n");
        }
    }

    public void cerrarConexion() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            txtResultado.appendText("Error cerrando conexión: " + e.getMessage());
        }
    }
}
