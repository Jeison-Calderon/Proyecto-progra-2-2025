package aplicacion.grafica;

import aplicacion.cliente.ClienteSocket;
import aplicacion.dto.ReservaDTO;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import org.json.JSONObject;
import org.json.JSONArray;

public class Reservas {
    private VBox vista;
    private String usuario;
    private TableView<ReservaDTO> tablaReservas;

    public Reservas(String usuario) {
        this.usuario = usuario;
        this.vista = new VBox(10);
        this.vista.setPadding(new Insets(20));
        inicializarVista();
        actualizarReservas();
    }

    private void inicializarVista() {
        Label titulo = new Label("Mis Reservas");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        tablaReservas = new TableView<>();
        // Configurar columnas de la tabla
        // ... (configuración de columnas similar a GestionReservas)

        vista.getChildren().addAll(titulo, tablaReservas);
    }

    public void actualizarReservas() {
        try {
            JSONObject filtro = new JSONObject();
            filtro.put("usuario", usuario);

            String respuesta = new ClienteSocket()
                    .enviarOperacion("LISTAR_RESERVAS_USUARIO", filtro.toString());

            JSONObject jsonRespuesta = new JSONObject(respuesta);
            if ("OK".equals(jsonRespuesta.getString("estado"))) {
                JSONArray reservasJson = jsonRespuesta.getJSONArray("reservas");
                // Convertir JSON a ReservaDTO y actualizar tabla
                // ...
            }
        } catch (Exception e) {
            mostrarError("Error cargando reservas", e.getMessage());
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public VBox getVista() {
        return vista;
    }
}