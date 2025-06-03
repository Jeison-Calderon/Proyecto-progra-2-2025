package aplicacion.grafica;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class GraficaHabitaciones {
    private final String codigoHotel;
    private TextField txtEstilo, txtPrecio;
    private TextArea txtResultado;
    private TableView<ObservableList<String>> tablaHabitaciones;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String host = "localhost";
    private int puerto = 9999;


    public GraficaHabitaciones(String codigoHotel) {
        this.codigoHotel = codigoHotel;
        conectarServidor();
    }

    public VBox getVista() {
        Label lblEstilo = new Label("Estilo:");
        txtEstilo = new TextField();

        Label lblPrecio = new Label("Precio:");
        txtPrecio = new TextField();

        Button btnRegistrar = new Button("Registrar Habitación");
        btnRegistrar.setOnAction(e -> registrarHabitacion());

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setWrapText(true);

        GridPane formulario = new GridPane();
        formulario.setPadding(new Insets(10));
        formulario.setVgap(10);
        formulario.setHgap(10);

        formulario.add(new Label("Hotel Código: " + codigoHotel), 0, 0, 2, 1);
        formulario.add(lblEstilo, 0, 1); formulario.add(txtEstilo, 1, 1);
        formulario.add(lblPrecio, 0, 2); formulario.add(txtPrecio, 1, 2);
        formulario.add(btnRegistrar, 1, 3);
        formulario.add(txtResultado, 0, 4, 2, 1);

        tablaHabitaciones = new TableView<>();
        agregarMenuContextual();

        VBox root = new VBox(10, formulario, new Label("Habitaciones existentes:"), tablaHabitaciones);
        root.setPadding(new Insets(10));

        listarHabitaciones();

        return root;
    }

    private void conectarServidor() {
        try {
            socket = new Socket("localhost", 9999);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registrarHabitacion() {
        String estilo = txtEstilo.getText().trim();
        String precioStr = txtPrecio.getText().trim();

        if (estilo.isEmpty() || precioStr.isEmpty()) {
            txtResultado.appendText("Todos los campos son obligatorios.\n");
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);

            JSONObject habitacion = new JSONObject();
            habitacion.put("estilo", estilo);
            habitacion.put("precio", precio);
            habitacion.put("codigoHotel", codigoHotel);

            JSONObject request = new JSONObject();
            request.put("operacion", "crearHabitacion");
            request.put("habitacion", habitacion);

            enviarPeticion(request);
            listarHabitaciones();

        } catch (NumberFormatException e) {
            txtResultado.appendText("Precio debe ser numérico válido.\n");
        }
    }

    private void listarHabitaciones() {
        try {
            JSONObject request = new JSONObject();
            request.put("operacion", "listarHabitaciones");
            request.put("codigoHotel", codigoHotel);

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
                        JSONObject h = habitaciones.getJSONObject(i);
                        ObservableList<String> fila = FXCollections.observableArrayList();
                        for (String key : h.keySet()) {
                            fila.add(h.get(key).toString());
                        }
                        tablaHabitaciones.getItems().add(fila);
                    }
                }
            }
        } catch (IOException e) {
            txtResultado.appendText("Error al cargar habitaciones: " + e.getMessage() + "\n");
        }
    }

    private void agregarMenuContextual() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem modificar = new MenuItem("Modificar estilo");
        MenuItem editar = new MenuItem("Editar");
        MenuItem borrar = new MenuItem("Borrar habitación");

        modificar.setOnAction(e -> {
            ObservableList<String> fila = tablaHabitaciones.getSelectionModel().getSelectedItem();
            if (fila == null) return;
            TextInputDialog dialog = new TextInputDialog(fila.get(1));
            dialog.setHeaderText("Modificar estilo:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(nuevo -> {
                JSONObject req = new JSONObject();
                JSONObject hab = new JSONObject();
                hab.put("id", fila.get(0));
                hab.put("estilo", nuevo);
                hab.put("precio", fila.get(2));
                hab.put("codigoHotel", codigoHotel);
                req.put("operacion", "modificarHabitacion");
                req.put("habitacion", hab);
                enviarPeticion(req);
                listarHabitaciones();
            });
        });

        editar.setOnAction(e -> {
            ObservableList<String> fila = tablaHabitaciones.getSelectionModel().getSelectedItem();
            if (fila == null) return;
            TextInputDialog dialog = new TextInputDialog(fila.get(2));
            dialog.setHeaderText("Editar:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(nuevo -> {
                JSONObject req = new JSONObject();
                JSONObject hab = new JSONObject();
                hab.put("id", fila.get(0));
                hab.put("estilo", fila.get(1));
                hab.put("precio", nuevo);
                hab.put("codigoHotel", codigoHotel);
                req.put("operacion", "modificarHabitacion");
                req.put("habitacion", hab);
                enviarPeticion(req);
                listarHabitaciones();
            });
        });

        borrar.setOnAction(e -> {
            ObservableList<String> fila = tablaHabitaciones.getSelectionModel().getSelectedItem();
            if (fila == null) return;
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("¿Eliminar habitación?");
            alert.setContentText("ID: " + fila.get(0));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                JSONObject req = new JSONObject();
                req.put("operacion", "eliminarHabitacion");
                req.put("id", fila.get(0));
                enviarPeticion(req);
                listarHabitaciones();
            }
        });

        contextMenu.getItems().addAll(modificar, editar, borrar);
        tablaHabitaciones.setContextMenu(contextMenu);
    }

    private void enviarPeticion(JSONObject request) {
        try (
                Socket socket = new Socket(host, puerto);  // Variables de conexión que debes tener definidas
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Enviar la petición
            writer.println(request.toString());

            // Leer la respuesta
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            // Procesar la respuesta
            if (respuesta.getString("estado").equals("ok")) {
                txtResultado.appendText("✔ " + respuesta.getString("mensaje") + "\n");
                txtEstilo.clear();
                txtPrecio.clear();
            } else {
                txtResultado.appendText("✘ Error: " + respuesta.getString("mensaje") + "\n");
            }

        } catch (IOException e) {
            txtResultado.appendText("Error de comunicación: " + e.getMessage() + "\n");
        }
    }

}