package Aplicacion.Grafica;

import Aplicacion.Grafica.MenuPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GraficaPrincipal extends Application {



    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();

        // Tab general del dashboard
        BorderPane dashboardPane = new BorderPane();
        MenuPrincipal menu = new MenuPrincipal();
        menu.construirMenu(dashboardPane, tabPane); // pasa el TabPane para que agregue pestañas por hotel

        Tab tabDashboard = new Tab("Dashboard", dashboardPane);
        tabDashboard.setClosable(false);

        tabPane.getTabs().add(tabDashboard);

        Scene scene = new Scene(tabPane, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gestión de Reservaciones por Hotel");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
