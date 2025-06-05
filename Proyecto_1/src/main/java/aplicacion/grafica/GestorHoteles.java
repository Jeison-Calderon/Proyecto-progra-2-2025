package aplicacion.grafica;

import aplicacion.dto.HotelDTO;
import aplicacion.servicio.ServicioHoteles;
import aplicacion.util.NotificacionManager;
import aplicacion.util.TabManager;
import aplicacion.vistas.VistaFormularios;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GestorHoteles {

    // ✅ SERVICIOS - Solo comunicación con servidor
    private ServicioHoteles servicioHoteles;
    private NotificacionManager notificacionManager;
    private TabManager tabManager;

    // ✅ DATOS - ObservableList para UI
    private ObservableList<HotelDTO> hotelesOriginales;
    private TableView<HotelDTO> tablaHoteles;

    // ✅ CALLBACK para cuando se selecciona "Habitaciones"
    private Consumer<HotelDTO> onVerHabitaciones;

    public GestorHoteles(ServicioHoteles servicioHoteles,
                         NotificacionManager notificacionManager,
                         TabManager tabManager) {
        this.servicioHoteles = servicioHoteles;
        this.notificacionManager = notificacionManager;
        this.tabManager = tabManager;
        this.hotelesOriginales = FXCollections.observableArrayList();
    }

    // ✅ CONFIGURAR CALLBACK PARA HABITACIONES
    public void setOnVerHabitaciones(Consumer<HotelDTO> callback) {
        this.onVerHabitaciones = callback;
    }

    // ✅ CREAR VISTA COMPLETA DE HOTELES
    public VBox crearVista() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));

        Label lblTitulo = new Label("Gestión de Hoteles");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // ✅ BARRA DE BÚSQUEDA
        HBox boxBusqueda = crearBarraBusqueda();

        // ✅ BOTÓN CREAR
        Button btnNuevoHotel = new Button("Crear Nuevo Hotel");
        btnNuevoHotel.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        btnNuevoHotel.setOnAction(e -> mostrarFormularioCrear());

        // ✅ TABLA DE HOTELES
        tablaHoteles = crearTablaHoteles();

        // ✅ CARGAR DATOS INICIALES DESDE SERVIDOR
        cargarDatosDesdeServidor();

        contenedor.getChildren().addAll(lblTitulo, boxBusqueda, btnNuevoHotel, tablaHoteles);
        return contenedor;
    }

    // ✅ CREAR BARRA DE BÚSQUEDA
    private HBox crearBarraBusqueda() {
        HBox boxBusqueda = new HBox(10);
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar hotel por nombre o ubicación...");

        Button btnBuscar = new Button("Buscar");
        btnBuscar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        Button btnListar = new Button("Listar");
        btnListar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

        // ✅ EVENTOS DE BÚSQUEDA
        btnBuscar.setOnAction(e -> buscarHoteles(txtBuscar.getText()));
        btnListar.setOnAction(e -> {
            txtBuscar.clear();
            cargarDatosDesdeServidor();
        });

        boxBusqueda.getChildren().addAll(txtBuscar, btnBuscar, btnListar);
        return boxBusqueda;
    }

    // ✅ CREAR TABLA DE HOTELES CON ACCIONES
    private TableView<HotelDTO> crearTablaHoteles() {
        TableView<HotelDTO> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ✅ COLUMNAS
        TableColumn<HotelDTO, String> colCodigo = new TableColumn<>("ID");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<HotelDTO, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<HotelDTO, String> colUbicacion = new TableColumn<>("Ubicación");
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));

        // ✅ COLUMNA DE ACCIONES
        TableColumn<HotelDTO, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(param -> new TableCell<HotelDTO, Void>() {
            private final HBox contenedor = new HBox(5);
            private final Button btnHabitaciones = new Button("Habitaciones");
            private final Button btnEditar = new Button("Editar");
            private final Button btnBorrar = new Button("Borrar");

            {
                btnHabitaciones.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-pref-width: 90;");
                btnEditar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-pref-width: 60;");
                btnBorrar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-pref-width: 60;");

                contenedor.getChildren().addAll(btnHabitaciones, btnEditar, btnBorrar);
                contenedor.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HotelDTO hotel = getTableRow().getItem();

                    btnHabitaciones.setOnAction(e -> {
                        if (onVerHabitaciones != null) {
                            onVerHabitaciones.accept(hotel);
                        }
                    });
                    btnEditar.setOnAction(e -> editarHotel(hotel));
                    btnBorrar.setOnAction(e -> confirmarEliminarHotel(hotel));

                    setGraphic(contenedor);
                }
            }
        });

        tabla.getColumns().addAll(colCodigo, colNombre, colUbicacion, colAccion);
        tabla.setItems(hotelesOriginales);

        return tabla;
    }

    // ✅ CARGAR DATOS DESDE SERVIDOR
    public void cargarDatosDesdeServidor() {
        Task<List<HotelDTO>> task = new Task<>() {
            @Override
            protected List<HotelDTO> call() throws Exception {
                return servicioHoteles.listarHoteles(); // ← SERVIDOR
            }
        };

        task.setOnSucceeded(e -> {
            hotelesOriginales.setAll(task.getValue());
            tablaHoteles.setItems(hotelesOriginales);
        });

        task.setOnFailed(e -> {
            notificacionManager.errorConexion(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // ✅ BUSCAR HOTELES (FILTRO LOCAL)
    private void buscarHoteles(String query) {
        String queryLower = query.trim().toLowerCase();
        if (queryLower.isEmpty()) {
            tablaHoteles.setItems(hotelesOriginales);
        } else {
            ObservableList<HotelDTO> filtrados = FXCollections.observableArrayList();
            for (HotelDTO hotel : hotelesOriginales) {
                if (hotel.getNombre().toLowerCase().contains(queryLower) ||
                        hotel.getUbicacion().toLowerCase().contains(queryLower)) {
                    filtrados.add(hotel);
                }
            }
            tablaHoteles.setItems(filtrados);
        }
    }

    // ✅ MOSTRAR FORMULARIO CREAR
    private void mostrarFormularioCrear() {
        Optional<HotelDTO> resultado = VistaFormularios.mostrarFormularioNuevoHotel();
        resultado.ifPresent(hotel -> crearHotelEnServidor(hotel));
    }

    // ✅ CREAR HOTEL EN SERVIDOR
    private void crearHotelEnServidor(HotelDTO hotel) {
        Task<HotelDTO> task = new Task<>() {
            @Override
            protected HotelDTO call() throws Exception {
                return servicioHoteles.guardarHotel(hotel.getNombre(), hotel.getUbicacion()); // ← SERVIDOR
            }
        };

        task.setOnSucceeded(e -> {
            HotelDTO hotelGuardado = task.getValue();
            notificacionManager.hotelCreado(hotelGuardado.getCodigo());
            cargarDatosDesdeServidor(); // ✅ REFRESCAR DESDE SERVIDOR
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof IllegalArgumentException) {
                notificacionManager.errorDuplicado("Hotel");
            } else {
                notificacionManager.errorGenerico(exception.getMessage());
            }
        });

        new Thread(task).start();
    }

    // ✅ EDITAR HOTEL
    private void editarHotel(HotelDTO hotel) {
        Optional<HotelDTO> resultado = VistaFormularios.mostrarFormularioEditarHotel(hotel);
        resultado.ifPresent(hotelModificado -> modificarHotelEnServidor(hotelModificado));
    }

    // ✅ MODIFICAR HOTEL EN SERVIDOR
    private void modificarHotelEnServidor(HotelDTO hotel) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return servicioHoteles.modificarHotel(hotel); // ← SERVIDOR
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                notificacionManager.hotelModificado();
                cargarDatosDesdeServidor(); // ✅ REFRESCAR DESDE SERVIDOR
            } else {
                notificacionManager.errorGenerico("No se pudo modificar el hotel");
            }
        });

        task.setOnFailed(e -> {
            notificacionManager.errorGenerico(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // ✅ CONFIRMAR ELIMINACIÓN
    private void confirmarEliminarHotel(HotelDTO hotel) {
        String mensaje = "Hotel: " + hotel.getNombre() + " (ID: " + hotel.getCodigo() + ")";
        boolean confirmado = VistaFormularios.confirmarEliminacion("hotel", mensaje);

        if (confirmado) {
            eliminarHotelEnServidor(hotel);
        }
    }

    // ✅ ELIMINAR HOTEL EN SERVIDOR
    private void eliminarHotelEnServidor(HotelDTO hotel) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return servicioHoteles.eliminarHotel(hotel.getCodigo()); // ← SERVIDOR
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                notificacionManager.hotelEliminado();
                cargarDatosDesdeServidor(); // ✅ REFRESCAR DESDE SERVIDOR
            } else {
                notificacionManager.errorGenerico("No se pudo eliminar el hotel");
            }
        });

        task.setOnFailed(e -> {
            notificacionManager.errorGenerico(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // ✅ GETTER PARA LA TABLA (si necesario)
    public TableView<HotelDTO> getTabla() {
        return tablaHoteles;
    }

    // ✅ GETTER PARA LOS DATOS (si necesario)
    public ObservableList<HotelDTO> getDatos() {
        return hotelesOriginales;
    }
}