package aplicacion.servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ActivarServidor {
    public static void main(String[] args) {
        int puerto = 5001;
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("servidor esperando conexiones en el puerto " + puerto);
            String carpetaDestino = "archivos_recibidos";
            Files.createDirectories(Paths.get(carpetaDestino)); // crea carpeta si no existe

            while (true) {
                Socket cliente = serverSocket.accept();
                System.out.println("cliente conectado desde " + cliente.getInetAddress());
                manejarCliente(cliente, carpetaDestino);
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }

    private static void manejarCliente(Socket cliente, String carpetaDestino) {
        try (
                DataInputStream in = new DataInputStream(cliente.getInputStream());
                DataOutputStream out = new DataOutputStream(cliente.getOutputStream());
        ) {
            String nombreArchivo = in.readUTF();

            out.writeUTF("OK");
            FileOutputStream fos = new FileOutputStream(carpetaDestino + File.separator + nombreArchivo);
            byte[] buffer = new byte[4096];
            int leido;
            while ((leido = in.read(buffer)) > 0) {
                fos.write(buffer, 0, leido);
            }
            fos.close();
            System.out.println("Archivo recibido: " + nombreArchivo);
        } catch (IOException e) {
            System.out.println("Error al recibir archivo: " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (IOException ignored) {}
        }
    }
}
