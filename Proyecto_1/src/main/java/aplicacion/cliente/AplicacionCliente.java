package aplicacion.cliente;

import aplicacion.grafica.LoginView;
import aplicacion.grafica.MenuPrincipal;
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
            cliente.enviarOperacion("LISTAR_HOTELES");

            // Mostrar login primero
            LoginView loginView = new LoginView();
            loginView.mostrar(primaryStage, (usuario) -> {
                // Si login fue exitoso, cargar la aplicación principal
                BorderPane root = new BorderPane();
                MenuPrincipal menu = new MenuPrincipal();
                BorderPane menuVista = menu.getVista();

                root.setCenter(menuVista.getCenter());
                root.setTop(menuVista.getTop());
                root.setBottom(menuVista.getBottom());

                Scene scene = new Scene(root, 900, 600);
                try {
                    scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
                } catch (Exception e) {
                    System.out.println("No se pudo cargar estilos.css");
                }

                primaryStage.setScene(scene);
                primaryStage.setTitle("Sistema de Gestión de Hoteles - Cliente");
                primaryStage.show();
            });

        } catch (Exception e) {
            // Error de conexión
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Conexión");
            alert.setHeaderText("No se pudo conectar al servidor");
            alert.setContentText("Verifique que el servidor esté ejecutándose en la IP y puerto correcto\n\nError: " + e.getMessage());
            alert.showAndWait();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
