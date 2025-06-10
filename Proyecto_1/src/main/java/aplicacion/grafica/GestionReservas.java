package aplicacion.grafica;

import aplicacion.dto.HotelDTO;
import aplicacion.dto.ReservaDTO;
import aplicacion.servicio.ServicioHoteles;
import aplicacion.servicio.ServicioReservas;
import aplicacion.servicio.ServicioReservas.ResultadoOperacion;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GestionReservas {

    private ServicioReservas servicioReservas;
    private ServicioHoteles servicioHoteles;

    private ComboBox<HotelItem> comboHotel;
    private ComboBox<String> comboEstado;
    private TextField txtCliente;
    private DatePicker dateDesde;
    private DatePicker dateHasta;
    private TextField txtCodigoReserva;
    private Button btnFiltrar;
    private Button btnLimpiar;
    private Button btnActualizar;

    // Tabla de reservas
    private TableView<ReservaDTO> tablaReservas;
    private ObservableList<ReservaDTO> datosTabla;
    private List<ReservaDTO> todasLasReservas;

    // Labels de estadísticas
    private Label lblTotalReservas;
    private Label lblReservasActivas;
    private Label lblIngresoTotal;
    private Label lblOcupacionPromedio;

    // Vista principal
    private BorderPane vistaPrincipal;

    public GestionReservas() {
        inicializarServicios();
        crearComponentes();
        configurarEventos();
        cargarDatosIniciales();
    }

    private void inicializarServicios() {
        try {
            servicioReservas = new ServicioReservas();
            servicioHoteles = new ServicioHoteles();
        } catch (Exception e) {
            mostrarError("Error inicializando servicios: " + e.getMessage());
        }
    }

    private void crearComponentes() {
        vistaPrincipal = new BorderPane();

        // Panel superior - Filtros
        VBox panelFiltros = crearPanelFiltros();
        vistaPrincipal.setTop(panelFiltros);

        // Panel central - Tabla
        VBox panelCentral = crearPanelCentral();
        vistaPrincipal.setCenter(panelCentral);

        // Panel inferior - Estadísticas
        HBox panelEstadisticas = crearPanelEstadisticas();
        vistaPrincipal.setBottom(panelEstadisticas);
    }

    private VBox crearPanelFiltros() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));
        contenedor.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        Label titulo = new Label("Filtros y Búsqueda de Reservas");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        // Primera fila - Hotel, Estado, Cliente
        HBox fila1 = new HBox(15);
        fila1.setAlignment(Pos.CENTER_LEFT);

        // Hotel
        VBox grupoHotel = new VBox(5);
        Label lblHotel = new Label("Hotel:");
        lblHotel.setStyle("-fx-font-weight: bold;");
        comboHotel = new ComboBox<>();
        comboHotel.setPrefWidth(180);
        grupoHotel.getChildren().addAll(lblHotel, comboHotel);

        // Estado
        VBox grupoEstado = new VBox(5);
        Label lblEstado = new Label("Estado:");
        lblEstado.setStyle("-fx-font-weight: bold;");
        comboEstado = new ComboBox<>();
        comboEstado.setPrefWidth(140);
        comboEstado.getItems().addAll("Todos", "CONFIRMADA", "EN_CURSO", "FINALIZADA", "CANCELADA");
        comboEstado.getSelectionModel().selectFirst();
        grupoEstado.getChildren().addAll(lblEstado, comboEstado);

        // Cliente
        VBox grupoCliente = new VBox(5);
        Label lblCliente = new Label("Cliente:");
        lblCliente.setStyle("-fx-font-weight: bold;");
        txtCliente = new TextField();
        txtCliente.setPrefWidth(180);
        txtCliente.setPromptText("Nombre del cliente");
        grupoCliente.getChildren().addAll(lblCliente, txtCliente);

        fila1.getChildren().addAll(grupoHotel, grupoEstado, grupoCliente);

        // Segunda fila - Fechas y Código
        HBox fila2 = new HBox(15);
        fila2.setAlignment(Pos.CENTER_LEFT);

        // Fecha desde
        VBox grupoDesde = new VBox(5);
        Label lblDesde = new Label("Desde:");
        lblDesde.setStyle("-fx-font-weight: bold;");
        dateDesde = new DatePicker();
        dateDesde.setPrefWidth(140);
        grupoDesde.getChildren().addAll(lblDesde, dateDesde);

        // Fecha hasta
        VBox grupoHasta = new VBox(5);
        Label lblHasta = new Label("Hasta:");
        lblHasta.setStyle("-fx-font-weight: bold;");
        dateHasta = new DatePicker();
        dateHasta.setPrefWidth(140);
        grupoHasta.getChildren().addAll(lblHasta, dateHasta);

        // Código de reserva
        VBox grupoCodigo = new VBox(5);
        Label lblCodigo = new Label("Código Reserva:");
        lblCodigo.setStyle("-fx-font-weight: bold;");
        txtCodigoReserva = new TextField();
        txtCodigoReserva.setPrefWidth(140);
        txtCodigoReserva.setPromptText("RES0001");
        grupoCodigo.getChildren().addAll(lblCodigo, txtCodigoReserva);

        fila2.getChildren().addAll(grupoDesde, grupoHasta, grupoCodigo);

        // Tercera fila - Botones
        HBox filaBotones = new HBox(10);
        filaBotones.setAlignment(Pos.CENTER_LEFT);

        btnFiltrar = new Button("Aplicar Filtros");
        btnFiltrar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        btnLimpiar = new Button("Limpiar Filtros");
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        btnActualizar = new Button("Actualizar");
        btnActualizar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        filaBotones.getChildren().addAll(btnFiltrar, btnLimpiar, btnActualizar);

        contenedor.getChildren().addAll(titulo, fila1, fila2, filaBotones);

        return contenedor;
    }

    private VBox crearPanelCentral() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));

        Label titulo = new Label("Lista de Reservas");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        // Crear tabla
        tablaReservas = new TableView<>();
        datosTabla = FXCollections.observableArrayList();
        tablaReservas.setItems(datosTabla);

        // Configurar columnas
        TableColumn<ReservaDTO, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setPrefWidth(80);

        TableColumn<ReservaDTO, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colCliente.setPrefWidth(150);

        TableColumn<ReservaDTO, String> colHabitacion = new TableColumn<>("Habitación");
        colHabitacion.setCellValueFactory(new PropertyValueFactory<>("codigoHabitacion"));
        colHabitacion.setPrefWidth(100);

        TableColumn<ReservaDTO, LocalDate> colFechaDesde = new TableColumn<>("Desde");
        colFechaDesde.setCellValueFactory(new PropertyValueFactory<>("fechaDesde"));
        colFechaDesde.setPrefWidth(100);
        colFechaDesde.setCellFactory(column -> new TableCell<ReservaDTO, LocalDate>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                if (empty || fecha == null) {
                    setText(null);
                } else {
                    setText(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        TableColumn<ReservaDTO, LocalDate> colFechaHasta = new TableColumn<>("Hasta");
        colFechaHasta.setCellValueFactory(new PropertyValueFactory<>("fechaHasta"));
        colFechaHasta.setPrefWidth(100);
        colFechaHasta.setCellFactory(column -> new TableCell<ReservaDTO, LocalDate>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                if (empty || fecha == null) {
                    setText(null);
                } else {
                    setText(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        TableColumn<ReservaDTO, Double> colPrecio = new TableColumn<>("Precio Total");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        colPrecio.setPrefWidth(100);
        colPrecio.setCellFactory(column -> new TableCell<ReservaDTO, Double>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", precio));
                }
            }
        });

        TableColumn<ReservaDTO, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(100);
        colEstado.setCellFactory(column -> new TableCell<ReservaDTO, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    switch (estado) {
                        case "CONFIRMADA":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                            break;
                        case "EN_CURSO":
                            setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460;");
                            break;
                        case "FINALIZADA":
                            setStyle("-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;");
                            break;
                        case "CANCELADA":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        TableColumn<ReservaDTO, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(180);
        colAcciones.setCellFactory(column -> new TableCell<ReservaDTO, Void>() {
            private final HBox botones = new HBox(5);
            private final Button btnVer = new Button("Ver");
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");

            {
                btnVer.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8;");
                btnEditar.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-size: 10px; -fx-padding: 3 8;");
                btnEliminar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8;");

                btnVer.setOnAction(event -> {
                    ReservaDTO reserva = getTableView().getItems().get(getIndex());
                    abrirDialogoDetalles(reserva);
                });

                btnEditar.setOnAction(event -> {
                    ReservaDTO reserva = getTableView().getItems().get(getIndex());
                    abrirDialogoEdicion(reserva);
                });

                btnEliminar.setOnAction(event -> {
                    ReservaDTO reserva = getTableView().getItems().get(getIndex());
                    confirmarEliminacion(reserva);
                });

                botones.getChildren().addAll(btnVer, btnEditar, btnEliminar);
                botones.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ReservaDTO reserva = getTableView().getItems().get(getIndex());

                    // Deshabilitar edición/eliminación para reservas finalizadas
                    boolean esEditable = reserva != null &&
                            !"FINALIZADA".equals(reserva.getEstado()) &&
                            !"CANCELADA".equals(reserva.getEstado());

                    btnEditar.setDisable(!esEditable);
                    btnEliminar.setDisable(!esEditable);

                    setGraphic(botones);
                }
            }
        });

        tablaReservas.getColumns().addAll(colCodigo, colCliente, colHabitacion, colFechaDesde,
                colFechaHasta, colPrecio, colEstado, colAcciones);

        // Configurar tabla
        tablaReservas.setRowFactory(tv -> {
            TableRow<ReservaDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDialogoDetalles(row.getItem());
                }
            });
            return row;
        });

        tablaReservas.setPrefHeight(400);

        contenedor.getChildren().addAll(titulo, tablaReservas);
        VBox.setVgrow(tablaReservas, Priority.ALWAYS);

        return contenedor;
    }

    private HBox crearPanelEstadisticas() {
        HBox contenedor = new HBox(20);
        contenedor.setPadding(new Insets(10, 15, 15, 15));
        contenedor.setAlignment(Pos.CENTER_LEFT);
        contenedor.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; -fx-border-width: 1 0 0 0;");

        lblTotalReservas = crearLabelEstadistica("Total: 0 reservas", "#007bff");
        lblReservasActivas = crearLabelEstadistica("Activas: 0", "#28a745");
        lblIngresoTotal = crearLabelEstadistica("Ingresos: $0.00", "#17a2b8");
        lblOcupacionPromedio = crearLabelEstadistica("Ocupación: 0%", "#6c757d");

        contenedor.getChildren().addAll(lblTotalReservas, lblReservasActivas, lblIngresoTotal, lblOcupacionPromedio);

        return contenedor;
    }

    private Label crearLabelEstadistica(String texto, String color) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-padding: 5 10; " +
                "-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-radius: 3;");
        return label;
    }

    private void configurarEventos() {
        btnFiltrar.setOnAction(e -> aplicarFiltros());
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        btnActualizar.setOnAction(e -> cargarReservas());

        // Auto-filtrado al escribir en código de reserva
        txtCodigoReserva.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                aplicarFiltros();
            }
        });
    }

    private void cargarDatosIniciales() {
        // Cargar hoteles
        cargarHoteles();

        // Cargar reservas
        cargarReservas();
    }

    private void cargarHoteles() {
        Task<List<HotelDTO>> task = new Task<List<HotelDTO>>() {
            @Override
            protected List<HotelDTO> call() throws Exception {
                return servicioHoteles.listarHoteles();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<HotelDTO> hoteles = getValue();
                    comboHotel.getItems().clear();
                    comboHotel.getItems().add(new HotelItem(null, "Todos los hoteles"));

                    for (HotelDTO hotel : hoteles) {
                        comboHotel.getItems().add(new HotelItem(hotel.getCodigo(), hotel.getNombre()));
                    }
                    comboHotel.getSelectionModel().selectFirst();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> mostrarError("Error cargando hoteles: " + getException().getMessage()));
            }
        };

        new Thread(task).start();
    }

    private void cargarReservas() {
        btnActualizar.setDisable(true);
        btnActualizar.setText("Cargando...");

        Task<List<ReservaDTO>> task = new Task<List<ReservaDTO>>() {
            @Override
            protected List<ReservaDTO> call() throws Exception {
                return servicioReservas.listarReservas();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    todasLasReservas = getValue();
                    actualizarTabla(todasLasReservas);
                    actualizarEstadisticas(todasLasReservas);

                    btnActualizar.setDisable(false);
                    btnActualizar.setText("Actualizar");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    mostrarError("Error cargando reservas: " + getException().getMessage());
                    btnActualizar.setDisable(false);
                    btnActualizar.setText("Actualizar");
                });
            }
        };

        new Thread(task).start();
    }

    private void aplicarFiltros() {
        if (todasLasReservas == null) {
            return;
        }

        List<ReservaDTO> reservasFiltradas = todasLasReservas.stream()
                .filter(this::aplicarFiltroHotel)
                .filter(this::aplicarFiltroEstado)
                .filter(this::aplicarFiltroCliente)
                .filter(this::aplicarFiltroFechas)
                .filter(this::aplicarFiltroCodigo)
                .collect(Collectors.toList());

        actualizarTabla(reservasFiltradas);
        actualizarEstadisticas(reservasFiltradas);
    }

    private boolean aplicarFiltroHotel(ReservaDTO reserva) {
        HotelItem hotelSeleccionado = comboHotel.getSelectionModel().getSelectedItem();
        if (hotelSeleccionado == null || hotelSeleccionado.getCodigo() == null) {
            return true;
        }
        return true;
    }

    private boolean aplicarFiltroEstado(ReservaDTO reserva) {
        String estadoSeleccionado = comboEstado.getSelectionModel().getSelectedItem();
        return estadoSeleccionado == null || "Todos".equals(estadoSeleccionado) ||
                estadoSeleccionado.equals(reserva.getEstado());
    }

    private boolean aplicarFiltroCliente(ReservaDTO reserva) {
        String filtroCliente = txtCliente.getText().trim();
        if (filtroCliente.isEmpty()) {
            return true;
        }

        return reserva.getClienteNombre() != null &&
                reserva.getClienteNombre().toLowerCase().contains(filtroCliente.toLowerCase());
    }

    private boolean aplicarFiltroFechas(ReservaDTO reserva) {
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();

        if (desde == null && hasta == null) {
            return true;
        }

        LocalDate fechaReserva = reserva.getFechaDesde();
        if (fechaReserva == null) {
            return false;
        }

        boolean cumpleDesde = desde == null || !fechaReserva.isBefore(desde);
        boolean cumpleHasta = hasta == null || !fechaReserva.isAfter(hasta);

        return cumpleDesde && cumpleHasta;
    }

    private boolean aplicarFiltroCodigo(ReservaDTO reserva) {
        String filtrocodigo = txtCodigoReserva.getText().trim();
        if (filtrocodigo.isEmpty()) {
            return true;
        }

        return reserva.getCodigo() != null &&
                reserva.getCodigo().toLowerCase().contains(filtrocodigo.toLowerCase());
    }

    private void actualizarTabla(List<ReservaDTO> reservas) {
        datosTabla.clear();
        datosTabla.addAll(reservas);
    }

    private void actualizarEstadisticas(List<ReservaDTO> reservas) {
        int total = reservas.size();
        long activas = reservas.stream()
                .filter(r -> "CONFIRMADA".equals(r.getEstado()) || "EN_CURSO".equals(r.getEstado()))
                .count();

        double ingresoTotal = reservas.stream()
                .filter(r -> !"CANCELADA".equals(r.getEstado()))
                .mapToDouble(ReservaDTO::getPrecioTotal)
                .sum();

        double porcentajeOcupacion = total > 0 ? (activas * 100.0 / total) : 0;

        lblTotalReservas.setText("Total: " + total + " reservas");
        lblReservasActivas.setText("Activas: " + activas);
        lblIngresoTotal.setText(String.format("Ingresos: $%.2f", ingresoTotal));
        lblOcupacionPromedio.setText(String.format("Activas: %.1f%%", porcentajeOcupacion));
    }

    private void abrirDialogoDetalles(ReservaDTO reserva) {
        DialogoDetallesReserva dialogo = new DialogoDetallesReserva(reserva);
        dialogo.showAndWait();
    }

    private void abrirDialogoEdicion(ReservaDTO reserva) {
        DialogoEditarReserva dialogo = new DialogoEditarReserva(reserva);
        dialogo.showAndWait();

        if (dialogo.isReservaModificada()) {
            cargarReservas(); // Recargar datos
        }
    }

    private void confirmarEliminacion(ReservaDTO reserva) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar reserva " + reserva.getCodigo() + "?");
        alert.setContentText("Esta acción no se puede deshacer.\n\nCliente: " + reserva.getClienteNombre() +
                "\nFechas: " + reserva.getFechaDesde() + " al " + reserva.getFechaHasta());

        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().setAll(btnEliminar, btnCancelar);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnEliminar) {
            eliminarReserva(reserva);
        }
    }

    private void eliminarReserva(ReservaDTO reserva) {
        Task<ResultadoOperacion> task = new Task<ResultadoOperacion>() {
            @Override
            protected ResultadoOperacion call() throws Exception {
                return servicioReservas.eliminarReserva(reserva.getCodigo());
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    ResultadoOperacion resultado = getValue();
                    if (resultado.isExito()) {
                        mostrarInformacion("Reserva eliminada exitosamente");
                        cargarReservas();
                    } else {
                        mostrarError("Error eliminando reserva: " + resultado.getMensaje());
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> mostrarError("Error eliminando reserva: " + getException().getMessage()));
            }
        };

        new Thread(task).start();
    }

    private void limpiarFiltros() {
        comboHotel.getSelectionModel().selectFirst();
        comboEstado.getSelectionModel().selectFirst();
        txtCliente.clear();
        dateDesde.setValue(null);
        dateHasta.setValue(null);
        txtCodigoReserva.clear();

        if (todasLasReservas != null) {
            actualizarTabla(todasLasReservas);
            actualizarEstadisticas(todasLasReservas);
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public BorderPane getVista() {
        return vistaPrincipal;
    }

    // Clase auxiliar
    private static class HotelItem {
        private final String codigo;
        private final String nombre;

        public HotelItem(String codigo, String nombre) {
            this.codigo = codigo;
            this.nombre = nombre;
        }

        public String getCodigo() { return codigo; }

        @Override
        public String toString() { return nombre; }
    }

    // Diálogo de detalles de reserva
    private class DialogoDetallesReserva extends Stage {

        public DialogoDetallesReserva(ReservaDTO reserva) {
            initModality(Modality.APPLICATION_MODAL);
            setTitle("Detalles de Reserva - " + reserva.getCodigo());
            setResizable(false);

            crearDialogo(reserva);
        }

        private void crearDialogo(ReservaDTO reserva) {
            VBox contenido = new VBox(15);
            contenido.setPadding(new Insets(20));

            Label titulo = new Label("Información Completa de la Reserva");
            titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

            // Información principal
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(10);
            grid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-color: #dee2e6;");

            grid.add(new Label("Código:"), 0, 0);
            grid.add(new Label(reserva.getCodigo()), 1, 0);

            grid.add(new Label("Cliente:"), 0, 1);
            grid.add(new Label(reserva.getClienteNombre()), 1, 1);

            grid.add(new Label("Habitación:"), 0, 2);
            grid.add(new Label(reserva.getCodigoHabitacion()), 1, 2);

            grid.add(new Label("Fecha Desde:"), 0, 3);
            grid.add(new Label(reserva.getFechaDesde().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 1, 3);

            grid.add(new Label("Fecha Hasta:"), 0, 4);
            grid.add(new Label(reserva.getFechaHasta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 1, 4);

            grid.add(new Label("Precio Total:"), 0, 5);
            grid.add(new Label(String.format("$%.2f", reserva.getPrecioTotal())), 1, 5);

            grid.add(new Label("Estado:"), 0, 6);
            Label lblEstado = new Label(reserva.getEstado());
            switch (reserva.getEstado()) {
                case "CONFIRMADA":
                    lblEstado.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 3 8;");
                    break;
                case "EN_CURSO":
                    lblEstado.setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460; -fx-padding: 3 8;");
                    break;
                case "FINALIZADA":
                    lblEstado.setStyle("-fx-background-color: #e2e3e5; -fx-text-fill: #383d41; -fx-padding: 3 8;");
                    break;
                case "CANCELADA":
                    lblEstado.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 3 8;");
                    break;
            }
            grid.add(lblEstado, 1, 6);

            // Botón cerrar
            Button btnCerrar = new Button("Cerrar");
            btnCerrar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            btnCerrar.setOnAction(e -> close());

            HBox botones = new HBox(btnCerrar);
            botones.setAlignment(Pos.CENTER);

            contenido.getChildren().addAll(titulo, grid, botones);

            Scene scene = new Scene(contenido);
            setScene(scene);
        }
    }

    // Diálogo de edición de reserva
    private class DialogoEditarReserva extends Stage {
        private ReservaDTO reserva;
        private boolean reservaModificada = false;

        private TextField txtCliente;
        private DatePicker dateDesde;
        private DatePicker dateHasta;
        private ComboBox<String> comboEstado;

        public DialogoEditarReserva(ReservaDTO reserva) {
            this.reserva = reserva;

            initModality(Modality.APPLICATION_MODAL);
            setTitle("Editar Reserva - " + reserva.getCodigo());
            setResizable(false);

            crearDialogo();
        }

        private void crearDialogo() {
            VBox contenido = new VBox(15);
            contenido.setPadding(new Insets(20));

            Label titulo = new Label("Modificar Datos de la Reserva");
            titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

            // Formulario
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(10);

            grid.add(new Label("Código:"), 0, 0);
            Label lblCodigo = new Label(reserva.getCodigo());
            lblCodigo.setStyle("-fx-font-weight: bold;");
            grid.add(lblCodigo, 1, 0);

            grid.add(new Label("Cliente:"), 0, 1);
            txtCliente = new TextField(reserva.getClienteNombre());
            txtCliente.setPrefWidth(200);
            grid.add(txtCliente, 1, 1);

            grid.add(new Label("Fecha Desde:"), 0, 2);
            dateDesde = new DatePicker(reserva.getFechaDesde());
            grid.add(dateDesde, 1, 2);

            grid.add(new Label("Fecha Hasta:"), 0, 3);
            dateHasta = new DatePicker(reserva.getFechaHasta());
            grid.add(dateHasta, 1, 3);

            grid.add(new Label("Estado:"), 0, 4);
            comboEstado = new ComboBox<>();
            comboEstado.getItems().addAll("CONFIRMADA", "EN_CURSO", "FINALIZADA", "CANCELADA");
            comboEstado.setValue(reserva.getEstado());
            grid.add(comboEstado, 1, 4);

            HBox botones = new HBox(10);
            botones.setAlignment(Pos.CENTER);

            Button btnGuardar = new Button("Guardar Cambios");
            btnGuardar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            btnGuardar.setOnAction(e -> guardarCambios());

            Button btnCancelar = new Button("Cancelar");
            btnCancelar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            btnCancelar.setOnAction(e -> close());

            botones.getChildren().addAll(btnGuardar, btnCancelar);

            contenido.getChildren().addAll(titulo, grid, botones);

            Scene scene = new Scene(contenido);
            setScene(scene);
        }

        private void guardarCambios() {
            if (!validarFormulario()) {
                return;
            }

            // Actualizar reserva
            reserva.setClienteNombre(txtCliente.getText().trim());
            reserva.setFechaDesde(dateDesde.getValue());
            reserva.setFechaHasta(dateHasta.getValue());
            reserva.setEstado(comboEstado.getValue());

            Task<ResultadoOperacion> task = new Task<ResultadoOperacion>() {
                @Override
                protected ResultadoOperacion call() throws Exception {
                    return servicioReservas.modificarReserva(reserva);
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        ResultadoOperacion resultado = getValue();
                        if (resultado.isExito()) {
                            reservaModificada = true;
                            close();
                        } else {
                            mostrarError("Error modificando reserva: " + resultado.getMensaje());
                        }
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> mostrarError("Error modificando reserva: " + getException().getMessage()));
                }
            };

            new Thread(task).start();
        }

        private boolean validarFormulario() {
            if (txtCliente.getText().trim().isEmpty()) {
                mostrarError("El nombre del cliente es requerido");
                return false;
            }

            if (dateDesde.getValue() == null || dateHasta.getValue() == null) {
                mostrarError("Las fechas son requeridas");
                return false;
            }

            if (dateDesde.getValue().isAfter(dateHasta.getValue()) || dateDesde.getValue().isEqual(dateHasta.getValue())) {
                mostrarError("La fecha hasta debe ser posterior a la fecha desde");
                return false;
            }

            return true;
        }

        private void mostrarError(String mensaje) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }

        public boolean isReservaModificada() { return reservaModificada; }
    }
}