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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GestionReservas {

    private ServicioReservas servicioReservas;
    private ServicioHoteles servicioHoteles;

    private ComboBox<HotelItem> comboHotel;
    private TableView<ReservaDTO> tablaReservas;
    private ObservableList<ReservaDTO> datosTabla;
    private Label lblTotal;
    private Button btnActualizar;
    private Button btnNuevaReserva; // ✅ NUEVO: Botón para nueva reserva

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
        VBox panelSuperior = crearPanelSuperior();
        vistaPrincipal.setTop(panelSuperior);

        // Panel central - Tabla
        VBox panelCentral = crearPanelCentral();
        vistaPrincipal.setCenter(panelCentral);

        // Panel inferior - Estadísticas
        HBox panelInferior = crearPanelInferior();
        vistaPrincipal.setBottom(panelInferior);
    }

    private VBox crearPanelSuperior() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));
        contenedor.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        Label titulo = new Label("Gestión de Reservas");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        HBox filtros = new HBox(15);
        filtros.setAlignment(Pos.CENTER_LEFT);

        // Hotel
        VBox grupoHotel = new VBox(5);
        Label lblHotel = new Label("Filtrar por Hotel:");
        lblHotel.setStyle("-fx-font-weight: bold;");
        comboHotel = new ComboBox<>();
        comboHotel.setPrefWidth(200);
        grupoHotel.getChildren().addAll(lblHotel, comboHotel);

        // Botones
        btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        // ✅ NUEVO: Botón para crear nueva reserva
        btnNuevaReserva = new Button("➕ Nueva Reserva");
        btnNuevaReserva.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        filtros.getChildren().addAll(grupoHotel, btnActualizar, btnNuevaReserva);

        contenedor.getChildren().addAll(titulo, filtros);
        return contenedor;
    }

    private VBox crearPanelCentral() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));

        // Crear tabla
        tablaReservas = new TableView<>();
        datosTabla = FXCollections.observableArrayList();
        tablaReservas.setItems(datosTabla);

        // Configurar columnas - ✅ ACTUALIZADO: Incluye columna de recepcionista
        crearColumnas();

        contenedor.getChildren().add(tablaReservas);
        VBox.setVgrow(tablaReservas, Priority.ALWAYS);

        return contenedor;
    }

    private void crearColumnas() {
        // Columna Código
        TableColumn<ReservaDTO, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setPrefWidth(100);

        // Columna Cliente
        TableColumn<ReservaDTO, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colCliente.setPrefWidth(150);

        // ✅ NUEVA: Columna Recepcionista
        TableColumn<ReservaDTO, String> colRecepcionista = new TableColumn<>("Recepcionista");
        colRecepcionista.setCellValueFactory(new PropertyValueFactory<>("recepcionista"));
        colRecepcionista.setPrefWidth(120);

        // Columna Habitación
        TableColumn<ReservaDTO, String> colHabitacion = new TableColumn<>("Habitación");
        colHabitacion.setCellValueFactory(new PropertyValueFactory<>("codigoHabitacion"));
        colHabitacion.setPrefWidth(100);

        // Columna Fecha Desde
        TableColumn<ReservaDTO, LocalDate> colFechaDesde = new TableColumn<>("Fecha Desde");
        colFechaDesde.setCellValueFactory(new PropertyValueFactory<>("fechaDesde"));
        colFechaDesde.setPrefWidth(120);
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

        // Columna Fecha Hasta
        TableColumn<ReservaDTO, LocalDate> colFechaHasta = new TableColumn<>("Fecha Hasta");
        colFechaHasta.setCellValueFactory(new PropertyValueFactory<>("fechaHasta"));
        colFechaHasta.setPrefWidth(120);
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

        // Columna Estado
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
                    switch (estado.toLowerCase()) {
                        case "activa":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                            break;
                        case "cancelada":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;");
                            break;
                        case "finalizada":
                            setStyle("-fx-background-color: #e2e3e5; -fx-text-fill: #383d41; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-font-weight: bold;");
                    }
                }
            }
        });

        // Columna Precio
        TableColumn<ReservaDTO, Double> colPrecio = new TableColumn<>("Precio Total");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        colPrecio.setPrefWidth(120);
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

        // Columna Acciones
        TableColumn<ReservaDTO, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(200);
        colAcciones.setCellFactory(param -> new TableCell<ReservaDTO, Void>() {
            private final Button btnVer = new Button("👁️ Ver");
            private final Button btnEditar = new Button("✏️ Editar");
            private final Button btnEliminar = new Button("🗑️ Eliminar");

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
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox botones = new HBox(5);
                    botones.getChildren().addAll(btnVer, btnEditar, btnEliminar);
                    setGraphic(botones);
                }
            }
        });

        // ✅ ACTUALIZADO: Agregar todas las columnas incluyendo la nueva de recepcionista
        tablaReservas.getColumns().addAll(colCodigo, colCliente, colRecepcionista, colHabitacion,
                colFechaDesde, colFechaHasta, colEstado, colPrecio, colAcciones);
    }

    private HBox crearPanelInferior() {
        HBox contenedor = new HBox(20);
        contenedor.setPadding(new Insets(10, 15, 15, 15));
        contenedor.setAlignment(Pos.CENTER_LEFT);
        contenedor.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #ced4da; -fx-border-width: 1 0 0 0;");

        lblTotal = new Label("Total: 0 reservas");
        lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");

        contenedor.getChildren().add(lblTotal);
        return contenedor;
    }

    private void configurarEventos() {
        btnActualizar.setOnAction(e -> cargarReservas());
        btnNuevaReserva.setOnAction(e -> abrirNuevaReserva()); // ✅ NUEVO
        comboHotel.setOnAction(e -> cargarReservas());
    }

    // ✅ NUEVO: Método para abrir ventana de nueva reserva
    private void abrirNuevaReserva() {
        try {
            NuevaReserva ventanaNuevaReserva = new NuevaReserva();
            ventanaNuevaReserva.showAndWait();

            if (ventanaNuevaReserva.isReservaCreada()) {
                cargarReservas(); // Recargar datos
                mostrarInformacion("¡Reserva creada exitosamente!\n\n" +
                        "Código: " + (ventanaNuevaReserva.getCodigoReservaCreada() != null ?
                        ventanaNuevaReserva.getCodigoReservaCreada() : "Generado"));
            }
        } catch (Exception e) {
            mostrarError("Error abriendo ventana de nueva reserva: " + e.getMessage());
        }
    }

    private void cargarDatosIniciales() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<HotelDTO> hoteles = servicioHoteles.listarHoteles();

                Platform.runLater(() -> {
                    comboHotel.getItems().clear();
                    comboHotel.getItems().add(new HotelItem(null, "Todos los hoteles"));

                    for (HotelDTO hotel : hoteles) {
                        comboHotel.getItems().add(new HotelItem(hotel.getCodigo(), hotel.getNombre()));
                    }
                    comboHotel.getSelectionModel().selectFirst();
                });

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> cargarReservas());
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> mostrarError("Error cargando datos iniciales: " + getException().getMessage()));
            }
        };

        new Thread(task).start();
    }

    private void cargarReservas() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String codigoHotel = null;
                HotelItem hotelSeleccionado = comboHotel.getSelectionModel().getSelectedItem();
                if (hotelSeleccionado != null && hotelSeleccionado.getCodigo() != null) {
                    codigoHotel = hotelSeleccionado.getCodigo();
                }

                List<ReservaDTO> reservas = servicioReservas.listarReservasPorHotel(codigoHotel);

                Platform.runLater(() -> {
                    datosTabla.clear();
                    datosTabla.addAll(reservas);
                    lblTotal.setText("Total: " + reservas.size() + " reservas");
                });

                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> mostrarError("Error cargando reservas: " + getException().getMessage()));
            }
        };

        new Thread(task).start();
    }

    private void confirmarEliminacion(ReservaDTO reserva) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar reserva?");
        confirmacion.setContentText("¿Está seguro de que desea eliminar la reserva " + reserva.getCodigo() + "?");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                eliminarReserva(reserva);
            }
        });
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
                        cargarReservas();
                        mostrarInformacion("Reserva eliminada exitosamente");
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

    // ✅ ACTUALIZADO: Diálogo de detalles con campo recepcionista
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
            grid.add(new Label(reserva.getClienteNombre() != null ? reserva.getClienteNombre() : "No especificado"), 1, 1);

            // ✅ NUEVO: Campo recepcionista en detalles
            grid.add(new Label("Recepcionista:"), 0, 2);
            grid.add(new Label(reserva.getRecepcionista() != null ? reserva.getRecepcionista() : "No especificado"), 1, 2);

            grid.add(new Label("Habitación:"), 0, 3);
            grid.add(new Label(reserva.getCodigoHabitacion()), 1, 3);

            grid.add(new Label("Fecha Desde:"), 0, 4);
            grid.add(new Label(reserva.getFechaDesde().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 1, 4);

            grid.add(new Label("Fecha Hasta:"), 0, 5);
            grid.add(new Label(reserva.getFechaHasta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 1, 5);

            grid.add(new Label("Precio Total:"), 0, 6);
            grid.add(new Label(String.format("$%.2f", reserva.getPrecioTotal())), 1, 6);

            grid.add(new Label("Estado:"), 0, 7);
            Label lblEstado = new Label(reserva.getEstado());
            switch (reserva.getEstado().toLowerCase()) {
                case "activa":
                    lblEstado.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 3 8;");
                    break;
                case "cancelada":
                    lblEstado.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 3 8;");
                    break;
                case "finalizada":
                    lblEstado.setStyle("-fx-background-color: #e2e3e5; -fx-text-fill: #383d41; -fx-padding: 3 8;");
                    break;
            }
            grid.add(lblEstado, 1, 7);

            // ✅ NUEVO: Mostrar fecha de creación si está disponible
            if (reserva.getFechaCreacion() != null) {
                grid.add(new Label("Fecha Creación:"), 0, 8);
                grid.add(new Label(reserva.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 1, 8);
            }

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

    // ✅ ACTUALIZADO: Diálogo de edición con campo recepcionista
    private class DialogoEditarReserva extends Stage {
        private ReservaDTO reserva;
        private boolean reservaModificada = false;

        private TextField txtCliente;
        private TextField txtRecepcionista; // ✅ NUEVO: Campo recepcionista
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
            txtCliente = new TextField(reserva.getClienteNombre() != null ? reserva.getClienteNombre() : "");
            txtCliente.setPrefWidth(250);
            grid.add(txtCliente, 1, 1);

            // ✅ NUEVO: Campo recepcionista en edición
            grid.add(new Label("Recepcionista:"), 0, 2);
            txtRecepcionista = new TextField(reserva.getRecepcionista() != null ? reserva.getRecepcionista() : "");
            txtRecepcionista.setPrefWidth(250);
            grid.add(txtRecepcionista, 1, 2);

            grid.add(new Label("Fecha Desde:"), 0, 3);
            dateDesde = new DatePicker(reserva.getFechaDesde());
            grid.add(dateDesde, 1, 3);

            grid.add(new Label("Fecha Hasta:"), 0, 4);
            dateHasta = new DatePicker(reserva.getFechaHasta());
            grid.add(dateHasta, 1, 4);

            grid.add(new Label("Estado:"), 0, 5);
            comboEstado = new ComboBox<>();
            comboEstado.getItems().addAll("activa", "cancelada", "finalizada");
            comboEstado.setValue(reserva.getEstado());
            grid.add(comboEstado, 1, 5);

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

            // ✅ ACTUALIZADO: Incluir recepcionista en la actualización
            reserva.setClienteNombre(txtCliente.getText().trim());
            reserva.setRecepcionista(txtRecepcionista.getText().trim());
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

            // ✅ NUEVA: Validación de recepcionista
            if (txtRecepcionista.getText().trim().isEmpty()) {
                mostrarError("El nombre del recepcionista es requerido");
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