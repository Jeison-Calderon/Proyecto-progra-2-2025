package aplicacion.grafica;

import aplicacion.dto.HotelDTO;
import aplicacion.servicio.ServicioHabitaciones;
import aplicacion.servicio.ServicioHoteles;
import aplicacion.util.NotificacionManager;
import aplicacion.util.TabManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    public MenuPrincipal() {
        this.servicioHoteles = new ServicioHoteles();
        this.servicioHabitaciones = new ServicioHabitaciones();
        // NO inicializar gestores aquí - esperar a que tabPane esté creado
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
        Label lblHeader = new Label("Sistema de Gestión de Hoteles - Cliente");
        lblHeader.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.setCenter(lblHeader);
        HBox botonesNavegacion = crearBotonesNavegacion();
        header.setLeft(botonesNavegacion);
        Button btnSalir = crearBotonSalir();
        header.setRight(btnSalir);
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

    private Button crearBotonSalir() {
        Button btnSalir = new Button("Salir");

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

        btnSalir.setStyle(estiloNormal);
        btnSalir.setOnMouseEntered(e -> btnSalir.setStyle(estiloHover));
        btnSalir.setOnMouseExited(e -> btnSalir.setStyle(estiloNormal));
        btnSalir.setOnAction(e -> confirmarSalida());

        return btnSalir;
    }

    private void verHabitacionesHotel(HotelDTO hotel) {
        tabManager.cerrarPestanasTemporales();
        Tab tabHabitaciones = gestorHabitaciones.crearPestanaParaHotel(hotel);
        tabManager.agregarYSeleccionar(tabHabitaciones);
    }

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