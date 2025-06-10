package aplicacion.grafica;

import aplicacion.cliente.AplicacionCliente;
import aplicacion.cliente.ClienteSocket;
import aplicacion.dto.HotelDTO;
import aplicacion.dto.Usuario.TipoUsuario;
import aplicacion.servicio.ServicioHabitaciones;
import aplicacion.servicio.ServicioHoteles;
import aplicacion.util.NotificacionManager;
import aplicacion.util.TabManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.util.Optional;

public class MenuPrincipal {

    private ServicioHoteles servicioHoteles;
    private ServicioHabitaciones servicioHabitaciones;
    private GestorHoteles gestorHoteles;
    private GestorHabitaciones gestorHabitaciones;
    private NotificacionManager notificacionManager;
    private TabManager tabManager;
    private ConsultaDisponibilidad consultaDisponibilidad;
    private GestionReservas gestionReservas;
    private TextArea txtResultado;
    private TabPane tabPane;
    private BorderPane root;
    private String usuario; // Agregar campo para el usuario
    private Stage stage; // Agregar referencia al Stage
    private Button btnCerrarSesion; // Agregar referencia al botón

    public MenuPrincipal() {
        this.servicioHoteles = new ServicioHoteles();
        this.servicioHabitaciones = new ServicioHabitaciones();
        // NO inicializar gestores aquí - esperar a que tabPane esté creado
    }

    // Método para establecer el usuario
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    // Método para establecer la referencia al Stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public BorderPane getVista() {
        root = new BorderPane();

        // Crear TabPane PRIMERO
        tabPane = new TabPane();

        // AHORA inicializar todos los gestores una sola vez
        inicializarGestores();

        // Crear pestañas principales
        crearPestanasPrincipales();

        root.setCenter(tabPane);
        BorderPane header = crearHeader();
        root.setTop(header);
        root.setBottom(txtResultado);
        return root;
    }

    private void inicializarGestores() {
        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setPrefHeight(100);

        notificacionManager = new NotificacionManager(txtResultado);
        tabManager = new TabManager(tabPane);

        gestorHoteles = new GestorHoteles(servicioHoteles, notificacionManager, tabManager);
        gestorHabitaciones = new GestorHabitaciones(servicioHabitaciones, notificacionManager, tabManager);

        consultaDisponibilidad = new ConsultaDisponibilidad();
        gestionReservas = new GestionReservas();

        // Configurar callback DESPUÉS de crear los gestores
        gestorHoteles.setOnVerHabitaciones(this::verHabitacionesHotel);
    }

    private void crearPestanasPrincipales() {
        Tab tabHoteles = new Tab("Gestión de Hoteles");
        tabHoteles.setClosable(false);
        tabHoteles.setContent(gestorHoteles.crearVista());

        Tab tabConsulta = new Tab("Consulta Disponibilidad");
        tabConsulta.setClosable(false);
        tabConsulta.setContent(consultaDisponibilidad.getVista());

        Tab tabReservas = new Tab("Gestión de Reservas");
        tabReservas.setClosable(false);
        tabReservas.setContent(gestionReservas.getVista());

        tabPane.getTabs().addAll(tabHoteles, tabConsulta, tabReservas);
        tabPane.getSelectionModel().selectFirst();
    }

    private BorderPane crearHeader() {
        BorderPane header = new BorderPane();

        // Título con nombre de usuario si está disponible
        String tituloTexto = "Sistema de Gestión de Hoteles - Recepcionista";
        if (usuario != null && !usuario.isEmpty()) {
            tituloTexto += ": " + usuario;
        }

        Label lblHeader = new Label(tituloTexto);
        lblHeader.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.setCenter(lblHeader);

        HBox botonesNavegacion = crearBotonesNavegacion();
        header.setLeft(botonesNavegacion);

        // Cambiar de botón "Salir" a botón "Cerrar Sesión"
        btnCerrarSesion = crearBotonCerrarSesion();
        header.setRight(btnCerrarSesion);

        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #343a40; -fx-text-fill: white;");
        return header;
    }

    private HBox crearBotonesNavegacion() {
        HBox contenedor = new HBox(5);
        contenedor.setStyle("-fx-alignment: center-left;");

        Button btnHoteles = new Button("Hoteles");
        btnHoteles.setStyle(estiloBotonNavegacion());
        btnHoteles.setOnAction(e -> {
            System.out.println("Navegando a Hoteles - Índice 0");
            // Cerrar pestañas temporales y volver a principal
            tabManager.volverAPrincipal();
        });

        Button btnDisponibilidad = new Button("Disponibilidad");
        btnDisponibilidad.setStyle(estiloBotonNavegacion());
        btnDisponibilidad.setOnAction(e -> {
            System.out.println("Navegando a Disponibilidad - Índice 1");
            // Cerrar pestañas temporales primero
            tabManager.cerrarPestanasTemporales();
            tabPane.getSelectionModel().select(1);
        });

        Button btnReservas = new Button("Reservas");
        btnReservas.setStyle(estiloBotonNavegacion());
        btnReservas.setOnAction(e -> {
            System.out.println("Navegando a Reservas - Índice 2");
            // Cerrar pestañas temporales primero
            tabManager.cerrarPestanasTemporales();
            tabPane.getSelectionModel().select(2);
        });

        contenedor.getChildren().addAll(btnHoteles, btnDisponibilidad, btnReservas);
        return contenedor;
    }

    private String estiloBotonNavegacion() {
        return "-fx-background-color: #495057; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 5 10; " +
                "-fx-border-radius: 3; " +
                "-fx-background-radius: 3; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 12px;";
    }

    // Cambiar de crearBotonSalir a crearBotonCerrarSesion
    private Button crearBotonCerrarSesion() {
        Button btnCerrarSesion = new Button("Cerrar Sesión");

        String estiloNormal = "-fx-background-color: #dc3545; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 16; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;";

        String estiloHover = "-fx-background-color: #c82333; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 16; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;";

        btnCerrarSesion.setStyle(estiloNormal);
        btnCerrarSesion.setOnMouseEntered(e -> btnCerrarSesion.setStyle(estiloHover));
        btnCerrarSesion.setOnMouseExited(e -> btnCerrarSesion.setStyle(estiloNormal));
        btnCerrarSesion.setOnAction(e -> cerrarSesion()); // Cambiar a cerrarSesion()

        return btnCerrarSesion;
    }

    private void verHabitacionesHotel(HotelDTO hotel) {
        tabManager.cerrarPestanasTemporales();
        Tab tabHabitaciones = gestorHabitaciones.crearPestanaParaHotel(hotel);
        tabManager.agregarYSeleccionar(tabHabitaciones);
    }

    // Nuevo método cerrarSesion() - idéntico al de MenuHuesped
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
                    datos.put("usuario", usuario != null ? usuario : "");
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
                    datos.put("usuario", usuario != null ? usuario : "");
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

    // Método para obtener el Stage de manera segura - idéntico al de MenuHuesped
    private Stage obtenerStage() {
        // Primero intentar usar la referencia directa
        if (stage != null) {
            return stage;
        }

        // Si no existe, intentar obtenerlo desde la vista
        try {
            if (root != null && root.getScene() != null && root.getScene().getWindow() != null) {
                return (Stage) root.getScene().getWindow();
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

    // Método para mostrar errores
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Mantener el método confirmarSalida() original como respaldo (ya no se usa)
    private void confirmarSalida() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Salida");
        alert.setHeaderText("¿Está seguro que desea salir de la aplicación?");
        alert.setContentText("Se cerrarán todas las ventanas abiertas y la conexión al servidor.");

        ButtonType btnSalir = new ButtonType("Salir", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().setAll(btnSalir, btnCancelar);
        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnSalir) {
            System.exit(0);
        }
    }

    public void irAConsultaDisponibilidad() {
        tabManager.cerrarPestanasTemporales();
        tabPane.getSelectionModel().select(1);
    }

    public void irAGestionReservas() {
        tabManager.cerrarPestanasTemporales();
        tabPane.getSelectionModel().select(2);
    }

    public void irAGestionHoteles() {
        tabManager.volverAPrincipal();
    }

    public ConsultaDisponibilidad getConsultaDisponibilidad() {
        return consultaDisponibilidad;
    }

    public GestionReservas getGestionReservas() {
        return gestionReservas;
    }
}