package aplicacion.grafica;

import aplicacion.cliente.AplicacionCliente;
import aplicacion.dto.Usuario.TipoUsuario;
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
    private Button btnCerrarSesion;
    private Stage stage;

    public MenuHuesped(String usuario) {
        this.usuario = usuario;
        this.vista = new BorderPane();
        this.consultaDisponibilidad = new ConsultaDisponibilidad();
        this.misReservas = new Reservas(usuario);
        inicializarVista();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
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
        btnCerrarSesion = new Button("Cerrar Sesión");

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

        // Crear botones personalizados con nombres diferentes para evitar conflictos
        ButtonType btnTipoCerrarSesion = new ButtonType("Cerrar Sesión", ButtonBar.ButtonData.LEFT);
        ButtonType btnTipoSalir = new ButtonType("Salir del Programa", ButtonBar.ButtonData.RIGHT);
        ButtonType btnTipoCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnTipoCerrarSesion, btnTipoSalir, btnTipoCancelar);

        // Personalizar el diálogo
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f8f9fa;");

        // Obtener y personalizar los botones
        dialogPane.lookupButton(btnTipoCerrarSesion)
                .setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        dialogPane.lookupButton(btnTipoSalir)
                .setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

        alert.showAndWait().ifPresent(response -> {
            try {
                if (response == btnTipoCerrarSesion) {
                    // Enviar operación de cierre de sesión al servidor
                    JSONObject datos = new JSONObject();
                    datos.put("usuario", usuario);
                    new ClienteSocket().enviarOperacion("CERRAR_SESION", datos.toString());

                    // Volver a la pantalla de login con el callback correcto
                    Stage currentStage = obtenerStage();
                    if (currentStage != null) {
                        LoginView loginView = new LoginView();
                        loginView.mostrar(currentStage, (u, t) -> {
                            // Recrear la instancia de AplicacionCliente y cargar el menú correspondiente
                            AplicacionCliente app = new AplicacionCliente();
                            if (t == TipoUsuario.RECEPCIONISTA) {
                                app.cargarMenuRecepcionista(currentStage, u);
                            } else {
                                app.cargarMenuHuesped(currentStage, u);
                            }
                        });
                    } else {
                        mostrarError("Error", "No se pudo obtener la ventana actual");
                    }

                } else if (response == btnTipoSalir) {
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

    // Método para obtener el Stage de manera segura
    private Stage obtenerStage() {
        // Primero intentar usar la referencia directa
        if (stage != null) {
            return stage;
        }

        // Si no existe, intentar obtenerlo desde la vista
        try {
            if (vista != null && vista.getScene() != null && vista.getScene().getWindow() != null) {
                return (Stage) vista.getScene().getWindow();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener Stage desde la vista: " + e.getMessage());
        }

        // Como último recurso, intentar obtenerlo desde el botón
        try {
            if (btnCerrarSesion != null && btnCerrarSesion.getScene() != null && btnCerrarSesion.getScene().getWindow() != null) {
                return (Stage) btnCerrarSesion.getScene().getWindow();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener Stage desde el botón: " + e.getMessage());
        }

        return null;
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