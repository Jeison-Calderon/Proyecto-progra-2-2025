package aplicacion.cliente;

import aplicacion.dto.Usuario.TipoUsuario;
import aplicacion.grafica.LoginView;
import aplicacion.grafica.MenuPrincipal;
import aplicacion.grafica.MenuHuesped;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AplicacionCliente extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Probar conexión al servidor
            ClienteSocket cliente = new ClienteSocket();
            cliente.enviarOperacion("LISTAR_HOTELES", null);

            // Mostrar login primero
            LoginView loginView = new LoginView();
            loginView.mostrar(primaryStage, (usuario, tipoUsuario) -> {
                // Si login fue exitoso, cargar la aplicación según el tipo de usuario
                if (tipoUsuario == TipoUsuario.RECEPCIONISTA) {
                    cargarMenuRecepcionista(primaryStage, usuario);
                } else {
                    cargarMenuHuesped(primaryStage, usuario);
                }
            });

        } catch (Exception e) {
            mostrarErrorConexion(e);
            System.exit(1);
        }
    }

    private void cargarMenuRecepcionista(Stage stage, String usuario) {
        try {
            BorderPane root = new BorderPane();
            MenuPrincipal menu = new MenuPrincipal();
            BorderPane menuVista = menu.getVista();

            root.setCenter(menuVista.getCenter());
            root.setTop(menuVista.getTop());
            root.setBottom(menuVista.getBottom());

            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Hoteles - Recepcionista: " + usuario);
            stage.show();
        } catch (Exception e) {
            mostrarError("Error", "Error cargando la interfaz de recepcionista: " + e.getMessage());
        }
    }

    private void cargarMenuHuesped(Stage stage, String usuario) {
        try {
            BorderPane root = new BorderPane();
            MenuHuesped menu = new MenuHuesped(usuario);
            BorderPane menuVista = menu.getVista();

            // Establecer el contenido del menú de huésped
            root.setCenter(menuVista.getCenter());
            root.setTop(menuVista.getTop());
            root.setBottom(menuVista.getBottom());

            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Sistema de Reservas - Huésped: " + usuario);
            stage.show();
        } catch (Exception e) {
            mostrarError("Error", "Error cargando la interfaz de huésped: " + e.getMessage());
        }
    }

    private void mostrarErrorConexion(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText("No se pudo conectar al servidor");
        alert.setContentText("Verifique que el servidor esté ejecutándose\n\nError: " + e.getMessage());
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}