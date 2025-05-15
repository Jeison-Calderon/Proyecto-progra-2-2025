package cr.ac.ucr.paraiso.progra2.javafxapp.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class MainApp extends Application {

    private static BorderPane root = new BorderPane();
    private static Socket socket = null;

    public static Socket createSocket() throws IOException{
        if (socket==null){
            //InetAddress inetAddress = InetAddress.getLocalHost();
            socket = new Socket("192.168.56.1", 9999);
        }
        return socket;
    }

    public static BorderPane getRoot() {
        return root;
    }

    public static Socket getSocket(){
        return socket;
    }
    @Override
    public void start(Stage primaryStage) throws IOException{
        MenuBar menuBar = (MenuBar)
                FXMLLoader.load(getClass().getResource("/main-menubar.fxml"));
        AnchorPane anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/main-pane.fxml"));
        root.setTop(menuBar);
        root.setCenter(anchorPane);
        Scene scene = new Scene(root, 640, 480);

        primaryStage.setTitle("Mùltiples formularios");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        createSocket(); //metodo que crea el socket
    }
    public static void main(String[] args){
    launch();
}
}
