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
    public void start(Stage primaryStage) {
        // ... setup de UI ...
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

        //el código de red VA EN UN HILO SEPARADO
        new Thread(() -> {
            try {
                InetAddress inetAddress = InetAddress.getLocalHost();
                System.out.println("IP del cliente: " + inetAddress.getHostAddress());
                Socket echoSocket = new Socket("192.168.100.8",5001);
                PrintWriter writer = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                String entrada = reader.readLine();
                System.out.println("servidor: " + entrada);
                String salida;
                BufferedReader lectorTeclado = new BufferedReader(new InputStreamReader(System.in));
                while((salida = lectorTeclado.readLine()) != null){
                    System.out.println("a4");
                    writer.println(salida);
                    entrada = reader.readLine();
                    System.out.println("servidor: " + entrada);
                }
                reader.close();
                writer.close();
                lectorTeclado.close();
                echoSocket.close();
            } catch (IOException e) {
                System.out.println("a");
                e.printStackTrace();
            }
        }).start();
    }
    public static void main(String[] args) throws UnknownHostException { launch(args); }
}