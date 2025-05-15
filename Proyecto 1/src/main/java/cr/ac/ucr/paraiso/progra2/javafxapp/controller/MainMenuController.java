package cr.ac.ucr.paraiso.progra2.javafxapp.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class MainMenuController {

    public void exit(ActionEvent event) throws IOException{
        Platform.exit();
    }

    public void borrar(ActionEvent event) {
    }

    public void loadMcdPane(ActionEvent actionEvent) throws IOException{
        GridPane gridPane = (GridPane)
                FXMLLoader.load(getClass().getResource("/mcd.fxml"));
        gridPane.setPadding(new Insets(10,0,0,20));
        BorderPane borderPane= MainApp.getRoot();
        borderPane.setCenter(gridPane);
    }
}
