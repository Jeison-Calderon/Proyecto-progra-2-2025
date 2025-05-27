package Aplicacion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MenuPrincipal extends Application {

    private StackPane contenidoCentral;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gestión de Reservaciones");

        // Contenedor principal
        BorderPane root = new BorderPane();

        // Contenedor para mostrar las vistas
        contenidoCentral = new StackPane();
        root.setCenter(contenidoCentral);

        // Barra de menú
        MenuBar menuBar = new MenuBar();

        // Menú Archivo
        Menu menuArchivo = new Menu("Archivo");
        MenuItem salirItem = new MenuItem("Salir");
        salirItem.setOnAction(e -> Platform.exit());
        menuArchivo.getItems().add(salirItem);

        // Menú Gestión
        Menu menuGestion = new Menu("Gestión");
        MenuItem hotelesItem = new MenuItem("Hoteles");
        hotelesItem.setOnAction(e -> mostrarVistaHoteles());
        MenuItem habitacionesItem = new MenuItem("Habitaciones");
        habitacionesItem.setOnAction(e -> mostrarVistaHabitaciones());
        menuGestion.getItems().addAll(hotelesItem, habitacionesItem);

        // Menú Ayuda
        Menu menuAyuda = new Menu("Ayuda");
        MenuItem acercaDeItem = new MenuItem("Acerca de");
        acercaDeItem.setOnAction(e -> mostrarAcercaDe());
        menuAyuda.getItems().add(acercaDeItem);

        // Agregar menús a la barra
        menuBar.getMenus().addAll(menuArchivo, menuGestion, menuAyuda);
        root.setTop(menuBar);

        // Escena
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void mostrarVistaHoteles() {
        GraficaHotel vistaHotel = new GraficaHotel();
        contenidoCentral.getChildren().setAll(vistaHotel.getVista());
    }

    private void mostrarVistaHabitaciones() {
        VistaHabitacion vistaHabitacion = new VistaHabitacion();
        contenidoCentral.getChildren().setAll(vistaHabitacion.getVista());
    }

    private void mostrarAcercaDe() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Sistema de Gestión de Reservaciones");
        alert.setContentText("Proyecto de Programación 2");
                alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
