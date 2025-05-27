package Aplicacion.Grafica;

import javafx.application.Application;
import javafx.stage.Stage;

public class GraficaPrincipal extends Application {

    @Override
    public void start(Stage primaryStage) {
        MenuPrincipal dashboard = new MenuPrincipal();
        dashboard.mostrarMenu(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
