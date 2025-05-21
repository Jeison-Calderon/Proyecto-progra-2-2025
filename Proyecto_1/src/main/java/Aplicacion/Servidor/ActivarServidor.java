package Aplicacion.Servidor;

import java.net.ServerSocket;
import java.io.IOException;


public class ActivarServidor {
    public static void main(String[] args) {
        ServerSocket serverSocket = null; // Este socket espera por
        // una conexión entrante
        boolean escuchando = true;

        try {
            serverSocket = new ServerSocket(9999);
            System.out.println("Servidor activo");
            while(escuchando){
                ServidorHilos hilo =
                        new ServidorHilos(serverSocket.accept());
                hilo.start();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
