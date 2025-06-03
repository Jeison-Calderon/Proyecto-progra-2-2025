package aplicacion.grafica;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class GraficaPrincipal extends Application {

    @Override
    public void start(Stage primaryStage) throws UnknownHostException {
        BorderPane root = new BorderPane();
        MenuPrincipal menu = new MenuPrincipal();
        BorderPane menuVista = menu.getVista();

        root.setCenter(menuVista.getCenter());
        root.setTop(menuVista.getTop());
        root.setBottom(menuVista.getBottom());

        Scene scene = new Scene(root, 900, 600);

        scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());

        primaryStage.setTitle("Sistema de Gestión de Hoteles");
        primaryStage.setScene(scene);
        primaryStage.show();

        InetAddress inetAddress =
                InetAddress.getLocalHost();

        Socket echoSocket = null;
        PrintWriter writer = null;
        BufferedReader reader = null;

        try {
            echoSocket = new Socket("192.168.0.181",5001); //la ip q va aqui es la del server
            writer = new PrintWriter(echoSocket.getOutputStream(), true);
            reader = new BufferedReader(
                    new InputStreamReader(
                            echoSocket.getInputStream()));
            String entrada = reader.readLine();
            System.out.println("servidor: " + entrada);
            String salida;
            BufferedReader lectorTeclado = new BufferedReader(
                    new InputStreamReader(System.in));
            while((salida = lectorTeclado.readLine()) != null){
                writer.println(salida);
                entrada = reader.readLine();
                System.out.println("servidor: " + entrada);
            }//while

            reader.close();
            writer.close();
            lectorTeclado.close();
            echoSocket.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws UnknownHostException {
        launch(args);




    }
}