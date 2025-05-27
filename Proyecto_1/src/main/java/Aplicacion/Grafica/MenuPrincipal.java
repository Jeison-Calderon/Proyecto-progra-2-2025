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

public class MenuPrincipal {

    private ComboBox<String> cbHoteles;
    private PrintWriter writer;
    private BufferedReader reader;

    public void construirMenu(BorderPane dashboardPane, TabPane tabPane) {
        conectar();

        Label lblTitulo = new Label("Seleccione un hotel para gestionar habitaciones");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");

        cbHoteles = new ComboBox<>();
        Button btnIr = new Button("Abrir pestaña de hotel");
        btnIr.setOnAction(e -> abrirPestanaHotel(tabPane));

        HBox boxSeleccion = new HBox(10, new Label("Hotel:"), cbHoteles, btnIr);
        boxSeleccion.setPadding(new Insets(10));

        VBox panelSeleccion = new VBox(10, lblTitulo, boxSeleccion);
        panelSeleccion.setPadding(new Insets(10));

        dashboardPane.setTop(panelSeleccion);
        cargarHoteles();
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
        Tab tabHotel = new Tab(codigoHotel, tabla);
        tabHotel.setClosable(true);
        tabPane.getTabs().add(tabHotel);
        tabPane.getSelectionModel().select(tabHotel);

        cargarHabitaciones(codigoHotel, tabla);
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
}
