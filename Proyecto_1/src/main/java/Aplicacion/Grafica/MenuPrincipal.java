package Aplicacion.Grafica;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.util.Callback;
import javafx.scene.Node;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

public class MenuPrincipal {

    private ComboBox<String> cbHoteles;
    private PrintWriter writer;
    private BufferedReader reader;
    private TextArea txtResultado;
    private TabPane tabPane;
    private BorderPane root;

    private void registrarHabitacion(String codigoHotel, TableView<ModeloHabitacion> tabla) {
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
            txtResultado.appendText("Todos los campos son obligatorios.\n");
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);

            JSONObject habitacion = new JSONObject();
            habitacion.put("id", UUID.randomUUID().toString().substring(0, 8));
            habitacion.put("estilo", estilo);
            habitacion.put("precio", precio);
            habitacion.put("codigoHotel", codigoHotel);

            JSONObject request = new JSONObject();
            request.put("operacion", "crearHabitacion");
            request.put("habitacion", habitacion);

            enviarPeticion(request);
            cargarHabitacionesSimples(codigoHotel, tabla);

        } catch (NumberFormatException e) {
            txtResultado.appendText("Precio debe ser numérico válido.\n");
        }
    }

    private void verHabitacionesHotel(ModeloHotel hotel) {
        // Crear una nueva pestaña para las habitaciones del hotel
        Tab tabHabitaciones = new Tab("Habitaciones: " + hotel.getNombre());
        tabHabitaciones.setClosable(true);

        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));

        Label lblTitulo = new Label("Habitaciones del Hotel: " + hotel.getNombre());
        lblTitulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Tabla simple con solo estilo y precio
        TableView<ModeloHabitacion> tablaHabitaciones = new TableView<>();
        tablaHabitaciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Definir solo las columnas necesarias
        TableColumn<ModeloHabitacion, String> colEstilo = new TableColumn<>("estilo");
        colEstilo.setCellValueFactory(new PropertyValueFactory<>("estilo"));

        TableColumn<ModeloHabitacion, Double> colPrecio = new TableColumn<>("precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        // Añadir las columnas a la tabla
        tablaHabitaciones.getColumns().addAll(colEstilo, colPrecio);

        // Agregar el menú contextual
        ContextMenu menuContextual = new ContextMenu();
        MenuItem itemRegistrar = new MenuItem("Registrar habitación");
        MenuItem itemEditar = new MenuItem("Editar precio");
        MenuItem itemBorrar = new MenuItem("Borrar habitación");

        menuContextual.getItems().addAll(itemRegistrar, itemEditar, itemBorrar);
        tablaHabitaciones.setContextMenu(menuContextual);

        // Configurar acciones de menú contextual
        itemRegistrar.setOnAction(e -> registrarHabitacion(hotel.getCodigo(), tablaHabitaciones));

        itemEditar.setOnAction(e -> {
            ModeloHabitacion habitacionSeleccionada = tablaHabitaciones.getSelectionModel().getSelectedItem();
            if (habitacionSeleccionada != null) {
                TextInputDialog dialog = new TextInputDialog(String.valueOf(habitacionSeleccionada.getPrecio()));
                dialog.setHeaderText("Editar precio:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(nuevoPrecio -> {
                    try {
                        double precio = Double.parseDouble(nuevoPrecio);

                        JSONObject request = new JSONObject();
                        JSONObject hab = new JSONObject();
                        hab.put("id", habitacionSeleccionada.getId());
                        hab.put("estilo", habitacionSeleccionada.getEstilo());
                        hab.put("precio", precio);
                        hab.put("codigoHotel", hotel.getCodigo());

                        request.put("operacion", "modificarHabitacion");
                        request.put("habitacion", hab);

                        enviarPeticion(request);
                        cargarHabitacionesSimples(hotel.getCodigo(), tablaHabitaciones);
                    } catch (NumberFormatException ex) {
                        mostrarNotificacion("El precio debe ser un número válido", false);
                    }
                });
            }
        });

        itemBorrar.setOnAction(e -> {
            ModeloHabitacion habitacionSeleccionada = tablaHabitaciones.getSelectionModel().getSelectedItem();
            if (habitacionSeleccionada != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmar eliminar");
                alert.setHeaderText("¿Está seguro que desea eliminar esta habitación?");
                alert.setContentText("Habitación: " + habitacionSeleccionada.getEstilo());

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    JSONObject request = new JSONObject();
                    request.put("operacion", "eliminarHabitacion");
                    request.put("id", habitacionSeleccionada.getId());

                    enviarPeticion(request);
                    cargarHabitacionesSimples(hotel.getCodigo(), tablaHabitaciones);
                }
            }
        });

        // Cargar datos de habitaciones
        cargarHabitacionesSimples(hotel.getCodigo(), tablaHabitaciones);

        // Botón para volver
        Button btnVolver = new Button("Volver a lista de hoteles");
        btnVolver.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnVolver.setOnAction(e -> tabPane.getSelectionModel().selectFirst());

        contenedor.getChildren().addAll(lblTitulo, tablaHabitaciones, btnVolver);

        tabHabitaciones.setContent(contenedor);
        tabPane.getTabs().add(tabHabitaciones);
        tabPane.getSelectionModel().select(tabHabitaciones);
    }

    // Método para cargar solo las columnas estilo y precio
    private void cargarHabitacionesSimples(String codigoHotel, TableView<ModeloHabitacion> tabla) {
        JSONObject request = new JSONObject();
        request.put("operacion", "listarHabitaciones");
        request.put("codigoHotel", codigoHotel);

        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                JSONArray habitaciones = respuesta.getJSONArray("habitaciones");
                ObservableList<ModeloHabitacion> datos = FXCollections.observableArrayList();

                for (int i = 0; i < habitaciones.length(); i++) {
                    JSONObject h = habitaciones.getJSONObject(i);

                    // Identificar la clave de identificación - podría ser "id", "ID", o "codigo"
                    String idHabitacion = "";
                    if (h.has("id")) {
                        idHabitacion = h.getString("id");
                    } else if (h.has("ID")) {
                        idHabitacion = h.getString("ID");
                    } else if (h.has("codigo")) {
                        idHabitacion = h.getString("codigo");
                    } else {
                        // Si no encontramos un ID, generamos uno para evitar errores
                        idHabitacion = UUID.randomUUID().toString().substring(0, 8);
                    }

                    // Obtener el estilo y precio
                    String estilo = h.has("estilo") ? h.getString("estilo") : "";
                    double precio = h.has("precio") ? h.getDouble("precio") : 0.0;

                    datos.add(new ModeloHabitacion(
                            idHabitacion,
                            estilo,
                            precio,
                            codigoHotel
                    ));
                }

                tabla.setItems(datos);
            }
        } catch (IOException e) {
            txtResultado.appendText("Error al cargar habitaciones: " + e.getMessage() + "\n");
        }
    }

    public BorderPane getVista() {
        conectar();

        root = new BorderPane();
        tabPane = new TabPane();

        // Crear tab para gestión de hoteles
        Tab tabHoteles = new Tab("Gestión de Hoteles");
        tabHoteles.setClosable(false);

        // Crear la vista de listado de hoteles
        VBox vistaHoteles = crearVistaListadoHoteles();
        tabHoteles.setContent(vistaHoteles);

        tabPane.getTabs().add(tabHoteles);
        root.setCenter(tabPane);

        // Área de notificaciones
        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setWrapText(true);
        txtResultado.setPrefHeight(80);
        txtResultado.setStyle("-fx-control-inner-background: #f8f9fa;");

        // Añadir un encabezado a la ventana
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

        // Botón para crear nuevo hotel
        Button btnNuevoHotel = new Button("Crear Nuevo Hotel");
        btnNuevoHotel.getStyleClass().add("btn-primary");
        btnNuevoHotel.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        btnNuevoHotel.setOnAction(e -> mostrarFormularioNuevoHotel());

        // Área de notificación para resultados de operaciones
        VBox areaNotificacion = new VBox();
        areaNotificacion.setVisible(false);
        areaNotificacion.getStyleClass().add("notification");
        areaNotificacion.setPadding(new Insets(10));
        areaNotificacion.setStyle("-fx-background-color: #d4edda; -fx-border-color: #c3e6cb; -fx-border-radius: 4px;");

        Label lblMensajeNotificacion = new Label();
        lblMensajeNotificacion.setStyle("-fx-text-fill: #155724;");
        areaNotificacion.getChildren().add(lblMensajeNotificacion);

        // Crear tabla para listar hoteles
        TableView<ModeloHotel> tablaHoteles = new TableView<>();
        tablaHoteles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columnas
        TableColumn<ModeloHotel, String> colCodigo = new TableColumn<>("ID");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<ModeloHotel, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<ModeloHotel, String> colUbicacion = new TableColumn<>("Ubicación");
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));

        // Columna de acciones
        TableColumn<ModeloHotel, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(param -> new TableCell<ModeloHotel, Void>() {
            private final HBox contenedor = new HBox(5);
            private final Button btnInfo = new Button("Habitaciones");
            private final Button btnEditar = new Button("Editar");
            private final Button btnBorrar = new Button("Borrar");

            {
                // Estilos para los botones
                btnInfo.getStyleClass().add("btn-info");
                btnInfo.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                btnInfo.setOnAction(e -> verHabitacionesHotel(getTableView().getItems().get(getIndex())));

                btnEditar.getStyleClass().add("btn-edit");
                btnEditar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
                btnEditar.setOnAction(e -> editarHotel(getTableView().getItems().get(getIndex())));

                btnBorrar.getStyleClass().add("btn-delete");
                btnBorrar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                btnBorrar.setOnAction(e -> confirmarBorradoHotel(getTableView().getItems().get(getIndex())));

                contenedor.getChildren().addAll(btnInfo, btnEditar, btnBorrar);
                contenedor.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(contenedor);
                }
            }
        });

        tablaHoteles.getColumns().addAll(colCodigo, colNombre, colUbicacion, colAccion);

        // Cargar datos de hoteles
        cargarDatosHoteles(tablaHoteles);

        contenedor.getChildren().addAll(lblTitulo, btnNuevoHotel, areaNotificacion, tablaHoteles);
        return contenedor;
    }

    private void mostrarFormularioNuevoHotel() {
        // Crear un diálogo para el formulario
        Dialog<ModeloHotel> dialog = new Dialog<>();
        dialog.setTitle("Crear Nuevo Hotel");
        dialog.setHeaderText("Ingrese los datos del nuevo hotel");

        // Botones
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        // Crear el grid para el formulario
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

        // Convertir el resultado del diálogo cuando se presione Guardar
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                return new ModeloHotel("", txtNombre.getText(), txtUbicacion.getText());
            }
            return null;
        });

        Optional<ModeloHotel> resultado = dialog.showAndWait();

        resultado.ifPresent(hotel -> {
            registrarHotel(hotel.getNombre(), hotel.getUbicacion());
        });
    }

    private void registrarHotel(String nombre, String ubicacion) {
        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            mostrarNotificacion("Todos los campos son obligatorios.", false);
            return;
        }

        JSONObject hotel = new JSONObject();
        hotel.put("nombre", nombre);
        hotel.put("ubicacion", ubicacion);

        JSONObject request = new JSONObject();
        request.put("operacion", "crearHotel");
        request.put("hotel", hotel);

        enviarPeticion(request);
    }

    private void verDetalleHotel(ModeloHotel hotel) {
        // Crear una nueva pestaña para los detalles del hotel
        Tab tabDetalle = new Tab("Info: " + hotel.getNombre());
        tabDetalle.setClosable(true);

        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));

        Label lblTitulo = new Label("Información del Hotel");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 1px;");

        // Añadir información del hotel
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(new Label(hotel.getNombre()), 1, 0);

        grid.add(new Label("ID:"), 0, 1);
        grid.add(new Label(hotel.getCodigo()), 1, 1);

        grid.add(new Label("Ubicación:"), 0, 2);
        grid.add(new Label(hotel.getUbicacion()), 1, 2);

        // Botón para volver
        Button btnVolver = new Button("Volver a lista de hoteles");
        btnVolver.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnVolver.setOnAction(e -> tabPane.getSelectionModel().selectFirst());

        // Subtitulo para habitaciones
        Label lblHabitaciones = new Label("Habitaciones del Hotel");
        lblHabitaciones.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Tabla para mostrar habitaciones
        TableView<ModeloHabitacion> tablaHabitaciones = new TableView<>();
        cargarHabitacionesHotel(hotel.getCodigo(), tablaHabitaciones);

        contenedor.getChildren().addAll(lblTitulo, grid, lblHabitaciones, tablaHabitaciones, btnVolver);

        tabDetalle.setContent(contenedor);
        tabPane.getTabs().add(tabDetalle);
        tabPane.getSelectionModel().select(tabDetalle);
    }

    private void editarHotel(ModeloHotel hotel) {
        // Crear una nueva pestaña para editar el hotel
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
        grid.add(new Label(hotel.getCodigo()), 1, 0);

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
            modificarHotel(hotel.getCodigo(), txtNombre.getText(), txtUbicacion.getText());
            tabPane.getSelectionModel().selectFirst();
        });

        btnVolver.setOnAction(e -> tabPane.getSelectionModel().selectFirst());

        contenedor.getChildren().addAll(lblTitulo, grid, botonesAccion);

        tabEditar.setContent(contenedor);
        tabPane.getTabs().add(tabEditar);
        tabPane.getSelectionModel().select(tabEditar);
    }

    private void confirmarBorradoHotel(ModeloHotel hotel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminar");
        alert.setHeaderText("¿Está seguro que desea eliminar este hotel?");
        alert.setContentText("Hotel: " + hotel.getNombre() + " (ID: " + hotel.getCodigo() + ")");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eliminarHotel(hotel.getCodigo());
        }
    }

    private void eliminarHotel(String codigo) {
        JSONObject request = new JSONObject();
        request.put("operacion", "eliminarHotel");
        request.put("codigo", codigo);

        enviarPeticion(request);
    }

    private void modificarHotel(String codigo, String nombre, String ubicacion) {
        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            mostrarNotificacion("Todos los campos son obligatorios.", false);
            return;
        }

        JSONObject hotel = new JSONObject();
        hotel.put("codigo", codigo);
        hotel.put("nombre", nombre);
        hotel.put("ubicacion", ubicacion);

        JSONObject request = new JSONObject();
        request.put("operacion", "modificarHotel");
        request.put("hotel", hotel);

        enviarPeticion(request);
    }

    private void cargarDatosHoteles(TableView<ModeloHotel> tabla) {
        JSONObject request = new JSONObject();
        request.put("operacion", "listarHoteles");
        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                JSONArray hoteles = respuesta.getJSONArray("hoteles");
                ObservableList<ModeloHotel> datos = FXCollections.observableArrayList();

                for (int i = 0; i < hoteles.length(); i++) {
                    JSONObject hotel = hoteles.getJSONObject(i);
                    datos.add(new ModeloHotel(
                            hotel.getString("codigo"),
                            hotel.getString("nombre"),
                            hotel.getString("ubicacion")
                    ));
                }

                tabla.setItems(datos);
            }
        } catch (IOException e) {
            txtResultado.appendText("Error al listar hoteles: " + e.getMessage() + "\n");
        }
    }

    private void cargarHabitacionesHotel(String codigoHotel, TableView<ModeloHabitacion> tabla) {
        JSONObject request = new JSONObject();
        request.put("operacion", "listarHabitaciones");
        request.put("codigoHotel", codigoHotel);

        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                JSONArray habitaciones = respuesta.getJSONArray("habitaciones");
                ObservableList<ModeloHabitacion> datos = FXCollections.observableArrayList();

                // Configurar columnas
                tabla.getColumns().clear();

                TableColumn<ModeloHabitacion, String> colId = new TableColumn<>("ID");
                colId.setCellValueFactory(new PropertyValueFactory<>("id"));

                TableColumn<ModeloHabitacion, String> colEstilo = new TableColumn<>("Estilo");
                colEstilo.setCellValueFactory(new PropertyValueFactory<>("estilo"));

                TableColumn<ModeloHabitacion, Double> colPrecio = new TableColumn<>("Precio");
                colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

                // Columna de acciones
                TableColumn<ModeloHabitacion, Void> colAccion = new TableColumn<>("Acción");
                colAccion.setCellFactory(param -> new TableCell<ModeloHabitacion, Void>() {
                    private final HBox contenedor = new HBox(5);
                    private final Button btnInfo = new Button("Info");
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnBorrar = new Button("Borrar");

                    {
                        btnInfo.getStyleClass().add("btn-info");
                        btnInfo.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                        btnInfo.setOnAction(e -> verDetalleHabitacion(getTableView().getItems().get(getIndex()), codigoHotel));

                        btnEditar.getStyleClass().add("btn-edit");
                        btnEditar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
                        btnEditar.setOnAction(e -> editarHabitacion(getTableView().getItems().get(getIndex()), codigoHotel));

                        btnBorrar.getStyleClass().add("btn-delete");
                        btnBorrar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        btnBorrar.setOnAction(e -> confirmarBorradoHabitacion(getTableView().getItems().get(getIndex())));

                        contenedor.getChildren().addAll(btnInfo, btnEditar, btnBorrar);
                        contenedor.setAlignment(Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(contenedor);
                        }
                    }
                });

                tabla.getColumns().addAll(colId, colEstilo, colPrecio, colAccion);

                // Añadir datos
                for (int i = 0; i < habitaciones.length(); i++) {
                    JSONObject h = habitaciones.getJSONObject(i);
                    datos.add(new ModeloHabitacion(
                            h.getString("id"),
                            h.getString("estilo"),
                            h.getDouble("precio"),
                            codigoHotel
                    ));
                }

                tabla.setItems(datos);
            }
        } catch (IOException e) {
            txtResultado.appendText("Error al cargar habitaciones: " + e.getMessage() + "\n");
        }
    }

    private void verDetalleHabitacion(ModeloHabitacion habitacion, String codigoHotel) {
        // Crear una nueva pestaña para los detalles de la habitación
        Tab tabDetalle = new Tab("Info: Habitación " + habitacion.getId());
        tabDetalle.setClosable(true);

        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));

        Label lblTitulo = new Label("Información de la Habitación");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 1px;");

        grid.add(new Label("ID:"), 0, 0);
        grid.add(new Label(habitacion.getId()), 1, 0);

        grid.add(new Label("Estilo:"), 0, 1);
        grid.add(new Label(habitacion.getEstilo()), 1, 1);

        grid.add(new Label("Precio:"), 0, 2);
        grid.add(new Label("$" + habitacion.getPrecio()), 1, 2);

        grid.add(new Label("Hotel ID:"), 0, 3);
        grid.add(new Label(habitacion.getCodigoHotel()), 1, 3);

        Button btnVolver = new Button("Volver");
        btnVolver.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnVolver.setOnAction(e -> tabPane.getTabs().remove(tabDetalle));

        contenedor.getChildren().addAll(lblTitulo, grid, btnVolver);

        tabDetalle.setContent(contenedor);
        tabPane.getTabs().add(tabDetalle);
        tabPane.getSelectionModel().select(tabDetalle);
    }

    private void editarHabitacion(ModeloHabitacion habitacion, String codigoHotel) {
        // Crear una nueva pestaña para editar la habitación
        Tab tabEditar = new Tab("Editar: Habitación " + habitacion.getId());
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
        grid.add(new Label(habitacion.getId()), 1, 0);

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
                habitacion.setEstilo(txtEstilo.getText().trim());
                habitacion.setPrecio(precio);

                JSONObject request = new JSONObject();
                JSONObject hab = new JSONObject();
                hab.put("id", habitacion.getId());
                hab.put("estilo", habitacion.getEstilo());
                hab.put("precio", habitacion.getPrecio());
                hab.put("codigoHotel", habitacion.getCodigoHotel());

                request.put("operacion", "modificarHabitacion");
                request.put("habitacion", hab);

                enviarPeticion(request);
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

    private void confirmarBorradoHabitacion(ModeloHabitacion habitacion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminar");
        alert.setHeaderText("¿Está seguro que desea eliminar esta habitación?");
        alert.setContentText("Habitación ID: " + habitacion.getId());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            JSONObject request = new JSONObject();
            request.put("operacion", "eliminarHabitacion");
            request.put("id", habitacion.getId());

            enviarPeticion(request);
            // Actualizar la vista
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getText().startsWith("Info: ") && tab.getText().contains(habitacion.getCodigoHotel())) {
                    Node content = tab.getContent();
                    if (content instanceof VBox) {
                        for (Node node : ((VBox) content).getChildren()) {
                            if (node instanceof TableView) {
                                @SuppressWarnings("unchecked")
                                TableView<ModeloHabitacion> tabla = (TableView<ModeloHabitacion>) node;
                                cargarHabitacionesHotel(habitacion.getCodigoHotel(), tabla);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private void conectar() {
        try {
            Socket socket = new Socket("localhost", 9999);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    private void enviarPeticion(JSONObject request) {
        try {
            writer.println(request.toString());
            String respuestaStr = reader.readLine();
            JSONObject respuesta = new JSONObject(respuestaStr);

            if (respuesta.getString("estado").equals("ok")) {
                mostrarNotificacion("✔ " + respuesta.getString("mensaje"), true);

                // Actualizar datos si es necesario
                if (request.getString("operacion").contains("Hotel")) {
                    actualizarTablasHoteles();
                } else if (request.getString("operacion").contains("Habitacion")) {
                    // Aquí podríamos tener lógica para actualizar las tablas de habitaciones
                }
            } else {
                mostrarNotificacion("✘ Error: " + respuesta.getString("mensaje"), false);
            }

        } catch (IOException e) {
            mostrarNotificacion("Error de comunicación: " + e.getMessage(), false);
        }
    }

    private void actualizarTablasHoteles() {
        // Buscar la tab de hoteles y actualizar sus tablas
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals("Gestión de Hoteles")) {
                VBox content = (VBox) tab.getContent();
                for (Node node : content.getChildren()) {
                    if (node instanceof TableView) {
                        @SuppressWarnings("unchecked")
                        TableView<ModeloHotel> tabla = (TableView<ModeloHotel>) node;
                        cargarDatosHoteles(tabla);
                        break;
                    }
                }
                break;
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

    // Clases de modelo
    public static class ModeloHotel {
        private String codigo;
        private String nombre;
        private String ubicacion;

        public ModeloHotel(String codigo, String nombre, String ubicacion) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.ubicacion = ubicacion;
        }

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getUbicacion() { return ubicacion; }
        public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    }

    public static class ModeloHabitacion {
        private String id;
        private String estilo;
        private double precio;
        private String codigoHotel;

        public ModeloHabitacion(String id, String estilo, double precio, String codigoHotel) {
            this.id = id;
            this.estilo = estilo;
            this.precio = precio;
            this.codigoHotel = codigoHotel;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEstilo() { return estilo; }
        public void setEstilo(String estilo) { this.estilo = estilo; }

        public double getPrecio() { return precio; }
        public void setPrecio(double precio) { this.precio = precio; }

        public String getCodigoHotel() { return codigoHotel; }
        public void setCodigoHotel(String codigoHotel) { this.codigoHotel = codigoHotel; }
    }


}