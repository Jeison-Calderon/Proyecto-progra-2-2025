package aplicacion.grafica;

import aplicacion.data.HabitacionesData;
import aplicacion.data.HotelesData;
import aplicacion.domain.Habitacion;
import aplicacion.domain.Hotel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.Node;

import java.util.*;

public class MenuPrincipal {

    private TextArea txtResultado;
    private TabPane tabPane;
    private BorderPane root;
    private ObservableList<Hotel> hotelesOriginales = FXCollections.observableArrayList();
    private final Map<String, ObservableList<Habitacion>> habitacionesPorHotel = new HashMap<>();

    public BorderPane getVista() {
        root = new BorderPane();
        tabPane = new TabPane();

        Tab tabHoteles = new Tab("Gestión de Hoteles");
        tabHoteles.setClosable(false);

        VBox vistaHoteles = crearVistaListadoHoteles();
        tabHoteles.setContent(vistaHoteles);

        tabPane.getTabs().add(tabHoteles);
        root.setCenter(tabPane);

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setWrapText(true);
        txtResultado.setPrefHeight(80);
        txtResultado.setStyle("-fx-control-inner-background: #f8f9fa;");

        Label lblHeader = new Label("Sistema de Gestión de Hoteles");
        lblHeader.getStyleClass().add("header-label");
        BorderPane header = new BorderPane();
        header.setCenter(lblHeader);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #343a40; -fx-text-fill: white;");
        lblHeader.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        root.setTop(header);
        root.setBottom(txtResultado);

        return root;
    }

    private VBox crearVistaListadoHoteles() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));

        Label lblTitulo = new Label("Gestión de Hoteles");
        lblTitulo.getStyleClass().add("section-header");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox boxBusqueda = new HBox(10);
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar hotel por nombre o ubicación...");
        Button btnBuscar = new Button("Buscar");
        btnBuscar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        Button btnListar = new Button("Listar");
        btnListar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        boxBusqueda.getChildren().addAll(txtBuscar, btnBuscar, btnListar);

        Button btnNuevoHotel = new Button("Crear Nuevo Hotel");
        btnNuevoHotel.getStyleClass().add("btn-primary");
        btnNuevoHotel.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        btnNuevoHotel.setOnAction(e -> mostrarFormularioNuevoHotel());

        VBox areaNotificacion = new VBox();
        areaNotificacion.setVisible(false);
        areaNotificacion.getStyleClass().add("notification");
        areaNotificacion.setPadding(new Insets(10));
        areaNotificacion.setStyle("-fx-background-color: #d4edda; -fx-border-color: #c3e6cb; -fx-border-radius: 4px;");

        Label lblMensajeNotificacion = new Label();
        lblMensajeNotificacion.setStyle("-fx-text-fill: #155724;");
        areaNotificacion.getChildren().add(lblMensajeNotificacion);

        TableView<Hotel> tablaHoteles = new TableView<>();
        tablaHoteles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Hotel, String> colCodigo = new TableColumn<>("ID");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoHotel"));

        TableColumn<Hotel, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Hotel, String> colUbicacion = new TableColumn<>("Ubicación");
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));

        // ✅ CORREGIDO: Columna de acciones con botones para todas las filas
        TableColumn<Hotel, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(param -> new TableCell<Hotel, Void>() {
            private final HBox contenedor = new HBox(5);
            private final Button btnInfo = new Button("Habitaciones");
            private final Button btnEditar = new Button("Editar");
            private final Button btnBorrar = new Button("Borrar");

            {
                // Configurar estilos de botones
                btnInfo.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-pref-width: 90;");
                btnEditar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-pref-width: 60;");
                btnBorrar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-pref-width: 60;");

                contenedor.getChildren().addAll(btnInfo, btnEditar, btnBorrar);
                contenedor.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    // ✅ CORREGIDO: Obtener el hotel de la fila actual
                    Hotel hotel = getTableRow().getItem();

                    // ✅ CORREGIDO: Configurar eventos para cada fila específica
                    btnInfo.setOnAction(e -> verHabitacionesHotel(hotel));
                    btnEditar.setOnAction(e -> editarHotel(hotel));
                    btnBorrar.setOnAction(e -> confirmarBorradoHotel(hotel));

                    setGraphic(contenedor);
                }
            }
        });

        tablaHoteles.getColumns().addAll(colCodigo, colNombre, colUbicacion, colAccion);

        // --- CARGA ASÍNCRONA DE HOTELES ---
        Task<List<Hotel>> cargarHotelesTask = new Task<>() {
            @Override
            protected List<Hotel> call() throws Exception {
                return HotelesData.listar();
            }
        };
        cargarHotelesTask.setOnSucceeded(event -> {
            hotelesOriginales.setAll(cargarHotelesTask.getValue());
            tablaHoteles.setItems(hotelesOriginales);
        });
        cargarHotelesTask.setOnFailed(event -> {
            mostrarNotificacion("Error cargando hoteles: " + cargarHotelesTask.getException(), false);
        });
        new Thread(cargarHotelesTask).start();
        // --- FIN DE CARGA ASÍNCRONA ---

        btnBuscar.setOnAction(e -> {
            String query = txtBuscar.getText().trim().toLowerCase();
            if (query.isEmpty()) {
                tablaHoteles.setItems(hotelesOriginales);
            } else {
                ObservableList<Hotel> filtrados = FXCollections.observableArrayList();
                for (Hotel hotel : hotelesOriginales) {
                    if (hotel.getNombre().toLowerCase().contains(query)
                            || hotel.getUbicacion().toLowerCase().contains(query)) {
                        filtrados.add(hotel);
                    }
                }
                tablaHoteles.setItems(filtrados);
            }
        });
        btnListar.setOnAction(e -> {
            txtBuscar.clear();
            tablaHoteles.setItems(hotelesOriginales);
        });

        contenedor.getChildren().addAll(lblTitulo, boxBusqueda, btnNuevoHotel, areaNotificacion, tablaHoteles);
        return contenedor;
    }

    private void cargarDatosHoteles() {
        hotelesOriginales.setAll(HotelesData.listar());
    }

    // ========== Vista y lógica para habitaciones ==========
    private void verHabitacionesHotel(Hotel hotel) {
        Tab tabHabitaciones = new Tab("Habitaciones: " + hotel.getNombre());
        tabHabitaciones.setClosable(true);

        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));

        Label lblTitulo = new Label("Habitaciones del Hotel: " + hotel.getNombre());
        lblTitulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox boxBusqueda = new HBox(10);
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar habitación por estilo o precio...");
        Button btnBuscar = new Button("Buscar");
        btnBuscar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        Button btnListar = new Button("Listar");
        btnListar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        boxBusqueda.getChildren().addAll(txtBuscar, btnBuscar, btnListar);

        TableView<Habitacion> tablaHabitaciones = new TableView<>();
        tablaHabitaciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Habitacion, String> colEstilo = new TableColumn<>("Estilo");
        colEstilo.setCellValueFactory(new PropertyValueFactory<>("estilo"));
        TableColumn<Habitacion, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tablaHabitaciones.getColumns().addAll(colEstilo, colPrecio);

        ObservableList<Habitacion> habitacionesOriginales = getHabitacionesHotel(hotel);
        tablaHabitaciones.setItems(habitacionesOriginales);

        ContextMenu menuContextual = new ContextMenu();
        MenuItem itemRegistrar = new MenuItem("Registrar habitación");
        MenuItem itemEditar = new MenuItem("Editar");
        MenuItem itemBorrar = new MenuItem("Borrar habitación");

        menuContextual.getItems().addAll(itemRegistrar, itemEditar, itemBorrar);
        tablaHabitaciones.setContextMenu(menuContextual);

        itemRegistrar.setOnAction(e -> registrarHabitacion(tablaHabitaciones, habitacionesOriginales, hotel));
        itemEditar.setOnAction(e -> {
            Habitacion habitacionSeleccionada = tablaHabitaciones.getSelectionModel().getSelectedItem();
            if (habitacionSeleccionada != null) {
                editarHabitacion(habitacionSeleccionada, tablaHabitaciones, habitacionesOriginales, hotel);
            }
        });
        itemBorrar.setOnAction(e -> {
            Habitacion habitacionSeleccionada = tablaHabitaciones.getSelectionModel().getSelectedItem();
            if (habitacionSeleccionada != null) {
                confirmarBorradoHabitacion(habitacionSeleccionada, tablaHabitaciones, habitacionesOriginales, hotel);
            }
        });

        btnBuscar.setOnAction(e -> {
            String query = txtBuscar.getText().trim().toLowerCase();
            if (query.isEmpty()) {
                tablaHabitaciones.setItems(habitacionesOriginales);
            } else {
                ObservableList<Habitacion> filtrados = FXCollections.observableArrayList();
                for (Habitacion hab : habitacionesOriginales) {
                    if (hab.getEstilo().toLowerCase().contains(query)
                            || String.valueOf(hab.getPrecio()).contains(query)) {
                        filtrados.add(hab);
                    }
                }
                tablaHabitaciones.setItems(filtrados);
            }
        });
        btnListar.setOnAction(e -> {
            txtBuscar.clear();
            tablaHabitaciones.setItems(habitacionesOriginales);
        });

        Button btnVolver = new Button("Volver a lista de hoteles");
        btnVolver.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnVolver.setOnAction(e -> tabPane.getSelectionModel().selectFirst());

        contenedor.getChildren().addAll(lblTitulo, boxBusqueda, tablaHabitaciones, btnVolver);

        tabHabitaciones.setContent(contenedor);
        tabPane.getTabs().add(tabHabitaciones);
        tabPane.getSelectionModel().select(tabHabitaciones);
    }

    // ✅ CORREGIDO: Carga solo las habitaciones del hotel dado, y cachea el resultado
    private ObservableList<Habitacion> getHabitacionesHotel(Hotel hotel) {
        String codigoHotel = hotel.getCodigoHotel();
        if (!habitacionesPorHotel.containsKey(codigoHotel)) {
            List<Habitacion> todas = HabitacionesData.listar();
            ObservableList<Habitacion> delHotel = FXCollections.observableArrayList();
            for (Habitacion habitacion : todas) {
                // ✅ CORREGIDO: Usar getCodigoHotel() en lugar de getCodigo()
                if (codigoHotel.equals(habitacion.getCodigoHotel())) {
                    delHotel.add(habitacion);
                }
            }
            habitacionesPorHotel.put(codigoHotel, delHotel);
        }
        return habitacionesPorHotel.get(codigoHotel);
    }

    // ========== CRUD Hotel/Habitación y utilidades ==========

    private void registrarHabitacion(TableView<Habitacion> tabla, ObservableList<Habitacion> habitacionesOriginales, Hotel hotel) {
        TextInputDialog dialogEstilo = new TextInputDialog();
        dialogEstilo.setHeaderText("Ingrese el estilo de la habitación:");
        Optional<String> optEstilo = dialogEstilo.showAndWait();
        if (!optEstilo.isPresent()) return;

        TextInputDialog dialogPrecio = new TextInputDialog();
        dialogPrecio.setHeaderText("Ingrese el precio de la habitación:");
        Optional<String> optPrecio = dialogPrecio.showAndWait();
        if (!optPrecio.isPresent()) return;

        String estilo = optEstilo.get().trim();
        String precioStr = optPrecio.get().trim();

        if (estilo.isEmpty() || precioStr.isEmpty()) {
            mostrarNotificacion("Todos los campos son obligatorios.", false);
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);

            String codigo = HabitacionesData.guardar(estilo, precio, hotel.getCodigoHotel());
            if ("duplicado".equals(codigo)) {
                mostrarNotificacion("✘ Error: Habitación duplicada", false);
            } else {
                mostrarNotificacion("✔ Habitación registrada con código: " + codigo, true);
                actualizarHabitacionesHotel(hotel);
                tabla.setItems(getHabitacionesHotel(hotel));
            }

        } catch (NumberFormatException e) {
            mostrarNotificacion("Precio debe ser numérico válido.", false);
        }
    }

    private void editarHabitacion(Habitacion habitacion, TableView<Habitacion> tabla, ObservableList<Habitacion> habitacionesOriginales, Hotel hotel) {
        Tab tabEditar = new Tab("Editar: Habitación " + habitacion.getCodigo());
        tabEditar.setClosable(true);

        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));

        Label lblTitulo = new Label("Editar Habitación");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtEstilo = new TextField(habitacion.getEstilo());
        TextField txtPrecio = new TextField(String.valueOf(habitacion.getPrecio()));

        grid.add(new Label("ID:"), 0, 0);
        grid.add(new Label(habitacion.getCodigo()), 1, 0);

        grid.add(new Label("Estilo:"), 0, 1);
        grid.add(txtEstilo, 1, 1);

        grid.add(new Label("Precio:"), 0, 2);
        grid.add(txtPrecio, 1, 2);

        HBox botonesAccion = new HBox(10);
        Button btnGuardar = new Button("Guardar Cambios");
        btnGuardar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");

        Button btnVolver = new Button("Volver");
        btnVolver.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

        botonesAccion.getChildren().addAll(btnGuardar, btnVolver);

        btnGuardar.setOnAction(e -> {
            try {
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                String estilo = txtEstilo.getText().trim();

                if (estilo.isEmpty()) {
                    mostrarNotificacion("El campo estilo no puede estar vacío", false);
                    return;
                }

                boolean modificado = HabitacionesData.modificar(
                        new Habitacion(habitacion.getCodigo(), estilo, precio, habitacion.getCodigoHotel())
                );
                if (modificado) {
                    mostrarNotificacion("✔ Habitación modificada correctamente", true);
                    actualizarHabitacionesHotel(hotel);
                    tabla.setItems(getHabitacionesHotel(hotel));
                } else {
                    mostrarNotificacion("✘ Error al modificar habitación", false);
                }
                tabPane.getTabs().remove(tabEditar);

            } catch (NumberFormatException ex) {
                mostrarNotificacion("El precio debe ser un número válido", false);
            }
        });

        btnVolver.setOnAction(e -> tabPane.getTabs().remove(tabEditar));

        contenedor.getChildren().addAll(lblTitulo, grid, botonesAccion);

        tabEditar.setContent(contenedor);
        tabPane.getTabs().add(tabEditar);
        tabPane.getSelectionModel().select(tabEditar);
    }

    private void confirmarBorradoHabitacion(Habitacion habitacion, TableView<Habitacion> tabla, ObservableList<Habitacion> habitacionesOriginales, Hotel hotel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminar");
        alert.setHeaderText("¿Está seguro que desea eliminar esta habitación?");
        alert.setContentText("Habitación: " + habitacion.getEstilo());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean eliminada = HabitacionesData.eliminar(habitacion.getCodigo());
            if (eliminada) {
                mostrarNotificacion("✔ Habitación eliminada correctamente", true);
                actualizarHabitacionesHotel(hotel);
                tabla.setItems(getHabitacionesHotel(hotel));
            } else {
                mostrarNotificacion("✘ Error al eliminar habitación", false);
            }
        }
    }

    private void mostrarFormularioNuevoHotel() {
        Dialog<Hotel> dialog = new Dialog<>();
        dialog.setTitle("Crear Nuevo Hotel");
        dialog.setHeaderText("Ingrese los datos del nuevo hotel");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtNombre = new TextField();
        TextField txtUbicacion = new TextField();

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Ubicación:"), 0, 1);
        grid.add(txtUbicacion, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                return new Hotel("", txtNombre.getText(), txtUbicacion.getText());
            }
            return null;
        });

        Optional<Hotel> resultado = dialog.showAndWait();

        resultado.ifPresent(hotel -> {
            registrarHotel(hotel.getNombre(), hotel.getUbicacion());
        });
    }

    private void registrarHotel(String nombre, String ubicacion) {
        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            mostrarNotificacion("Todos los campos son obligatorios.", false);
            return;
        }

        String codigo = HotelesData.guardar(nombre, ubicacion);
        if ("duplicado".equals(codigo)) {
            mostrarNotificacion("✘ Error: Hotel duplicado", false);
        } else {
            mostrarNotificacion("✔ Hotel registrado con código: " + codigo, true);
            cargarDatosHoteles();
        }
    }

    private void editarHotel(Hotel hotel) {
        Tab tabEditar = new Tab("Editar: " + hotel.getNombre());
        tabEditar.setClosable(true);

        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));

        Label lblTitulo = new Label("Editar Hotel");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtNombre = new TextField(hotel.getNombre());
        TextField txtUbicacion = new TextField(hotel.getUbicacion());

        grid.add(new Label("ID:"), 0, 0);
        grid.add(new Label(hotel.getCodigoHotel()), 1, 0);

        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(txtNombre, 1, 1);

        grid.add(new Label("Ubicación:"), 0, 2);
        grid.add(txtUbicacion, 1, 2);

        HBox botonesAccion = new HBox(10);
        Button btnGuardar = new Button("Guardar Cambios");
        btnGuardar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");

        Button btnVolver = new Button("Volver a lista de hoteles");
        btnVolver.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

        botonesAccion.getChildren().addAll(btnGuardar, btnVolver);

        btnGuardar.setOnAction(e -> {
            modificarHotel(hotel.getCodigoHotel(), txtNombre.getText(), txtUbicacion.getText());
            tabPane.getSelectionModel().selectFirst();
        });

        btnVolver.setOnAction(e -> tabPane.getSelectionModel().selectFirst());

        contenedor.getChildren().addAll(lblTitulo, grid, botonesAccion);

        tabEditar.setContent(contenedor);
        tabPane.getTabs().add(tabEditar);
        tabPane.getSelectionModel().select(tabEditar);
    }

    private void modificarHotel(String codigo, String nombre, String ubicacion) {
        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            mostrarNotificacion("Todos los campos son obligatorios.", false);
            return;
        }

        boolean modificado = HotelesData.modificar(new Hotel(codigo, nombre, ubicacion));
        if (modificado) {
            mostrarNotificacion("✔ Hotel modificado correctamente", true);
            cargarDatosHoteles();
        } else {
            mostrarNotificacion("✘ Error al modificar hotel", false);
        }
    }

    private void confirmarBorradoHotel(Hotel hotel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminar");
        alert.setHeaderText("¿Está seguro que desea eliminar este hotel?");
        alert.setContentText("Hotel: " + hotel.getNombre() + " (ID: " + hotel.getCodigoHotel() + ")");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean eliminado = HotelesData.eliminar(hotel.getCodigoHotel());
            if (eliminado) {
                mostrarNotificacion("✔ Hotel eliminado correctamente", true);
                cargarDatosHoteles();
            } else {
                mostrarNotificacion("✘ Error al eliminar hotel", false);
            }
        }
    }

    private void mostrarNotificacion(String mensaje, boolean esExito) {
        txtResultado.clear();
        txtResultado.appendText(mensaje + "\n");

        if (esExito) {
            txtResultado.setStyle("-fx-control-inner-background: #d4edda;");
        } else {
            txtResultado.setStyle("-fx-control-inner-background: #f8d7da;");
        }
    }

    // ✅ CORREGIDO: Refresca cache de habitaciones de un hotel específico
    private void actualizarHabitacionesHotel(Hotel hotel) {
        String codigoHotel = hotel.getCodigoHotel();
        List<Habitacion> todas = HabitacionesData.listar();
        ObservableList<Habitacion> delHotel = FXCollections.observableArrayList();
        for (Habitacion habitacion : todas) {
            // ✅ CORREGIDO: Usar getCodigoHotel() en lugar de getCodigo()
            if (codigoHotel.equals(habitacion.getCodigoHotel())) {
                delHotel.add(habitacion);
            }
        }
        habitacionesPorHotel.put(codigoHotel, delHotel);
    }
}