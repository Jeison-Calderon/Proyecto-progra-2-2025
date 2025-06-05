package aplicacion.grafica;

import aplicacion.dto.HotelDTO;
import aplicacion.servicio.ServicioHabitaciones;
import aplicacion.servicio.ServicioHoteles;
import aplicacion.util.NotificacionManager;
import aplicacion.util.TabManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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

        // ✅ CREAR PESTAÑA PRINCIPAL DE HOTELES
        Tab tabHoteles = new Tab("Gestión de Hoteles");
        tabHoteles.setClosable(false);
        tabHoteles.setContent(gestorHoteles.crearVista());

        tabPane.getTabs().add(tabHoteles);
        root.setCenter(tabPane);

        // ✅ CREAR HEADER CON BOTÓN SALIR
        BorderPane header = crearHeader();
        root.setTop(header);

        // ✅ ÁREA DE NOTIFICACIONES EN LA PARTE INFERIOR
        root.setBottom(txtResultado);

        return root;
    }

    // ✅ CREAR HEADER CON TÍTULO Y BOTÓN SALIR
    private BorderPane crearHeader() {
        BorderPane header = new BorderPane();

        Label lblHeader = new Label("Sistema de Gestión de Hoteles - Cliente");
        lblHeader.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.setCenter(lblHeader);

        Button btnSalir = crearBotonSalir();
        header.setRight(btnSalir);

        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #343a40; -fx-text-fill: white;");

        return header;
    }

    // ✅ CREAR BOTÓN SALIR CON ESTILOS Y EVENTOS
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
        alert.setContentText("Se cerrarán todas las ventanas abiertas.");

        ButtonType btnSalir = new ButtonType("Salir", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().setAll(btnSalir, btnCancelar);

        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnSalir) {
            System.exit(0);
        }
    }
}