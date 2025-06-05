package aplicacion.grafica;

import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import aplicacion.servicio.ServicioHabitaciones;
import aplicacion.util.NotificacionManager;
import aplicacion.util.TabManager;
import aplicacion.vistas.VistaFormularios;
import javafx.application.Platform;
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

public class GestorHabitaciones {

    // ✅ SERVICIOS - Solo comunicación con servidor
    private ServicioHabitaciones servicioHabitaciones;
    private NotificacionManager notificacionManager;
    private TabManager tabManager;

    // ✅ DATOS POR HOTEL - ObservableList para UI
    private ObservableList<HabitacionDTO> habitacionesActuales;
    private TableView<HabitacionDTO> tablaHabitaciones;
    private HotelDTO hotelActual;

    public GestorHabitaciones(ServicioHabitaciones servicioHabitaciones,
                              NotificacionManager notificacionManager,
                              TabManager tabManager) {
        this.servicioHabitaciones = servicioHabitaciones;
        this.notificacionManager = notificacionManager;
        this.tabManager = tabManager;
        this.habitacionesActuales = FXCollections.observableArrayList();
    }

    // ✅ CREAR VISTA COMPLETA DE HABITACIONES PARA UN HOTEL
    public VBox crearVistaParaHotel(HotelDTO hotel) {
        this.hotelActual = hotel;

        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));

        Label lblTitulo = new Label("Habitaciones del Hotel: " + hotel.getNombre());
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // ✅ BARRA DE BÚSQUEDA
        HBox boxBusqueda = crearBarraBusqueda();

        // ✅ BOTÓN CREAR HABITACIÓN
        Button btnNuevaHabitacion = new Button("Crear Nueva Habitación");
        btnNuevaHabitacion.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        btnNuevaHabitacion.setOnAction(e -> mostrarFormularioCrear());

        // ✅ TABLA DE HABITACIONES
        tablaHabitaciones = crearTablaHabitaciones();

        // ✅ BOTÓN VOLVER
        Button btnVolver = new Button("Volver a lista de hoteles");
        btnVolver.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnVolver.setOnAction(e -> volverAHoteles());

        // ✅ CARGAR DATOS INICIALES DESDE SERVIDOR
        cargarDatosDesdeServidor();

        contenedor.getChildren().addAll(lblTitulo, boxBusqueda, btnNuevaHabitacion, tablaHabitaciones, btnVolver);
        return contenedor;
    }

    // ✅ CREAR BARRA DE BÚSQUEDA
    private HBox crearBarraBusqueda() {
        HBox boxBusqueda = new HBox(10);
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar habitación por estilo o precio...");

        Button btnBuscar = new Button("Buscar");
        btnBuscar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        Button btnListar = new Button("Listar");
        btnListar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

        // ✅ EVENTOS DE BÚSQUEDA
        btnBuscar.setOnAction(e -> buscarHabitaciones(txtBuscar.getText()));
        btnListar.setOnAction(e -> {
            txtBuscar.clear();
            cargarDatosDesdeServidor();
        });

        boxBusqueda.getChildren().addAll(txtBuscar, btnBuscar, btnListar);
        return boxBusqueda;
    }

    // ✅ CREAR TABLA DE HABITACIONES CON ACCIONES
    private TableView<HabitacionDTO> crearTablaHabitaciones() {
        TableView<HabitacionDTO> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ✅ COLUMNAS
        TableColumn<HabitacionDTO, String> colCodigo = new TableColumn<>("ID");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<HabitacionDTO, String> colEstilo = new TableColumn<>("Estilo");
        colEstilo.setCellValueFactory(new PropertyValueFactory<>("estilo"));

        TableColumn<HabitacionDTO, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        // ✅ COLUMNA DE ACCIONES
        TableColumn<HabitacionDTO, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(param -> new TableCell<HabitacionDTO, Void>() {
            private final HBox contenedor = new HBox(5);
            private final Button btnEditar = new Button("Editar");
            private final Button btnBorrar = new Button("Borrar");

            {
                btnEditar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-pref-width: 60;");
                btnBorrar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-pref-width: 60;");

                contenedor.getChildren().addAll(btnEditar, btnBorrar);
                contenedor.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HabitacionDTO habitacion = getTableRow().getItem();

                    btnEditar.setOnAction(e -> editarHabitacion(habitacion));
                    btnBorrar.setOnAction(e -> confirmarEliminarHabitacion(habitacion));

                    setGraphic(contenedor);
                }
            }
        });

        tabla.getColumns().addAll(colCodigo, colEstilo, colPrecio, colAccion);
        tabla.setItems(habitacionesActuales);

        return tabla;
    }

    // ✅ CARGAR DATOS DESDE SERVIDOR
    public void cargarDatosDesdeServidor() {
        if (hotelActual == null) return;

        Task<List<HabitacionDTO>> task = new Task<>() {
            @Override
            protected List<HabitacionDTO> call() throws Exception {
                return servicioHabitaciones.listarHabitacionesPorHotel(hotelActual.getCodigo()); // ← SERVIDOR
            }
        };

        task.setOnSucceeded(e -> {
            habitacionesActuales.setAll(task.getValue());
            if (tablaHabitaciones != null) {
                tablaHabitaciones.setItems(habitacionesActuales);
            }
        });

        task.setOnFailed(e -> {
            notificacionManager.errorConexion(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // ✅ BUSCAR HABITACIONES (FILTRO LOCAL)
    private void buscarHabitaciones(String query) {
        String queryLower = query.trim().toLowerCase();
        if (queryLower.isEmpty()) {
            tablaHabitaciones.setItems(habitacionesActuales);
        } else {
            ObservableList<HabitacionDTO> filtradas = FXCollections.observableArrayList();
            for (HabitacionDTO habitacion : habitacionesActuales) {
                if (habitacion.getEstilo().toLowerCase().contains(queryLower) ||
                        String.valueOf(habitacion.getPrecio()).contains(queryLower)) {
                    filtradas.add(habitacion);
                }
            }
            tablaHabitaciones.setItems(filtradas);
        }
    }

    // ✅ MOSTRAR FORMULARIO CREAR
    private void mostrarFormularioCrear() {
        Optional<HabitacionDTO> resultado = VistaFormularios.mostrarFormularioNuevaHabitacion(hotelActual);
        resultado.ifPresent(habitacion -> crearHabitacionEnServidor(habitacion));
    }

    // ✅ CREAR HABITACIÓN EN SERVIDOR
    private void crearHabitacionEnServidor(HabitacionDTO habitacion) {
        Task<HabitacionDTO> task = new Task<>() {
            @Override
            protected HabitacionDTO call() throws Exception {
                String codigo = servicioHabitaciones.guardarHabitacion(
                        habitacion.getEstilo(),
                        habitacion.getPrecio(),
                        hotelActual.getCodigo()
                ); // ← SERVIDOR
                return new HabitacionDTO(codigo, habitacion.getEstilo(), habitacion.getPrecio(), hotelActual.getCodigo());
            }
        };

        task.setOnSucceeded(e -> {
            HabitacionDTO habitacionGuardada = task.getValue();
            notificacionManager.habitacionCreada(habitacionGuardada.getCodigo());

            // ✅ AGREGAR DIRECTAMENTE A LA LISTA VISIBLE (SOLUCIÓN 2)
            Platform.runLater(() -> {
                habitacionesActuales.add(habitacionGuardada);
                if (tablaHabitaciones != null) {
                    tablaHabitaciones.refresh();
                }
            });
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof IllegalArgumentException) {
                notificacionManager.errorDuplicado("Habitación");
            } else {
                notificacionManager.errorGenerico(exception.getMessage());
            }
        });

        new Thread(task).start();
    }

    // ✅ EDITAR HABITACIÓN
    private void editarHabitacion(HabitacionDTO habitacion) {
        Optional<HabitacionDTO> resultado = VistaFormularios.mostrarFormularioEditarHabitacion(habitacion);
        resultado.ifPresent(habitacionModificada -> modificarHabitacionEnServidor(habitacionModificada));
    }

    // ✅ MODIFICAR HABITACIÓN EN SERVIDOR
    private void modificarHabitacionEnServidor(HabitacionDTO habitacion) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return servicioHabitaciones.modificarHabitacion(habitacion); // ← SERVIDOR
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                notificacionManager.habitacionModificada();
                cargarDatosDesdeServidor(); // ✅ REFRESCAR DESDE SERVIDOR
            } else {
                notificacionManager.errorGenerico("No se pudo modificar la habitación");
            }
        });

        task.setOnFailed(e -> {
            notificacionManager.errorGenerico(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // ✅ CONFIRMAR ELIMINACIÓN
    private void confirmarEliminarHabitacion(HabitacionDTO habitacion) {
        String mensaje = "Habitación: " + habitacion.getEstilo() + " (ID: " + habitacion.getCodigo() + ")";
        boolean confirmado = VistaFormularios.confirmarEliminacion("habitación", mensaje);

        if (confirmado) {
            eliminarHabitacionEnServidor(habitacion);
        }
    }

    // ✅ ELIMINAR HABITACIÓN EN SERVIDOR
    private void eliminarHabitacionEnServidor(HabitacionDTO habitacion) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return servicioHabitaciones.eliminarHabitacion(habitacion.getCodigo()); // ← SERVIDOR
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                notificacionManager.habitacionEliminada();
                cargarDatosDesdeServidor(); // ✅ REFRESCAR DESDE SERVIDOR
            } else {
                notificacionManager.errorGenerico("No se pudo eliminar la habitación");
            }
        });

        task.setOnFailed(e -> {
            notificacionManager.errorGenerico(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // ✅ VOLVER A LISTA DE HOTELES
    private void volverAHoteles() {
        tabManager.volverAPrincipal();
    }

    // ✅ CREAR PESTAÑA PARA EL HOTEL
    public Tab crearPestanaParaHotel(HotelDTO hotel) {
        Tab tab = tabManager.crearPestana("Habitaciones: " + hotel.getNombre(), true);
        VBox contenido = crearVistaParaHotel(hotel);
        tab.setContent(contenido);
        return tab;
    }

    // ✅ GETTER PARA LA TABLA (si necesario)
    public TableView<HabitacionDTO> getTabla() {
        return tablaHabitaciones;
    }

    // ✅ GETTER PARA LOS DATOS (si necesario)
    public ObservableList<HabitacionDTO> getDatos() {
        return habitacionesActuales;
    }

    // ✅ GETTER PARA EL HOTEL ACTUAL
    public HotelDTO getHotelActual() {
        return hotelActual;
    }
}