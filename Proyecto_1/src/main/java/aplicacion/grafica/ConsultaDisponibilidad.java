package aplicacion.grafica;

import aplicacion.dto.DisponibilidadDTO;
import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import aplicacion.servicio.ServicioHabitaciones;
import aplicacion.servicio.ServicioHoteles;
import aplicacion.servicio.ServicioReservas;
import aplicacion.servicio.ServicioReservas.ResultadoOperacion;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ConsultaDisponibilidad {

    private ServicioHoteles servicioHoteles;
    private ServicioHabitaciones servicioHabitaciones;
    private ServicioReservas servicioReservas;

    private ComboBox<HotelItem> comboHotel;
    private DatePicker dateDesde;
    private DatePicker dateHasta;
    private TextField txtPrecioMin;
    private TextField txtPrecioMax;
    private ComboBox<String> comboEstilo;
    private Button btnConsultar;
    private Button btnLimpiar;

    // Tabla de resultados
    private TableView<DisponibilidadDTO> tablaDisponibilidad;
    private ObservableList<DisponibilidadDTO> datosTabla;

    // Labels de estadísticas
    private Label lblTotalHabitaciones;
    private Label lblPrecioPromedio;
    private Label lblRangoPrecios;

    // Vista principal y componentes de carga
    private BorderPane vistaPrincipal;
    private StackPane contenedorPrincipal;
    private VBox panelCarga;
    private Label tituloResultados;

    // ✅ NUEVO: Variables para las columnas (necesarias para el ordenamiento)
    private TableColumn<DisponibilidadDTO, String> colHotel;
    private TableColumn<DisponibilidadDTO, String> colHabitacion;
    private TableColumn<DisponibilidadDTO, String> colEstilo;
    private TableColumn<DisponibilidadDTO, Double> colPrecio;
    private TableColumn<DisponibilidadDTO, Integer> colImagenes;
    private TableColumn<DisponibilidadDTO, String> colEstado;

    public ConsultaDisponibilidad() {
        inicializarServicios();
        crearComponentes();
        configurarEventos();
        cargarDatosIniciales();
    }

    private void inicializarServicios() {
        try {
            servicioHoteles = new ServicioHoteles();
            servicioHabitaciones = new ServicioHabitaciones();
            servicioReservas = new ServicioReservas();
        } catch (Exception e) {
            mostrarError("Error inicializando servicios: " + e.getMessage());
        }
    }

    private void crearComponentes() {
        // ✅ CORREGIDO: Contenedor principal con StackPane para overlays
        contenedorPrincipal = new StackPane();
        vistaPrincipal = new BorderPane();
        contenedorPrincipal.getChildren().add(vistaPrincipal);

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

        Label titulo = new Label("Filtros de Búsqueda");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        // Primera fila - Hotel y Fechas
        HBox fila1 = new HBox(15);
        fila1.setAlignment(Pos.CENTER_LEFT);

        // Hotel
        VBox grupoHotel = new VBox(5);
        Label lblHotel = new Label("Hotel:");
        lblHotel.setStyle("-fx-font-weight: bold;");
        comboHotel = new ComboBox<>();
        comboHotel.setPrefWidth(200);
        grupoHotel.getChildren().addAll(lblHotel, comboHotel);

        // Fecha desde
        VBox grupoDesde = new VBox(5);
        Label lblDesde = new Label("Fecha Desde:");
        lblDesde.setStyle("-fx-font-weight: bold;");
        dateDesde = new DatePicker();
        dateDesde.setPrefWidth(150);
        dateDesde.setValue(LocalDate.now());
        grupoDesde.getChildren().addAll(lblDesde, dateDesde);

        // Fecha hasta
        VBox grupoHasta = new VBox(5);
        Label lblHasta = new Label("Fecha Hasta:");
        lblHasta.setStyle("-fx-font-weight: bold;");
        dateHasta = new DatePicker();
        dateHasta.setPrefWidth(150);
        dateHasta.setValue(LocalDate.now().plusDays(1));
        grupoHasta.getChildren().addAll(lblHasta, dateHasta);

        fila1.getChildren().addAll(grupoHotel, grupoDesde, grupoHasta);

        // Segunda fila - Precios y Estilo
        HBox fila2 = new HBox(15);
        fila2.setAlignment(Pos.CENTER_LEFT);

        // Precio mínimo
        VBox grupoPrecioMin = new VBox(5);
        Label lblPrecioMin = new Label("Precio Mín:");
        lblPrecioMin.setStyle("-fx-font-weight: bold;");
        txtPrecioMin = new TextField();
        txtPrecioMin.setPrefWidth(100);
        txtPrecioMin.setPromptText("0.00");
        grupoPrecioMin.getChildren().addAll(lblPrecioMin, txtPrecioMin);

        // Precio máximo
        VBox grupoPrecioMax = new VBox(5);
        Label lblPrecioMax = new Label("Precio Máx:");
        lblPrecioMax.setStyle("-fx-font-weight: bold;");
        txtPrecioMax = new TextField();
        txtPrecioMax.setPrefWidth(100);
        txtPrecioMax.setPromptText("999.99");
        grupoPrecioMax.getChildren().addAll(lblPrecioMax, txtPrecioMax);

        // Estilo
        VBox grupoEstilo = new VBox(5);
        Label lblEstilo = new Label("Estilo:");
        lblEstilo.setStyle("-fx-font-weight: bold;");
        comboEstilo = new ComboBox<>();
        comboEstilo.setPrefWidth(150);
        grupoEstilo.getChildren().addAll(lblEstilo, comboEstilo);

        fila2.getChildren().addAll(grupoPrecioMin, grupoPrecioMax, grupoEstilo);

        // Tercera fila - Botones
        HBox filaBotones = new HBox(10);
        filaBotones.setAlignment(Pos.CENTER_LEFT);

        btnConsultar = new Button("Consultar Disponibilidad");
        btnConsultar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        btnLimpiar = new Button("Limpiar Filtros");
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        filaBotones.getChildren().addAll(btnConsultar, btnLimpiar);

        contenedor.getChildren().addAll(titulo, fila1, fila2, filaBotones);

        return contenedor;
    }

    private VBox crearPanelCentral() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));

        // Título con contador de resultados
        tituloResultados = new Label("Habitaciones Disponibles");
        tituloResultados.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        // Crear tabla
        tablaDisponibilidad = new TableView<>();
        datosTabla = FXCollections.observableArrayList();
        tablaDisponibilidad.setItems(datosTabla);

        // Configurar placeholder para tabla vacía
        configurarTablaVacia();

        // ✅ CORREGIDO: Crear columnas como variables de instancia
        crearColumnas();

        // Agregar todas las columnas incluyendo la nueva de Estado
        tablaDisponibilidad.getColumns().addAll(colHotel, colHabitacion, colEstilo, colPrecio, colImagenes, colEstado, crearColumnaAcciones());

        // ✅ CORREGIDO: Habilitar ordenamiento de columnas
        habilitarOrdenamientoColumnas();

        // Configurar tabla
        tablaDisponibilidad.setRowFactory(tv -> {
            TableRow<DisponibilidadDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDialogoReserva(row.getItem());
                }
            });
            return row;
        });

        contenedor.getChildren().addAll(tituloResultados, tablaDisponibilidad);
        VBox.setVgrow(tablaDisponibilidad, Priority.ALWAYS);

        return contenedor;
    }

    // ✅ NUEVO: Método separado para crear columnas
    private void crearColumnas() {
        // Columna Hotel
        colHotel = new TableColumn<>("Hotel");
        colHotel.setCellValueFactory(new PropertyValueFactory<>("nombreHotel"));
        colHotel.setPrefWidth(120);

        // Columna Habitación
        colHabitacion = new TableColumn<>("Habitación");
        colHabitacion.setCellValueFactory(new PropertyValueFactory<>("numeroHabitacion"));
        colHabitacion.setPrefWidth(100);

        // Columna Estilo
        colEstilo = new TableColumn<>("Estilo");
        colEstilo.setCellValueFactory(new PropertyValueFactory<>("estilo"));
        colEstilo.setPrefWidth(120);

        // Columna Precio
        colPrecio = new TableColumn<>("Precio/Noche");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setPrefWidth(120);
        colPrecio.setCellFactory(column -> new TableCell<DisponibilidadDTO, Double>() {
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

        // Columna Imágenes
        colImagenes = new TableColumn<>("Imágenes");
        colImagenes.setCellValueFactory(new PropertyValueFactory<>("cantidadImagenes"));
        colImagenes.setPrefWidth(80);

        // Columna Estado
        colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(120);
        colEstado.setCellFactory(column -> new TableCell<DisponibilidadDTO, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    // Aplicar estilos según el estado
                    switch (estado.toUpperCase()) {
                        case "DISPONIBLE":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold; -fx-padding: 3 8; -fx-border-radius: 3;");
                            break;
                        case "MANTENIMIENTO":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold; -fx-padding: 3 8; -fx-border-radius: 3;");
                            break;
                        case "OCUPADA":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold; -fx-padding: 3 8; -fx-border-radius: 3;");
                            break;
                        case "FUERA_DE_SERVICIO":
                            setStyle("-fx-background-color: #e2e3e5; -fx-text-fill: #383d41; -fx-font-weight: bold; -fx-padding: 3 8; -fx-border-radius: 3;");
                            break;
                        default:
                            setStyle("-fx-font-weight: bold; -fx-padding: 3 8;");
                    }
                }
            }
        });
    }

    // ✅ NUEVO: Método separado para crear columna de acciones
    private TableColumn<DisponibilidadDTO, Void> crearColumnaAcciones() {
        TableColumn<DisponibilidadDTO, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setSortable(false); // Las acciones no se pueden ordenar

        colAcciones.setCellFactory(column -> new TableCell<DisponibilidadDTO, Void>() {
            private final Button btnReservar = new Button("Reservar");

            {
                btnReservar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 11px;");
                btnReservar.setOnAction(event -> {
                    DisponibilidadDTO item = getTableView().getItems().get(getIndex());
                    abrirDialogoReserva(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnReservar);
                }
            }
        });

        return colAcciones;
    }

    // ✅ CORREGIDO: Habilitar ordenamiento sin parámetros problemáticos
    private void habilitarOrdenamientoColumnas() {
        // Habilitar ordenamiento para columnas apropiadas
        colHotel.setSortable(true);
        colHabitacion.setSortable(true);
        colEstilo.setSortable(true);
        colPrecio.setSortable(true);
        colImagenes.setSortable(true);
        colEstado.setSortable(true);

        // Configurar comparadores personalizados
        colPrecio.setComparator(Double::compare);
        colImagenes.setComparator(Integer::compare);

        // Configurar ordenamiento inicial por precio (menor a mayor)
        tablaDisponibilidad.getSortOrder().add(colPrecio);
        colPrecio.setSortType(TableColumn.SortType.ASCENDING);

        // ✅ ELIMINADO: setSortPolicy problemático - JavaFX maneja esto automáticamente
        // tablaDisponibilidad.setSortPolicy(TableView.DEFAULT_SORT_POLICY);
    }

    // Configurar placeholder para tabla vacía
    private void configurarTablaVacia() {
        VBox placeholderVacio = new VBox(20);
        placeholderVacio.setAlignment(Pos.CENTER);
        placeholderVacio.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 40;");

        Label iconoVacio = new Label("🔍");
        iconoVacio.setStyle("-fx-font-size: 48px;");

        Label tituloVacio = new Label("No se encontraron habitaciones disponibles");
        tituloVacio.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6c757d;");

        Label mensajeVacio = new Label("Prueba ajustando los filtros de búsqueda o selecciona otras fechas");
        mensajeVacio.setStyle("-fx-font-size: 12px; -fx-text-fill: #868e96; -fx-text-alignment: center; -fx-wrap-text: true;");
        mensajeVacio.setMaxWidth(300);

        Button btnNuevaBusqueda = new Button("🔄 Nueva Búsqueda");
        btnNuevaBusqueda.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-border-radius: 5; -fx-background-radius: 5;");
        btnNuevaBusqueda.setOnAction(e -> limpiarFiltros());

        placeholderVacio.getChildren().addAll(iconoVacio, tituloVacio, mensajeVacio, btnNuevaBusqueda);

        tablaDisponibilidad.setPlaceholder(placeholderVacio);
    }

    // Crear indicador de carga
    private VBox crearIndicadorCarga(String mensaje) {
        VBox indicador = new VBox(15);
        indicador.setAlignment(Pos.CENTER);
        indicador.setStyle("-fx-background-color: rgba(248, 249, 250, 0.95); -fx-padding: 40; -fx-border-radius: 10; -fx-background-radius: 10;");
        indicador.setMaxWidth(300);
        indicador.setMaxHeight(200);

        Label iconoCarga = new Label("⏳");
        iconoCarga.setStyle("-fx-font-size: 32px;");

        ProgressIndicator progreso = new ProgressIndicator();
        progreso.setPrefSize(50, 50);
        progreso.setStyle("-fx-accent: #007bff;");

        Label textoCarga = new Label(mensaje);
        textoCarga.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #007bff; -fx-text-alignment: center;");
        textoCarga.setWrapText(true);

        indicador.getChildren().addAll(iconoCarga, progreso, textoCarga);
        return indicador;
    }

    // Mostrar/ocultar indicador de carga
    private void mostrarIndicadorCarga(String mensaje) {
        panelCarga = crearIndicadorCarga(mensaje);
        contenedorPrincipal.getChildren().add(panelCarga);
        StackPane.setAlignment(panelCarga, Pos.CENTER);
    }

    private void ocultarIndicadorCarga() {
        if (panelCarga != null) {
            contenedorPrincipal.getChildren().remove(panelCarga);
            panelCarga = null;
        }
    }

    private HBox crearPanelEstadisticas() {
        HBox contenedor = new HBox(20);
        contenedor.setPadding(new Insets(10, 15, 15, 15));
        contenedor.setAlignment(Pos.CENTER_LEFT);
        contenedor.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; -fx-border-width: 1 0 0 0;");

        lblTotalHabitaciones = crearLabelEstadistica("Total: 0 habitaciones", "#007bff");
        lblPrecioPromedio = crearLabelEstadistica("Precio promedio: $0.00", "#28a745");
        lblRangoPrecios = crearLabelEstadistica("Rango: $0.00 - $0.00", "#6c757d");

        contenedor.getChildren().addAll(lblTotalHabitaciones, lblPrecioPromedio, lblRangoPrecios);

        return contenedor;
    }

    private Label crearLabelEstadistica(String texto, String color) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-padding: 5 10; " +
                "-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-radius: 3;");
        return label;
    }

    private void configurarEventos() {
        btnConsultar.setOnAction(e -> consultarDisponibilidad());
        btnLimpiar.setOnAction(e -> limpiarFiltros());

        // Validación de fechas
        dateDesde.setOnAction(e -> validarFechas());
        dateHasta.setOnAction(e -> validarFechas());

        // Validación de precios
        txtPrecioMin.textProperty().addListener((obs, oldVal, newVal) -> validarPrecio(txtPrecioMin, newVal));
        txtPrecioMax.textProperty().addListener((obs, oldVal, newVal) -> validarPrecio(txtPrecioMax, newVal));
    }

    private void cargarDatosIniciales() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Cargar hoteles
                List<HotelDTO> hoteles = servicioHoteles.listarHoteles();

                Platform.runLater(() -> {
                    comboHotel.getItems().clear();
                    comboHotel.getItems().add(new HotelItem(null, "Todos los hoteles"));

                    for (HotelDTO hotel : hoteles) {
                        comboHotel.getItems().add(new HotelItem(hotel.getCodigo(), hotel.getNombre()));
                    }
                    comboHotel.getSelectionModel().selectFirst();
                });
                List<HabitacionDTO> habitaciones = servicioHabitaciones.listarHabitaciones();

                Platform.runLater(() -> {
                    comboEstilo.getItems().clear();
                    comboEstilo.getItems().add("Todos");

                    habitaciones.stream()
                            .map(HabitacionDTO::getEstilo)
                            .distinct()
                            .forEach(estilo -> comboEstilo.getItems().add(estilo));

                    comboEstilo.getSelectionModel().selectFirst();
                });

                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> mostrarError("Error cargando datos iniciales: " + getException().getMessage()));
            }
        };

        new Thread(task).start();
    }

    // Consulta con indicadores de carga y notificaciones
    private void consultarDisponibilidad() {
        if (!validarFormulario()) {
            return;
        }

        // Mostrar indicador de carga
        mostrarIndicadorCarga("Consultando disponibilidad en el servidor...");

        Task<List<DisponibilidadDTO>> task = new Task<List<DisponibilidadDTO>>() {
            @Override
            protected List<DisponibilidadDTO> call() throws Exception {
                String codigoHotel = null;
                HotelItem hotelSeleccionado = comboHotel.getSelectionModel().getSelectedItem();
                if (hotelSeleccionado != null && hotelSeleccionado.getCodigo() != null) {
                    codigoHotel = hotelSeleccionado.getCodigo();
                }

                LocalDate desde = dateDesde.getValue();
                LocalDate hasta = dateHasta.getValue();

                return servicioReservas.consultarDisponibilidad(codigoHotel, desde, hasta);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    ocultarIndicadorCarga();

                    List<DisponibilidadDTO> disponibilidad = getValue();
                    List<DisponibilidadDTO> disponibilidadFiltrada = aplicarFiltrosAdicionales(disponibilidad);

                    actualizarTabla(disponibilidadFiltrada);
                    actualizarEstadisticas(disponibilidadFiltrada);

                    // Mostrar mensaje de éxito
                    mostrarMensajeExito("Consulta completada: " + disponibilidadFiltrada.size() + " habitaciones encontradas");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    ocultarIndicadorCarga();
                    mostrarError("Error consultando disponibilidad: " + getException().getMessage());
                });
            }
        };

        new Thread(task).start();
    }

    private List<DisponibilidadDTO> aplicarFiltrosAdicionales(List<DisponibilidadDTO> disponibilidad) {
        return disponibilidad.stream()
                .filter(this::filtrarPorPrecio)
                .filter(this::filtrarPorEstilo)
                .toList();
    }

    private boolean filtrarPorPrecio(DisponibilidadDTO item) {
        try {
            if (!txtPrecioMin.getText().trim().isEmpty()) {
                double precioMin = Double.parseDouble(txtPrecioMin.getText().trim());
                if (item.getPrecio() < precioMin) return false;
            }

            if (!txtPrecioMax.getText().trim().isEmpty()) {
                double precioMax = Double.parseDouble(txtPrecioMax.getText().trim());
                if (item.getPrecio() > precioMax) return false;
            }

            return true;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private boolean filtrarPorEstilo(DisponibilidadDTO item) {
        String estiloSeleccionado = comboEstilo.getSelectionModel().getSelectedItem();
        return estiloSeleccionado == null || "Todos".equals(estiloSeleccionado) ||
                estiloSeleccionado.equals(item.getEstilo());
    }

    // Actualizar tabla con contador de resultados
    private void actualizarTabla(List<DisponibilidadDTO> disponibilidad) {
        datosTabla.clear();
        datosTabla.addAll(disponibilidad);

        // Actualizar título con cantidad de resultados
        if (disponibilidad.isEmpty()) {
            tituloResultados.setText("Habitaciones Disponibles - Sin resultados");
            tituloResultados.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
        } else {
            tituloResultados.setText("Habitaciones Disponibles (" + disponibilidad.size() + " encontradas)");
            tituloResultados.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        }
    }

    private void actualizarEstadisticas(List<DisponibilidadDTO> disponibilidad) {
        int total = disponibilidad.size();
        double promedio = disponibilidad.stream().mapToDouble(DisponibilidadDTO::getPrecio).average().orElse(0.0);
        double min = disponibilidad.stream().mapToDouble(DisponibilidadDTO::getPrecio).min().orElse(0.0);
        double max = disponibilidad.stream().mapToDouble(DisponibilidadDTO::getPrecio).max().orElse(0.0);

        lblTotalHabitaciones.setText("Total: " + total + " habitaciones");
        lblPrecioPromedio.setText(String.format("Precio promedio: $%.2f", promedio));
        lblRangoPrecios.setText(String.format("Rango: $%.2f - $%.2f", min, max));
    }

    // Sistema de notificaciones mejorado
    private void mostrarMensajeExito(String mensaje) {
        Label notificacion = new Label("✅ " + mensaje);
        notificacion.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; " +
                "-fx-padding: 10 15; -fx-font-weight: bold; -fx-border-color: #c3e6cb; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        mostrarNotificacionTemporal(notificacion);
    }

    private void mostrarMensajeInfo(String mensaje) {
        Label notificacion = new Label("ℹ️ " + mensaje);
        notificacion.setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460; " +
                "-fx-padding: 10 15; -fx-font-weight: bold; -fx-border-color: #bee5eb; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        mostrarNotificacionTemporal(notificacion);
    }

    private void mostrarNotificacionTemporal(Label notificacion) {
        VBox contenedorNotificacion = (VBox) vistaPrincipal.getTop();
        contenedorNotificacion.getChildren().add(0, notificacion);

        // Eliminar notificación después de 3 segundos
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            contenedorNotificacion.getChildren().remove(notificacion);
        }));
        timeline.play();
    }

    private void abrirDialogoReserva(DisponibilidadDTO disponibilidad) {
        DialogoReservaRapida dialogo = new DialogoReservaRapida(disponibilidad, dateDesde.getValue(), dateHasta.getValue());
        dialogo.showAndWait();

        if (dialogo.getCodigoReserva() != null && !dialogo.getCodigoReserva().isEmpty()) {
            mostrarInformacion("¡Reserva creada exitosamente!\n\n" +
                    "Código de reserva: " + dialogo.getCodigoReserva() + "\n" +
                    "Hotel: " + disponibilidad.getNombreHotel() + "\n" +
                    "Habitación: " + disponibilidad.getNumeroHabitacion());
        } else if (dialogo.isReservaCreada()) {
            mostrarInformacion("Reserva creada exitosamente");
        }
    }

    private boolean validarFormulario() {
        if (dateDesde.getValue() == null || dateHasta.getValue() == null) {
            mostrarError("Seleccione las fechas de búsqueda");
            return false;
        }

        if (dateDesde.getValue().isAfter(dateHasta.getValue()) || dateDesde.getValue().isEqual(dateHasta.getValue())) {
            mostrarError("La fecha 'hasta' debe ser posterior a la fecha 'desde'");
            return false;
        }

        if (dateDesde.getValue().isBefore(LocalDate.now())) {
            mostrarError("La fecha 'desde' no puede ser anterior a hoy");
            return false;
        }

        return true;
    }

    private void validarFechas() {
        if (dateDesde.getValue() != null && dateHasta.getValue() != null) {
            if (dateDesde.getValue().isAfter(dateHasta.getValue())) {
                dateHasta.setValue(dateDesde.getValue().plusDays(1));
            }
        }
    }

    private void validarPrecio(TextField campo, String nuevoValor) {
        if (!nuevoValor.isEmpty()) {
            try {
                Double.parseDouble(nuevoValor);
            } catch (NumberFormatException e) {
                Platform.runLater(() -> campo.setText(nuevoValor.replaceAll("[^0-9.]", "")));
            }
        }
    }

    // Limpiar filtros con mensaje informativo
    private void limpiarFiltros() {
        comboHotel.getSelectionModel().selectFirst();
        dateDesde.setValue(LocalDate.now());
        dateHasta.setValue(LocalDate.now().plusDays(1));
        txtPrecioMin.clear();
        txtPrecioMax.clear();
        comboEstilo.getSelectionModel().selectFirst();
        datosTabla.clear();

        lblTotalHabitaciones.setText("Total: 0 habitaciones");
        lblPrecioPromedio.setText("Precio promedio: $0.00");
        lblRangoPrecios.setText("Rango: $0.00 - $0.00");

        // Resetear título
        tituloResultados.setText("Habitaciones Disponibles");
        tituloResultados.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        mostrarMensajeInfo("Filtros limpiados - Lista para nueva búsqueda");
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

    // Retornar StackPane en lugar de BorderPane
    public StackPane getVista() {
        return contenedorPrincipal;
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

    // ✅ ACTUALIZADO: Diálogo de reserva rápida con campo recepcionista
    private class DialogoReservaRapida extends Stage {
        private DisponibilidadDTO disponibilidad;
        private LocalDate fechaDesde;
        private LocalDate fechaHasta;
        private boolean reservaCreada = false;
        private String codigoReserva;

        private TextField txtCliente;
        private TextField txtRecepcionista; // ✅ NUEVO: Campo recepcionista
        private Label lblPrecioTotal;

        public DialogoReservaRapida(DisponibilidadDTO disponibilidad, LocalDate fechaDesde, LocalDate fechaHasta) {
            this.disponibilidad = disponibilidad;
            this.fechaDesde = fechaDesde;
            this.fechaHasta = fechaHasta;

            initModality(Modality.APPLICATION_MODAL);
            setTitle("Reserva Rápida");
            setResizable(false);

            crearDialogo();
        }

        private void crearDialogo() {
            VBox contenido = new VBox(15);
            contenido.setPadding(new Insets(20));

            // Información de la habitación
            VBox infoHabitacion = new VBox(5);
            infoHabitacion.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-color: #dee2e6;");

            Label lblTitulo = new Label("Detalles de la Reserva");
            lblTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label lblHotel = new Label("Hotel: " + disponibilidad.getNombreHotel());
            Label lblHabitacion = new Label("Habitación: " + disponibilidad.getNumeroHabitacion() + " - " + disponibilidad.getEstilo());
            Label lblFechas = new Label("Fechas: " + fechaDesde + " al " + fechaHasta);

            // Mostrar también el estado de la habitación
            Label lblEstado = new Label("Estado: " + disponibilidad.getEstado());
            lblEstado.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");

            long dias = fechaDesde.until(fechaHasta).getDays();
            double precioTotal = dias * disponibilidad.getPrecio();
            lblPrecioTotal = new Label(String.format("Precio Total: $%.2f (%d noches × $%.2f)", precioTotal, dias, disponibilidad.getPrecio()));
            lblPrecioTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");

            infoHabitacion.getChildren().addAll(lblTitulo, lblHotel, lblHabitacion, lblEstado, lblFechas, lblPrecioTotal);

            // ✅ ACTUALIZADO: Formulario con cliente y recepcionista
            VBox formulario = new VBox(10);

            Label lblCliente = new Label("Nombre del Cliente:");
            lblCliente.setStyle("-fx-font-weight: bold;");

            txtCliente = new TextField();
            txtCliente.setPromptText("Ingrese el nombre completo del cliente");
            txtCliente.setPrefWidth(300);

            // ✅ NUEVO: Campo recepcionista
            Label lblRecepcionista = new Label("Recepcionista:");
            lblRecepcionista.setStyle("-fx-font-weight: bold;");

            txtRecepcionista = new TextField();
            txtRecepcionista.setPromptText("Ingrese el nombre del recepcionista");
            txtRecepcionista.setPrefWidth(300);

            // ✅ MEJORA: Preconfigurar con el usuario actual (CalebHv21)
            txtRecepcionista.setText("CalebHv21");

            formulario.getChildren().addAll(lblCliente, txtCliente, lblRecepcionista, txtRecepcionista);

            // Botones
            HBox botones = new HBox(10);
            botones.setAlignment(Pos.CENTER);

            Button btnConfirmar = new Button("Confirmar Reserva");
            btnConfirmar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            btnConfirmar.setOnAction(e -> confirmarReserva());

            Button btnCancelar = new Button("Cancelar");
            btnCancelar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            btnCancelar.setOnAction(e -> close());

            botones.getChildren().addAll(btnConfirmar, btnCancelar);

            contenido.getChildren().addAll(infoHabitacion, formulario, botones);

            Scene scene = new Scene(contenido);
            setScene(scene);
        }

        private void confirmarReserva() {
            String nombreCliente = txtCliente.getText().trim();
            String recepcionista = txtRecepcionista.getText().trim(); // ✅ NUEVO

            if (nombreCliente.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Datos Incompletos");
                alert.setHeaderText(null);
                alert.setContentText("Ingrese el nombre del cliente");
                alert.showAndWait();
                return;
            }

            // ✅ NUEVA: Validación de recepcionista
            if (recepcionista.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Datos Incompletos");
                alert.setHeaderText(null);
                alert.setContentText("Ingrese el nombre del recepcionista");
                alert.showAndWait();
                return;
            }

            try {
                // ✅ ACTUALIZADO: Usar el nuevo método crearReservaCompleta
                ResultadoOperacion resultado = servicioReservas.crearReservaCompleta(
                        nombreCliente,
                        recepcionista,
                        disponibilidad.getCodigoHabitacion(),
                        fechaDesde,
                        fechaHasta
                );

                if (resultado.isExito()) {
                    reservaCreada = true;

                    if (resultado.tieneReserva() && resultado.getReserva() != null) {
                        codigoReserva = resultado.getReserva().getCodigo();
                        System.out.println("✅ Código extraído de la reserva: " + codigoReserva);
                    } else {
                        if (resultado.getMensaje().contains("código:")) {
                            codigoReserva = resultado.getMensaje().replaceAll(".*código: ", "").trim();
                        } else {
                            // Como último recurso, generar un código temporal
                            codigoReserva = "RES" + System.currentTimeMillis();
                        }
                        System.out.println("✅ Código fallback generado: " + codigoReserva);
                    }

                    close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error creando reserva");
                    alert.setContentText(resultado.getMensaje());
                    alert.showAndWait();
                }

            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error de comunicación");
                alert.setContentText("No se pudo crear la reserva: " + ex.getMessage());
                alert.showAndWait();
            }
        }

        public boolean isReservaCreada() { return reservaCreada; }
        public String getCodigoReserva() { return codigoReserva; }
    }
}