package aplicacion.servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ActivarServidor {
    public static final int PUERTO = 5001;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("servidor esperando conexiones en el puerto " + PUERTO);
            String carpetaDestino = "archivos_recibidos";
            Files.createDirectories(Paths.get(carpetaDestino)); // crea carpeta si no existe

            while (true) {
                Socket cliente = serverSocket.accept();
                System.out.println("cliente conectado desde " + cliente.getInetAddress());

                // Crear un nuevo hilo para manejar la conexión
                ServidorHilos hiloCliente = new ServidorHilos(cliente);
                hiloCliente.start();
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
}