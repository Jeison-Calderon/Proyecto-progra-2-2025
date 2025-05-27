package Aplicacion.Grafica;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class GraficaHabitaciones {

    private TextField txtCodigo, txtEstilo, txtPrecio;
    private TextArea txtResultado;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public GraficaHabitaciones() {
        conectarAlServidor();
    }

    public GridPane getVista() {
        Label lblCodigo = new Label("Código (para modificar/eliminar):");
        txtCodigo = new TextField();

        Label lblEstilo = new Label("Estilo:");
        txtEstilo = new TextField();

        Label lblPrecio = new Label("Precio:");
        txtPrecio = new TextField();

        Button btnRegistrar = new Button("Registrar");
        btnRegistrar.setOnAction(e -> registrarHabitacion());

        Button btnModificar = new Button("Modificar");
        btnModificar.setOnAction(e -> modificarHabitacion());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarHabitacion());

        Button btnBuscar = new Button("Buscar");
        btnBuscar.setOnAction(e -> buscarHabitaciones());

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        grid.add(lblCodigo, 0, 0); grid.add(txtCodigo, 1, 0);
        grid.add(lblEstilo, 0, 1); grid.add(txtEstilo, 1, 1);
        grid.add(lblPrecio, 0, 2); grid.add(txtPrecio, 1, 2);
        grid.add(btnRegistrar, 0, 3);
        grid.add(btnModificar, 1, 3);
        grid.add(btnEliminar, 0, 4);
        grid.add(btnBuscar, 1, 4);
        grid.add(txtResultado, 0, 5, 2, 1);

        return grid;
    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localhost", 9999);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            if (txtResultado != null) {
                txtResultado.appendText("Error al conectar al servidor: " + e.getMessage() + "\n");
            }
        }
    }

    private void registrarHabitacion() {
        String estilo = txtEstilo.getText().trim();
        String precio = txtPrecio.getText().trim();

        if (estilo.isEmpty() || precio.isEmpty()) {
            txtResultado.appendText("Campos estilo y precio son obligatorios.\n");
            return;
        }

        JSONObject habitacion = new JSONObject();
        habitacion.put("estilo", estilo);
        habitacion.put("precio", precio);

        JSONObject request = new JSONObject();
        request.put("operacion", "crearHabitacion");
        request.put("habitacion", habitacion);

        enviarPeticionYMostrarRespuesta(request);
    }

    private void buscarHabitaciones() {
        JSONObject request = new JSONObject();
        request.put("operacion", "listarHabitaciones");  // Nombre corregido

        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                JSONArray habitaciones = respuesta.getJSONArray("habitaciones");
                txtResultado.appendText("Lista de Habitaciones:\n");
                for (int i = 0; i < habitaciones.length(); i++) {
                    JSONObject habitacion = habitaciones.getJSONObject(i);
                    txtResultado.appendText(
                            habitacion.getString("codigo") + " - " +
                                    habitacion.getString("estilo") + " ($" +
                                    habitacion.getDouble("precio") + ")\n"
                    );
                }
            } else {
                txtResultado.appendText("Error: " + respuesta.getString("mensaje") + "\n");
            }

        } catch (IOException e) {
            txtResultado.appendText("Error al buscar habitaciones: " + e.getMessage() + "\n");
        }
    }

    private void modificarHabitacion() {
        String codigo = txtCodigo.getText().trim();
        String estilo = txtEstilo.getText().trim();
        String precio = txtPrecio.getText().trim();

        if (codigo.isEmpty() || estilo.isEmpty() || precio.isEmpty()) {
            txtResultado.appendText("Debe completar código, estilo y precio.\n");
            return;
        }

        JSONObject habitacion = new JSONObject();
        habitacion.put("codigo", codigo);
        habitacion.put("estilo", estilo);
        habitacion.put("precio", Double.parseDouble(precio));

        JSONObject request = new JSONObject();
        request.put("operacion", "modificarHabitacion");  // Nombre corregido
        request.put("habitacion", habitacion);

        enviarPeticionYMostrarRespuesta(request);
    }

    private void eliminarHabitacion() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            txtResultado.appendText("Debe ingresar el código de la habitación a eliminar.\n");
            return;
        }

        JSONObject request = new JSONObject();
        request.put("operacion", "eliminarHabitacion");  // Nombre corregido
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
                txtCodigo.clear();
                txtEstilo.clear();
                txtPrecio.clear();
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