package Aplicacion;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class GraficaHotel {

    private TextField txtNombre;
    private TextField txtUbicacion;
    private TextField txtCodigo;
    private TextArea txtResultado;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public GraficaHotel() {
        conectarAlServidor();
    }

    public GridPane getVista() {
        Label lblCodigo = new Label("Código (para modificar/eliminar):");
        txtCodigo = new TextField();

        Label lblNombre = new Label("Nombre del Hotel:");
        txtNombre = new TextField();

        Label lblUbicacion = new Label("Ubicación:");
        txtUbicacion = new TextField();

        Button btnRegistrar = new Button("Registrar");
        btnRegistrar.setOnAction(e -> registrarHotel());

        Button btnListar = new Button("Buscar");
        btnListar.setOnAction(e -> listarHoteles());

        Button btnModificar = new Button("Modificar");
        btnModificar.setOnAction(e -> modificarHotel());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarHotel());

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        grid.add(lblCodigo, 0, 0); grid.add(txtCodigo, 1, 0);
        grid.add(lblNombre, 0, 1); grid.add(txtNombre, 1, 1);
        grid.add(lblUbicacion, 0, 2); grid.add(txtUbicacion, 1, 2);
        grid.add(btnRegistrar, 0, 3);
        grid.add(btnModificar, 1, 3);
        grid.add(btnEliminar, 0, 4);
        grid.add(btnListar, 1, 4);
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

    private void listarHoteles() {
        JSONObject request = new JSONObject();
        request.put("operacion", "listarHoteles");
        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                JSONArray hoteles = respuesta.getJSONArray("hoteles");
                txtResultado.appendText("Lista de Hoteles:\n");
                for (int i = 0; i < hoteles.length(); i++) {
                    JSONObject hotel = hoteles.getJSONObject(i);
                    txtResultado.appendText(
                            hotel.getString("codigo") + " - " +
                                    hotel.getString("nombre") + " (" +
                                    hotel.getString("ubicacion") + ")\n"
                    );
                }
            } else {
                txtResultado.appendText("Error: " + respuesta.getString("mensaje") + "\n");
            }

        } catch (IOException e) {
            txtResultado.appendText("Error al listar hoteles: " + e.getMessage() + "\n");
        }
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
                txtCodigo.clear();
                txtNombre.clear();
                txtUbicacion.clear();
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
