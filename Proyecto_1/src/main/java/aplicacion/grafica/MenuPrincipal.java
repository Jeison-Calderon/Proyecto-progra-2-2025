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

    // ✅ SERVICIOS para comunicación con el servidor
    private ServicioHoteles servicioHoteles;
    private ServicioHabitaciones servicioHabitaciones;

    // ✅ GESTORES ESPECIALIZADOS
    private GestorHoteles gestorHoteles;
    private GestorHabitaciones gestorHabitaciones;
    private NotificacionManager notificacionManager;
    private TabManager tabManager;

    // ✅ NUEVAS CLASES DE RESERVAS (JavaFX)
    private ConsultaDisponibilidad consultaDisponibilidad;
    private GestionReservas gestionReservas;

    // ✅ COMPONENTES UI PRINCIPALES
    private TextArea txtResultado;
    private TabPane tabPane;
    private BorderPane root;

    // ✅ CONSTRUCTOR: Inicializar servicios
    public MenuPrincipal() {
        this.servicioHoteles = new ServicioHoteles();
        this.servicioHabitaciones = new ServicioHabitaciones();
        inicializarGestores();
    }

    // ✅ INICIALIZAR GESTORES Y CONFIGURAR CALLBACKS
    private void inicializarGestores() {
        // Crear área de notificaciones
        txtResultado = new TextArea();

        // Inicializar gestores
        notificacionManager = new NotificacionManager(txtResultado);
        tabManager = new TabManager(tabPane); // Se inicializa después

        gestorHoteles = new GestorHoteles(servicioHoteles, notificacionManager, tabManager);
        gestorHabitaciones = new GestorHabitaciones(servicioHabitaciones, notificacionManager, tabManager);

        // ✅ INICIALIZAR NUEVAS CLASES DE RESERVAS
        consultaDisponibilidad = new ConsultaDisponibilidad();
        gestionReservas = new GestionReservas();

        // ✅ CONFIGURAR CALLBACK: Cuando se hace clic en "Habitaciones"
        gestorHoteles.setOnVerHabitaciones(this::verHabitacionesHotel);
    }

    // ✅ CREAR VISTA PRINCIPAL
    public BorderPane getVista() {
        root = new BorderPane();
        tabPane = new TabPane();

        // Actualizar tabManager con el tabPane creado
        tabManager = new TabManager(tabPane);

        // Reconfigurar gestores con el tabManager actualizado
        gestorHoteles = new GestorHoteles(servicioHoteles, notificacionManager, tabManager);
        gestorHabitaciones = new GestorHabitaciones(servicioHabitaciones, notificacionManager, tabManager);
        gestorHoteles.setOnVerHabitaciones(this::verHabitacionesHotel);

        // ✅ CREAR PESTAÑAS PRINCIPALES
        crearPestanasPrincipales();

        root.setCenter(tabPane);

        // ✅ CREAR HEADER CON TÍTULO Y BOTONES
        BorderPane header = crearHeader();
        root.setTop(header);

        // ✅ ÁREA DE NOTIFICACIONES EN LA PARTE INFERIOR
        root.setBottom(txtResultado);

        return root;
    }

    // ✅ CREAR PESTAÑAS PRINCIPALES DEL SISTEMA
    private void crearPestanasPrincipales() {
        // 🏨 PESTAÑA GESTIÓN DE HOTELES
        Tab tabHoteles = new Tab("Gestión de Hoteles");
        tabHoteles.setClosable(false);
        tabHoteles.setContent(gestorHoteles.crearVista());

        // 🔍 PESTAÑA CONSULTA DE DISPONIBILIDAD
        Tab tabConsulta = new Tab("Consulta Disponibilidad");
        tabConsulta.setClosable(false);
        tabConsulta.setContent(consultaDisponibilidad.getVista());

        // 📋 PESTAÑA GESTIÓN DE RESERVAS
        Tab tabReservas = new Tab("Gestión de Reservas");
        tabReservas.setClosable(false);
        tabReservas.setContent(gestionReservas.getVista());

        // ✅ AGREGAR PESTAÑAS AL TabPane
        tabPane.getTabs().addAll(tabHoteles, tabConsulta, tabReservas);

        // ✅ SELECCIONAR PRIMERA PESTAÑA POR DEFECTO
        tabPane.getSelectionModel().selectFirst();
    }

    // ✅ CREAR HEADER CON TÍTULO Y BOTONES DE NAVEGACIÓN
    private BorderPane crearHeader() {
        BorderPane header = new BorderPane();

        // ✅ TÍTULO PRINCIPAL
        Label lblHeader = new Label("Sistema de Gestión de Hoteles - Cliente");
        lblHeader.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.setCenter(lblHeader);

        // ✅ BOTONES DE NAVEGACIÓN RÁPIDA (IZQUIERDA) - SIN EMOJIS
        HBox botonesNavegacion = crearBotonesNavegacion();
        header.setLeft(botonesNavegacion);

        // ✅ BOTÓN SALIR (DERECHA) - SIN EMOJI
        Button btnSalir = crearBotonSalir();
        header.setRight(btnSalir);

        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #343a40; -fx-text-fill: white;");

        return header;
    }

    // ✅ CREAR BOTONES DE NAVEGACIÓN RÁPIDA - SIN EMOJIS
    private HBox crearBotonesNavegacion() {
        HBox contenedor = new HBox(5);
        contenedor.setStyle("-fx-alignment: center-left;");

        // Botón Hoteles - ÍNDICE 0
        Button btnHoteles = new Button("Hoteles");
        btnHoteles.setStyle(estiloBotonNavegacion());
        btnHoteles.setOnAction(e -> {
            System.out.println("Navegando a Hoteles - Índice 0");
            tabPane.getSelectionModel().select(0);
        });

        // Botón Disponibilidad - ÍNDICE 1
        Button btnDisponibilidad = new Button("Disponibilidad");
        btnDisponibilidad.setStyle(estiloBotonNavegacion());
        btnDisponibilidad.setOnAction(e -> {
            System.out.println("Navegando a Disponibilidad - Índice 1");
            tabPane.getSelectionModel().select(1);
        });

        // Botón Reservas - ÍNDICE 2
        Button btnReservas = new Button("Reservas");
        btnReservas.setStyle(estiloBotonNavegacion());
        btnReservas.setOnAction(e -> {
            System.out.println("Navegando a Reservas - Índice 2");
            tabPane.getSelectionModel().select(2);
        });

        contenedor.getChildren().addAll(btnHoteles, btnDisponibilidad, btnReservas);

        return contenedor;
    }

    // ✅ ESTILO PARA BOTONES DE NAVEGACIÓN
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

    // ✅ CREAR BOTÓN SALIR SIN EMOJI
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

    // ✅ VER HABITACIONES DE UN HOTEL (CALLBACK)
    private void verHabitacionesHotel(HotelDTO hotel) {
        tabManager.cerrarPestanasTemporales();

        Tab tabHabitaciones = gestorHabitaciones.crearPestanaParaHotel(hotel);
        tabManager.agregarYSeleccionar(tabHabitaciones);
    }

    // ✅ CONFIRMACIÓN DE SALIDA
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

    // ✅ MÉTODOS PÚBLICOS PARA NAVEGACIÓN EXTERNA

    /**
     * Navegar directamente a la pestaña de consulta de disponibilidad
     */
    public void irAConsultaDisponibilidad() {
        tabPane.getSelectionModel().select(1);
    }

    /**
     * Navegar directamente a la pestaña de gestión de reservas
     */
    public void irAGestionReservas() {
        tabPane.getSelectionModel().select(2);
    }

    /**
     * Navegar directamente a la pestaña de gestión de hoteles
     */
    public void irAGestionHoteles() {
        tabPane.getSelectionModel().select(0);
    }

    /**
     * Obtener referencia a la consulta de disponibilidad
     */
    public ConsultaDisponibilidad getConsultaDisponibilidad() {
        return consultaDisponibilidad;
    }

    /**
     * Obtener referencia a la gestión de reservas
     */
    public GestionReservas getGestionReservas() {
        return gestionReservas;
    }
}