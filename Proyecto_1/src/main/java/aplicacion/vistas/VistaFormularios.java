package aplicacion.vistas;

import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class VistaFormularios {

    // ✅ FORMULARIO PARA NUEVO HOTEL
    public static Optional<HotelDTO> mostrarFormularioNuevoHotel() {
        Dialog<HotelDTO> dialog = new Dialog<>();
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
                String nombre = txtNombre.getText().trim();
                String ubicacion = txtUbicacion.getText().trim();
                if (!nombre.isEmpty() && !ubicacion.isEmpty()) {
                    return new HotelDTO("", nombre, ubicacion);
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ✅ FORMULARIO PARA NUEVA HABITACIÓN
    public static Optional<HabitacionDTO> mostrarFormularioNuevaHabitacion(HotelDTO hotel) {
        Dialog<HabitacionDTO> dialog = new Dialog<>();
        dialog.setTitle("Crear Nueva Habitación");
        dialog.setHeaderText("Ingrese los datos de la nueva habitación para: " + hotel.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtEstilo = new TextField();
        TextField txtPrecio = new TextField();

        grid.add(new Label("Estilo:"), 0, 0);
        grid.add(txtEstilo, 1, 0);
        grid.add(new Label("Precio:"), 0, 1);
        grid.add(txtPrecio, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                String estilo = txtEstilo.getText().trim();
                String precioStr = txtPrecio.getText().trim();
                if (!estilo.isEmpty() && !precioStr.isEmpty()) {
                    try {
                        double precio = Double.parseDouble(precioStr);
                        return new HabitacionDTO("", estilo, precio, hotel.getCodigo());
                    } catch (NumberFormatException e) {
                        mostrarError("Precio debe ser un número válido");
                    }
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ✅ FORMULARIO PARA EDITAR HOTEL
    public static Optional<HotelDTO> mostrarFormularioEditarHotel(HotelDTO hotel) {
        Dialog<HotelDTO> dialog = new Dialog<>();
        dialog.setTitle("Editar Hotel");
        dialog.setHeaderText("Modifique los datos del hotel");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtNombre = new TextField(hotel.getNombre());
        TextField txtUbicacion = new TextField(hotel.getUbicacion());

        grid.add(new Label("ID:"), 0, 0);
        grid.add(new Label(hotel.getCodigo()), 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(txtNombre, 1, 1);
        grid.add(new Label("Ubicación:"), 0, 2);
        grid.add(txtUbicacion, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                String nombre = txtNombre.getText().trim();
                String ubicacion = txtUbicacion.getText().trim();
                if (!nombre.isEmpty() && !ubicacion.isEmpty()) {
                    return new HotelDTO(hotel.getCodigo(), nombre, ubicacion);
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ✅ FORMULARIO PARA EDITAR HABITACIÓN
    public static Optional<HabitacionDTO> mostrarFormularioEditarHabitacion(HabitacionDTO habitacion) {
        Dialog<HabitacionDTO> dialog = new Dialog<>();
        dialog.setTitle("Editar Habitación");
        dialog.setHeaderText("Modifique los datos de la habitación");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtEstilo = new TextField(habitacion.getEstilo());
        TextField txtPrecio = new TextField(String.valueOf(habitacion.getPrecio()));

        grid.add(new Label("ID:"), 0, 0);
        grid.add(new Label(habitacion.getCodigo()), 1, 0);
        grid.add(new Label("Estilo:"), 0, 1);
        grid.add(txtEstilo, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(txtPrecio, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                String estilo = txtEstilo.getText().trim();
                String precioStr = txtPrecio.getText().trim();
                if (!estilo.isEmpty() && !precioStr.isEmpty()) {
                    try {
                        double precio = Double.parseDouble(precioStr);
                        return new HabitacionDTO(habitacion.getCodigo(), estilo, precio, habitacion.getCodigoHotel());
                    } catch (NumberFormatException e) {
                        mostrarError("Precio debe ser un número válido");
                    }
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ✅ CONFIRMACIÓN DE ELIMINACIÓN
    public static boolean confirmarEliminacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminar");
        alert.setHeaderText("¿Está seguro que desea eliminar este " + titulo + "?");
        alert.setContentText(mensaje);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ✅ MOSTRAR ERROR
    public static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}