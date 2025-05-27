package Aplicacion.Grafica;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

public class MenuPrincipal {

    private ComboBox<String> cbHoteles;
    private PrintWriter writer;
    private BufferedReader reader;
    private TextField txtCodigo, txtNombre, txtUbicacion;
    private TextArea txtResultado;

    public BorderPane getVista() {
        conectar();

        BorderPane root = new BorderPane();
        TabPane tabPane = new TabPane();
        root.setCenter(tabPane);

        VBox crudBox = construirFormularioCRUD();

        Label lblTitulo = new Label("Seleccione un hotel para gestionar habitaciones");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");

        cbHoteles = new ComboBox<>();
        Button btnIr = new Button("Abrir pestaña de hotel");
        btnIr.setOnAction(e -> abrirPestanaHotel(tabPane));

        HBox boxSeleccion = new HBox(10, new Label("Hotel:"), cbHoteles, btnIr);
        boxSeleccion.setPadding(new Insets(10));

        VBox panelSeleccion = new VBox(10, lblTitulo, boxSeleccion);
        panelSeleccion.setPadding(new Insets(10));

        HBox topPanel = new HBox(30, panelSeleccion, crudBox);
        root.setTop(topPanel);

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setWrapText(true);
        txtResultado.setPrefHeight(100);
        root.setBottom(txtResultado);

        cargarHoteles();
        return root;
    }

    private VBox construirFormularioCRUD() {
        txtCodigo = new TextField();
        txtCodigo.setPromptText("Código");
        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");
        txtUbicacion = new TextField();
        txtUbicacion.setPromptText("Ubicación");

        Button btnCrear = new Button("Crear");
        btnCrear.setOnAction(e -> registrarHotel());
        Button btnModificar = new Button("Modificar");
        btnModificar.setOnAction(e -> modificarHotel());
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarHotel());
        Button btnListar = new Button("Listar");
        btnListar.setOnAction(e -> cargarHoteles());

        HBox botones = new HBox(10, btnCrear, btnModificar, btnEliminar, btnListar);
        VBox crud = new VBox(10, new Label("CRUD de Hotel:"), txtCodigo, txtNombre, txtUbicacion, botones);
        crud.setPadding(new Insets(10));
        crud.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");

        return crud;
    }

    private void abrirPestanaHotel(TabPane tabPane) {
        String codigoHotel = cbHoteles.getValue();
        if (codigoHotel == null) return;

        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals(codigoHotel)) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }

        TableView<ObservableList<String>> tabla = new TableView<>();
        agregarMenuContextual(tabla, codigoHotel);

        Tab tabHotel = new Tab(codigoHotel, tabla);
        tabHotel.setClosable(true);
        tabPane.getTabs().add(tabHotel);
        tabPane.getSelectionModel().select(tabHotel);

        cargarHabitaciones(codigoHotel, tabla);
    }

    private void agregarMenuContextual(TableView<ObservableList<String>> tabla, String codigoHotel) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem registrar = new MenuItem("Registrar habitación");
        MenuItem editar = new MenuItem("Editar precio");
        MenuItem borrar = new MenuItem("Borrar habitación");

        registrar.setOnAction(e -> registrarHabitacion(codigoHotel, tabla));

        editar.setOnAction(e -> {
            ObservableList<String> fila = tabla.getSelectionModel().getSelectedItem();
            if (fila == null) return;
            TextInputDialog dialog = new TextInputDialog(fila.get(2));
            dialog.setHeaderText("Editar precio:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(nuevo -> {
                JSONObject request = new JSONObject();
                JSONObject hab = new JSONObject();
                hab.put("codigoHotel", codigoHotel);
                hab.put("id", fila.get(0));
                hab.put("estilo", fila.get(1));
                hab.put("precio", nuevo);
                request.put("operacion", "modificarHabitacion");
                request.put("habitacion", hab);
                enviarPeticion(request);
                cargarHabitaciones(codigoHotel, tabla);
            });
        });

        borrar.setOnAction(e -> {
            ObservableList<String> fila = tabla.getSelectionModel().getSelectedItem();
            if (fila == null) return;
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("¿Eliminar habitación?");
            alert.setContentText("ID: " + fila.get(0));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                JSONObject request = new JSONObject();
                request.put("operacion", "eliminarHabitacion");
                request.put("id", fila.get(0));
                enviarPeticion(request);
                cargarHabitaciones(codigoHotel, tabla);
            }
        });

        contextMenu.getItems().addAll(registrar, editar, borrar);
        tabla.setContextMenu(contextMenu);
    }

    private void registrarHabitacion(String codigoHotel, TableView<ObservableList<String>> tabla) {
        TextInputDialog dialogEstilo = new TextInputDialog();
        dialogEstilo.setHeaderText("Ingrese el estilo de la habitación:");
        Optional<String> optEstilo = dialogEstilo.showAndWait();
        if (!optEstilo.isPresent()) return;

        TextInputDialog dialogPrecio = new TextInputDialog();
        dialogPrecio.setHeaderText("Ingrese el precio de la habitación:");
        Optional<String> optPrecio = dialogPrecio.showAndWait();
        if (!optPrecio.isPresent()) return;

        String estilo = optEstilo.get().trim();
        String precioStr = optPrecio.get().trim();

        if (estilo.isEmpty() || precioStr.isEmpty()) {
            txtResultado.appendText("Todos los campos son obligatorios.\n");
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);

            JSONObject habitacion = new JSONObject();
            habitacion.put("id", UUID.randomUUID().toString().substring(0, 8));
            habitacion.put("estilo", estilo);
            habitacion.put("precio", precio);
            habitacion.put("codigoHotel", codigoHotel);

            JSONObject request = new JSONObject();
            request.put("operacion", "crearHabitacion");
            request.put("habitacion", habitacion);

            enviarPeticion(request);
            cargarHabitaciones(codigoHotel, tabla);

        } catch (NumberFormatException e) {
            txtResultado.appendText("Precio debe ser numérico válido.\n");
        }
    }

    private void conectar() {
        try {
            Socket socket = new Socket("localhost", 9999);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    private void cargarHoteles() {
        JSONObject request = new JSONObject();
        request.put("operacion", "listarHoteles");
        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                JSONArray hoteles = respuesta.getJSONArray("hoteles");
                cbHoteles.getItems().clear();
                for (int i = 0; i < hoteles.length(); i++) {
                    JSONObject hotel = hoteles.getJSONObject(i);
                    cbHoteles.getItems().add(hotel.getString("codigo"));
                }
            }
        } catch (IOException e) {
            System.out.println("Error al listar hoteles: " + e.getMessage());
        }
    }

    private void cargarHabitaciones(String codigoHotel, TableView<ObservableList<String>> tabla) {
        JSONObject request = new JSONObject();
        request.put("operacion", "listarHabitaciones");
        request.put("codigoHotel", codigoHotel);

        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                JSONArray habitaciones = respuesta.getJSONArray("habitaciones");

                tabla.getItems().clear();
                tabla.getColumns().clear();

                if (habitaciones.length() > 0) {
                    JSONObject first = habitaciones.getJSONObject(0);
                    for (String key : first.keySet()) {
                        TableColumn<ObservableList<String>, String> col = new TableColumn<>(key);
                        final int colIndex = new java.util.ArrayList<>(first.keySet()).indexOf(key);
                        col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(colIndex)));
                        tabla.getColumns().add(col);
                    }

                    for (int i = 0; i < habitaciones.length(); i++) {
                        JSONObject habitacion = habitaciones.getJSONObject(i);
                        ObservableList<String> fila = FXCollections.observableArrayList();
                        for (String key : habitacion.keySet()) {
                            fila.add(habitacion.get(key).toString());
                        }
                        tabla.getItems().add(fila);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar habitaciones: " + e.getMessage());
        }
    }

    private void registrarHotel() {
        String nombre = txtNombre.getText().trim();
        String ubicacion = txtUbicacion.getText().trim();

        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            txtResultado.appendText("Todos los campos son obligatorios.\n");
            return;
        }

        JSONObject hotel = new JSONObject();
        hotel.put("nombre", nombre);
        hotel.put("ubicacion", ubicacion);

        JSONObject request = new JSONObject();
        request.put("operacion", "crearHotel");
        request.put("hotel", hotel);

        enviarPeticion(request);
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

        enviarPeticion(request);
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

        enviarPeticion(request);
    }

    private void enviarPeticion(JSONObject request) {
        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                txtResultado.appendText("✔ " + respuesta.getString("mensaje") + "\n");
                txtCodigo.clear();
                txtNombre.clear();
                txtUbicacion.clear();
                cargarHoteles();
            } else {
                txtResultado.appendText("✘ Error: " + respuesta.getString("mensaje") + "\n");
            }

        } catch (IOException e) {
            txtResultado.appendText("Error de comunicación: " + e.getMessage() + "\n");
        }
    }

}
