package aplicacion.cliente;

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

            // Si llegamos aquí, el servidor está disponible
            BorderPane root = new BorderPane();
            MenuPrincipal menu = new MenuPrincipal();
            BorderPane menuVista = menu.getVista();

            root.setCenter(menuVista.getCenter());
            root.setTop(menuVista.getTop());
            root.setBottom(menuVista.getBottom());

            Scene scene = new Scene(root, 900, 600);

            // Cargar CSS si existe
            try {
                scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("No se pudo cargar estilos.css");
            }

            primaryStage.setTitle("Sistema de Gestión de Hoteles - Cliente");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            // Error de conexión
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Conexión");
            alert.setHeaderText("No se pudo conectar al servidor");
            alert.setContentText("Verifique que el servidor esté ejecutándose en 10.59.18.141:5001\n\nError: " + e.getMessage());
            alert.showAndWait();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}