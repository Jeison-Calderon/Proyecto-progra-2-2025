package Aplicacion;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class GraficaPrincipal extends Application {

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();

        Tab tabHoteles = new Tab("Hoteles", new GraficaHotel().getVista());
        tabHoteles.setClosable(false);

        Tab tabHabitaciones = new Tab("Habitaciones", new GraficaHabitaciones().getVista());
        tabHabitaciones.setClosable(false);

        tabPane.getTabs().addAll(tabHoteles, tabHabitaciones);

        primaryStage.setTitle("Gestión de reservaciones");
        primaryStage.setScene(new Scene(tabPane, 600, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
