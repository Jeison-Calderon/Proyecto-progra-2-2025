package Aplicacion.Grafica;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GraficaPrincipal extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        MenuPrincipal menu = new MenuPrincipal();
        BorderPane menuVista = menu.getVista();

        root.setCenter(menuVista.getCenter());
        root.setTop(menuVista.getTop());
        root.setBottom(menuVista.getBottom());

        Scene scene = new Scene(root, 900, 600);
        // Aplicamos un CSS para la apariencia moderna
        scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());

        primaryStage.setTitle("Sistema de Gestión de Hoteles");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}