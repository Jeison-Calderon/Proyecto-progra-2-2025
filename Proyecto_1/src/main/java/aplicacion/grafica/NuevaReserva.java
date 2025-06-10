package aplicacion.grafica;

import aplicacion.dto.*;
import aplicacion.servicio.*;
import aplicacion.servicio.ServicioReservas.ResultadoOperacion;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class NuevaReserva extends Stage {

    private final ServicioReservas servicioReservas;
    private final ServicioHoteles servicioHoteles;
    private final ServicioHabitaciones servicioHabitaciones;

    // Controles del formulario
    private ComboBox<HotelItem> comboHotel;
    private ComboBox<HabitacionItem> comboHabitacion;
    private TextField txtCliente;
    private TextField txtRecepcionista;
    private DatePicker dateDesde;
    private DatePicker dateHasta;
    private Label lblPrecioTotal;
    private Label lblDisponibilidad;

    // Control de estado
    private boolean reservaCreada = false;
    private String codigoReservaCreada = null;

    public NuevaReserva() {
        this.servicioReservas = new ServicioReservas();
        this.servicioHoteles = new ServicioHoteles();
        this.servicioHabitaciones = new ServicioHabitaciones();

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Nueva Reserva - Sistema Hotelero");
        setResizable(false);

        crearInterfaz();
        cargarDatosIniciales();
    }

    private void crearInterfaz() {
        VBox contenedorPrincipal = new VBox(20);
        contenedorPrincipal.setPadding(new Insets(25));
        contenedorPrincipal.setStyle("-fx-background-color: #f8f9fa;");

        // Título
        Label titulo = new Label("🏨 Crear Nueva Reserva");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Panel del formulario
        VBox panelFormulario = crearPanelFormulario();

        // Panel de información
        VBox panelInfo = crearPanelInformacion();

        // Botones
        HBox panelBotones = crearPanelBotones();

        contenedorPrincipal.getChildren().addAll(titulo, panelFormulario, panelInfo, panelBotones);

        Scene scene = new Scene(contenedorPrincipal, 500, 650);
        setScene(scene);
    }

    private VBox crearPanelFormulario() {
        VBox panel = new VBox(15);
        panel.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #dee2e6; -fx-border-radius: 8;");

        Label subtitulo = new Label("📋 Datos de la Reserva");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #495057;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        // Hotel
        grid.add(new Label("🏨 Hotel:"), 0, 0);
        comboHotel = new ComboBox<>();
        comboHotel.setPrefWidth(250);
        comboHotel.setPromptText("Seleccione un hotel");
        comboHotel.setOnAction(e -> cargarHabitacionesDelHotel());
        grid.add(comboHotel, 1, 0);

        // Habitación
        grid.add(new Label("🛏️ Habitación:"), 0, 1);
        comboHabitacion = new ComboBox<>();
        comboHabitacion.setPrefWidth(250);
        comboHabitacion.setPromptText("Primero seleccione un hotel");
        comboHabitacion.setDisable(true);
        comboHabitacion.setOnAction(e -> actualizarPrecioYDisponibilidad());
        grid.add(comboHabitacion, 1, 1);

        // Cliente
        grid.add(new Label("👤 Cliente:"), 0, 2);
        txtCliente = new TextField();
        txtCliente.setPrefWidth(250);
        txtCliente.setPromptText("Nombre completo del cliente");
        grid.add(txtCliente, 1, 2);

        // Recepcionista
        grid.add(new Label("👨‍💼 Recepcionista:"), 0, 3);
        txtRecepcionista = new TextField();
        txtRecepcionista.setPrefWidth(250);
        txtRecepcionista.setPromptText("Nombre del recepcionista");
        grid.add(txtRecepcionista, 1, 3);

        // Fecha desde
        grid.add(new Label("📅 Fecha Desde:"), 0, 4);
        dateDesde = new DatePicker();
        dateDesde.setPrefWidth(250);
        dateDesde.setValue(LocalDate.now());
        dateDesde.setOnAction(e -> {
            validarFechas();
            actualizarPrecioYDisponibilidad();
        });
        grid.add(dateDesde, 1, 4);

        // Fecha hasta
        grid.add(new Label("📅 Fecha Hasta:"), 0, 5);
        dateHasta = new DatePicker();
        dateHasta.setPrefWidth(250);
        dateHasta.setValue(LocalDate.now().plusDays(1));
        dateHasta.setOnAction(e -> {
            validarFechas();
            actualizarPrecioYDisponibilidad();
        });
        grid.add(dateHasta, 1, 5);

        panel.getChildren().addAll(subtitulo, grid);
        return panel;
    }

    private VBox crearPanelInformacion() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: #e9ecef; -fx-padding: 15; -fx-border-color: #ced4da; -fx-border-radius: 8;");

        Label subtitulo = new Label("💰 Información de Reserva");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #495057;");

        lblPrecioTotal = new Label("Precio Total: No calculado");
        lblPrecioTotal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        lblDisponibilidad = new Label("Disponibilidad: Seleccione habitación y fechas");
        lblDisponibilidad.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        panel.getChildren().addAll(subtitulo, lblPrecioTotal, lblDisponibilidad);
        return panel;
    }

    private HBox crearPanelBotones() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);

        Button btnCrear = new Button("✅ Crear Reserva");
        btnCrear.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 13px;");
        btnCrear.setOnAction(e -> crearReserva());

        Button btnCancelar = new Button("❌ Cancelar");
        btnCancelar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 13px;");
        btnCancelar.setOnAction(e -> close());

        Button btnLimpiar = new Button("🔄 Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 13px;");
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        panel.getChildren().addAll(btnCrear, btnLimpiar, btnCancelar);
        return panel;
    }

    private void cargarDatosIniciales() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<HotelDTO> hoteles = servicioHoteles.listarHoteles();

                Platform.runLater(() -> {
                    comboHotel.getItems().clear();
                    for (HotelDTO hotel : hoteles) {
                        comboHotel.getItems().add(new HotelItem(hotel.getCodigo(), hotel.getNombre()));
                    }
                });

                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> mostrarError("Error cargando hoteles: " + getException().getMessage()));
            }
        };

        new Thread(task).start();
    }

    private void cargarHabitacionesDelHotel() {
        HotelItem hotelSeleccionado = comboHotel.getSelectionModel().getSelectedItem();
        if (hotelSeleccionado == null) {
            comboHabitacion.getItems().clear();
            comboHabitacion.setDisable(true);
            comboHabitacion.setPromptText("Primero seleccione un hotel");
            return;
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<HabitacionDTO> habitaciones = servicioHabitaciones.listarHabitacionesPorHotel(hotelSeleccionado.getCodigo());

                Platform.runLater(() -> {
                    comboHabitacion.getItems().clear();
                    for (HabitacionDTO habitacion : habitaciones) {
                        comboHabitacion.getItems().add(new HabitacionItem(habitacion));
                    }
                    comboHabitacion.setDisable(false);
                    comboHabitacion.setPromptText("Seleccione una habitación");
                    actualizarPrecioYDisponibilidad();
                });

                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> mostrarError("Error cargando habitaciones: " + getException().getMessage()));
            }
        };

        new Thread(task).start();
    }

    private void actualizarPrecioYDisponibilidad() {
        HabitacionItem habitacionSeleccionada = comboHabitacion.getSelectionModel().getSelectedItem();
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();

        if (habitacionSeleccionada == null || desde == null || hasta == null) {
            lblPrecioTotal.setText("Precio Total: No calculado");
            lblDisponibilidad.setText("Disponibilidad: Seleccione habitación y fechas");
            return;
        }

        if (!desde.isBefore(hasta)) {
            lblPrecioTotal.setText("Precio Total: Fechas inválidas");
            lblDisponibilidad.setText("Disponibilidad: Fechas inválidas");
            return;
        }

        // Calcular precio
        long dias = desde.until(hasta).getDays();
        double precioTotal = dias * habitacionSeleccionada.getHabitacion().getPrecio();
        lblPrecioTotal.setText(String.format("Precio Total: $%.2f (%d noches × $%.2f)",
                precioTotal, dias, habitacionSeleccionada.getHabitacion().getPrecio()));

        // Verificar disponibilidad
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                boolean disponible = servicioReservas.verificarDisponibilidadHabitacion(
                        habitacionSeleccionada.getHabitacion().getCodigo(), desde, hasta);

                Platform.runLater(() -> {
                    if (disponible) {
                        lblDisponibilidad.setText("✅ Habitación disponible");
                        lblDisponibilidad.setStyle("-fx-font-size: 13px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else {
                        lblDisponibilidad.setText("❌ Habitación no disponible en estas fechas");
                        lblDisponibilidad.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    }
                });

                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    lblDisponibilidad.setText("⚠️ Error verificando disponibilidad");
                    lblDisponibilidad.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffc107; -fx-font-weight: bold;");
                });
            }
        };

        new Thread(task).start();
    }

    private void validarFechas() {
        if (dateDesde.getValue() != null && dateHasta.getValue() != null) {
            if (dateDesde.getValue().isAfter(dateHasta.getValue()) ||
                    dateDesde.getValue().isEqual(dateHasta.getValue())) {
                dateHasta.setValue(dateDesde.getValue().plusDays(1));
            }
        }
    }

    private void crearReserva() {
        if (!validarFormulario()) {
            return;
        }

        HabitacionItem habitacionSeleccionada = comboHabitacion.getSelectionModel().getSelectedItem();
        String cliente = txtCliente.getText().trim();
        String recepcionista = txtRecepcionista.getText().trim();
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();

        // Mostrar indicador de carga
        mostrarIndicadorCarga("Creando reserva...");

        Task<ResultadoOperacion> task = new Task<ResultadoOperacion>() {
            @Override
            protected ResultadoOperacion call() throws Exception {
                return servicioReservas.crearReservaCompleta(
                        cliente,
                        recepcionista,
                        habitacionSeleccionada.getHabitacion().getCodigo(),
                        desde,
                        hasta
                );
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    ocultarIndicadorCarga();
                    ResultadoOperacion resultado = getValue();

                    if (resultado.isExito()) {
                        reservaCreada = true;
                        if (resultado.tieneReserva()) {
                            codigoReservaCreada = resultado.getReserva().getCodigo();
                        }
                        mostrarExito("¡Reserva creada exitosamente!\n\n" +
                                "Código: " + (codigoReservaCreada != null ? codigoReservaCreada : "Generado") + "\n" +
                                "Cliente: " + cliente + "\n" +
                                "Recepcionista: " + recepcionista + "\n" +
                                "Habitación: " + habitacionSeleccionada.getHabitacion().getNumero());
                        close();
                    } else {
                        mostrarError("Error creando reserva: " + resultado.getMensaje());
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    ocultarIndicadorCarga();
                    mostrarError("Error creando reserva: " + getException().getMessage());
                });
            }
        };

        new Thread(task).start();
    }

    private boolean validarFormulario() {
        // Validar hotel
        if (comboHotel.getSelectionModel().getSelectedItem() == null) {
            mostrarError("Seleccione un hotel");
            return false;
        }

        // Validar habitación
        if (comboHabitacion.getSelectionModel().getSelectedItem() == null) {
            mostrarError("Seleccione una habitación");
            return false;
        }

        // Validar cliente
        if (txtCliente.getText().trim().isEmpty()) {
            mostrarError("Ingrese el nombre del cliente");
            return false;
        }

        // Validar recepcionista
        if (txtRecepcionista.getText().trim().isEmpty()) {
            mostrarError("Ingrese el nombre del recepcionista");
            return false;
        }

        // Validar fechas
        if (dateDesde.getValue() == null || dateHasta.getValue() == null) {
            mostrarError("Seleccione las fechas de la reserva");
            return false;
        }

        if (dateDesde.getValue().isBefore(LocalDate.now())) {
            mostrarError("La fecha de inicio no puede ser anterior a hoy");
            return false;
        }

        if (dateDesde.getValue().isAfter(dateHasta.getValue()) ||
                dateDesde.getValue().isEqual(dateHasta.getValue())) {
            mostrarError("La fecha de fin debe ser posterior a la fecha de inicio");
            return false;
        }

        return true;
    }

    private void limpiarFormulario() {
        comboHotel.getSelectionModel().clearSelection();
        comboHabitacion.getItems().clear();
        comboHabitacion.setDisable(true);
        comboHabitacion.setPromptText("Primero seleccione un hotel");
        txtCliente.clear();
        txtRecepcionista.clear();
        dateDesde.setValue(LocalDate.now());
        dateHasta.setValue(LocalDate.now().plusDays(1));
        lblPrecioTotal.setText("Precio Total: No calculado");
        lblDisponibilidad.setText("Disponibilidad: Seleccione habitación y fechas");
        lblDisponibilidad.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");
    }

    private void mostrarIndicadorCarga(String mensaje) {
        // Implementar indicador de carga si es necesario
    }

    private void ocultarIndicadorCarga() {
        // Ocultar indicador de carga si es necesario
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText("Reserva Creada");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Getters para verificar el resultado
    public boolean isReservaCreada() {
        return reservaCreada;
    }

    public String getCodigoReservaCreada() {
        return codigoReservaCreada;
    }

    // Clases auxiliares
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

    private static class HabitacionItem {
        private final HabitacionDTO habitacion;

        public HabitacionItem(HabitacionDTO habitacion) {
            this.habitacion = habitacion;
        }

        public HabitacionDTO getHabitacion() { return habitacion; }

        @Override
        public String toString() {
            return String.format("Hab. %s - %s ($%.2f/noche)",
                    habitacion.getNumero(),
                    habitacion.getEstilo(),
                    habitacion.getPrecio());
        }
    }
}