package aplicacion.grafica;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONObject;
import aplicacion.cliente.ClienteSocket;

public class MenuHuesped {
    private BorderPane vista;
    private final String usuario;
    private ConsultaDisponibilidad consultaDisponibilidad;
    private Reservas misReservas;

    public MenuHuesped(String usuario) {
        this.usuario = usuario;
        this.vista = new BorderPane();
        this.consultaDisponibilidad = new ConsultaDisponibilidad();
        this.misReservas = new Reservas(usuario);
        inicializarVista();
    }

    private void inicializarVista() {
        // Barra superior
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #343a40;");
        header.setPadding(new Insets(10));

        Label lblBienvenida = new Label("¡Bienvenido, " + usuario + "!");
        lblBienvenida.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        HBox menuBar = new HBox(10);
        menuBar.setAlignment(Pos.CENTER);

        // Botones de navegación
        Button btnConsultarDisponibilidad = new Button("Consultar Disponibilidad");
        Button btnMisReservas = new Button("Mis Reservas");
        Button btnNuevaReserva = new Button("Nueva Reserva");
        Button btnCerrarSesion = new Button("Cerrar Sesión");

        // Estilos de botones
        String estiloBoton = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;";
        btnConsultarDisponibilidad.setStyle(estiloBoton);
        btnMisReservas.setStyle(estiloBoton);
        btnNuevaReserva.setStyle(estiloBoton);
        btnCerrarSesion.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        menuBar.getChildren().addAll(btnConsultarDisponibilidad, btnMisReservas, btnNuevaReserva, btnCerrarSesion);
        header.getChildren().addAll(lblBienvenida, menuBar);

        // Contenido inicial
        TabPane tabPane = new TabPane();

        Tab tabConsulta = new Tab("Consultar Disponibilidad");
        tabConsulta.setContent(consultaDisponibilidad.getVista());
        tabConsulta.setClosable(false);

        Tab tabMisReservas = new Tab("Mis Reservas");
        tabMisReservas.setContent(misReservas.getVista());
        tabMisReservas.setClosable(false);

        tabPane.getTabs().addAll(tabConsulta, tabMisReservas);

        // Acciones de botones
        btnConsultarDisponibilidad.setOnAction(e -> tabPane.getSelectionModel().select(0));
        btnMisReservas.setOnAction(e -> tabPane.getSelectionModel().select(1));
        btnNuevaReserva.setOnAction(e -> mostrarNuevaReserva());
        btnCerrarSesion.setOnAction(e -> cerrarSesion());

        // Footer
        Label lblFooter = new Label("Sistema de Reservas Hoteleras - Sesión de Huésped");
        lblFooter.setStyle("-fx-text-fill: #6c757d;");
        BorderPane footer = new BorderPane(lblFooter);
        footer.setPadding(new Insets(10));
        footer.setStyle("-fx-background-color: #f8f9fa;");

        // Configurar layout
        vista.setTop(header);
        vista.setCenter(tabPane);
        vista.setBottom(footer);
    }

    private void mostrarNuevaReserva() {
        NuevaReserva ventanaReserva = new NuevaReserva();
        ventanaReserva.showAndWait();

        if (ventanaReserva.isReservaCreada()) {
            // Actualizar la lista de reservas
            misReservas.actualizarReservas();
        }
    }

    private void cerrarSesion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("¿Qué desea hacer?");
        alert.setContentText("Seleccione una opción:");

        // Crear botones personalizados
        ButtonType btnCerrarSesion = new ButtonType("Cerrar Sesión", ButtonBar.ButtonData.LEFT);
        ButtonType btnSalir = new ButtonType("Salir del Programa", ButtonBar.ButtonData.RIGHT);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnCerrarSesion, btnSalir, btnCancelar);

        // Personalizar el diálogo
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f8f9fa;");

        // Obtener y personalizar los botones - Versión corregida
        dialogPane.lookupButton(btnCerrarSesion)
                .setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        dialogPane.lookupButton(btnSalir)
                .setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

        alert.showAndWait().ifPresent(response -> {
            try {
                if (response == btnCerrarSesion) {
                    // Enviar operación de cierre de sesión al servidor
                    JSONObject datos = new JSONObject();
                    datos.put("usuario", usuario);
                    new ClienteSocket().enviarOperacion("CERRAR_SESION", datos.toString());

                    // Volver a la pantalla de login
                    Stage stage = (Stage) vista.getScene().getWindow();
                    new LoginView().mostrar(stage, (u, t) -> {});

                } else if (response == btnSalir) {
                    // Enviar operación de cierre de sesión al servidor antes de salir
                    JSONObject datos = new JSONObject();
                    datos.put("usuario", usuario);
                    try {
                        new ClienteSocket().enviarOperacion("CERRAR_SESION", datos.toString());
                    } catch (Exception ex) {
                        System.err.println("Error al notificar cierre de sesión: " + ex.getMessage());
                    }

                    // Cerrar la aplicación
                    Platform.exit();
                    System.exit(0);
                }
            } catch (Exception e) {
                mostrarError("Error", "Error al procesar la operación: " + e.getMessage());
            }
        });
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public BorderPane getVista() {
        return vista;
    }
}