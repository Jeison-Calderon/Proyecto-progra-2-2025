package Aplicacion;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class GraficaHabitaciones {

    private TextField txtCodigo, txtEstilo, txtPrecio;
    private TextArea txtResultado;
    private PrintWriter writer;
    private BufferedReader reader;

    public GraficaHabitaciones() {
        conectarAlServidor();
    }

    public GridPane getVista() {
        txtCodigo = new TextField();
        txtEstilo = new TextField();
        txtPrecio = new TextField();
        txtResultado = new TextArea();
        txtResultado.setEditable(false);

        Button btnRegistrar = new Button("Registrar");
        btnRegistrar.setOnAction(e -> registrarHabitacion());

        // Puedes agregar botones de modificar, eliminar, listar como en hoteles

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        grid.add(new Label("Código (opcional):"), 0, 0);
        grid.add(txtCodigo, 1, 0);
        grid.add(new Label("Estilo:"), 0, 1);
        grid.add(txtEstilo, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(txtPrecio, 1, 2);
        grid.add(btnRegistrar, 1, 3);
        grid.add(txtResultado, 0, 4, 2, 1);

        return grid;
    }

    private void conectarAlServidor() {
        try {
            Socket socket = new Socket("localhost", 9999);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            txtResultado.appendText("Error al conectar: " + e.getMessage());
        }
    }

    private void registrarHabitacion() {
        String estilo = txtEstilo.getText().trim();
        String precio = txtPrecio.getText().trim();

        if (estilo.isEmpty() || precio.isEmpty()) {
            txtResultado.appendText("Todos los campos son obligatorios.\n");
            return;
        }

        JSONObject habitacion = new JSONObject();
        habitacion.put("estilo", estilo);
        habitacion.put("precio", precio);

        JSONObject request = new JSONObject();
        request.put("operacion", "crearHabitacion");
        request.put("habitacion", habitacion);

        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);
            txtResultado.appendText("✔ " + respuesta.getString("mensaje") + "\n");
        } catch (IOException e) {
            txtResultado.appendText("Error: " + e.getMessage() + "\n");
        }
    }
}
