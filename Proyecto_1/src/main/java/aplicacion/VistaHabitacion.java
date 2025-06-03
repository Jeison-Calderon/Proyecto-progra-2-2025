package aplicacion;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class VistaHabitacion {

    private TextField txtEstilo;
    private TextField txtPrecio;
    private TextField txtCodigo;
    private TextArea txtResultado;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public VistaHabitacion() {
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
        grid.add(btnRegistrar, 1, 3);
        grid.add(txtResultado, 0, 4, 2, 1);

        return grid;
    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localhost", 9999);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            txtResultado.appendText("Error al conectar al servidor: " + e.getMessage() + "\n");
        }
    }

    private void registrarHabitacion() {
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

            JSONObject request = new JSONObject();
            request.put("operacion", "crearHabitacion");
            request.put("habitacion", habitacion);

            enviarPeticionYMostrarRespuesta(request);
        } catch (NumberFormatException e) {
            txtResultado.appendText("Precio debe ser un número válido.\n");
        }
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