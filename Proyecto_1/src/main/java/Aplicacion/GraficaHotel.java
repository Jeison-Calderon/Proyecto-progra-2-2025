package Aplicacion;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

import org.json.JSONObject;

public class GraficaHotel extends Application {

    private TextField txtNombre;
    private TextField txtUbicacion;
    private TextArea txtResultado;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Registro de Hoteles");

        // Formulario
        Label lblNombre = new Label("Nombre del Aplicación.Servidor.Hotel:");
        txtNombre = new TextField();

        Label lblUbicacion = new Label("Ubicación:");
        txtUbicacion = new TextField();

        Button btnEnviar = new Button("Registrar Aplicación.Servidor.Hotel");
        btnEnviar.setOnAction(e -> registrarHotel());

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setWrapText(true);

        // Layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(lblNombre, 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(lblUbicacion, 0, 1);
        grid.add(txtUbicacion, 1, 1);
        grid.add(btnEnviar, 1, 2);
        grid.add(txtResultado, 0, 3, 2, 1);

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        conectarAlServidor(); // Al iniciar, conectarse
    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localhost", 9999);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            txtResultado.appendText("Conectado al servidor\n");
        } catch (IOException e) {
            txtResultado.appendText("Error al conectar al servidor: " + e.getMessage() + "\n");
        }
    }

    private void registrarHotel() {
        String nombre = txtNombre.getText().trim();
        String ubicacion = txtUbicacion.getText().trim();

        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            txtResultado.appendText("Todos los campos son obligatorios.\n");
            return;
        }

        try {
            // Construir JSON para enviar al servidor
            JSONObject hotel = new JSONObject();
            hotel.put("nombre", nombre);
            hotel.put("ubicacion", ubicacion);

            JSONObject request = new JSONObject();
            request.put("operacion", "crearHotel");
            request.put("hotel", hotel);

            writer.println(request.toString());

            // Leer respuesta del servidor
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                txtResultado.appendText("✔ " + respuesta.getString("mensaje") + "\n");
                txtNombre.clear();
                txtUbicacion.clear();
            } else {
                txtResultado.appendText("✘ Error: " + respuesta.getString("mensaje") + "\n");
            }

        } catch (IOException e) {
            txtResultado.appendText("Error de comunicación: " + e.getMessage() + "\n");
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (writer != null) writer.close();
        if (reader != null) reader.close();
        if (socket != null && !socket.isClosed()) socket.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}