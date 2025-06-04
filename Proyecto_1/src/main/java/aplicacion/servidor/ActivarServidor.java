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
            while (true) {
                String nombreArchivo = in.readUTF();
                if (nombreArchivo.equals("FIN")) {
                    break;
                }

                long tamanoArchivo = in.readLong();
                out.writeUTF("OK");

                FileOutputStream fos = new FileOutputStream(carpetaDestino + File.separator + nombreArchivo);
                byte[] buffer = new byte[4096];
                long bytesRecibidos = 0;
                while (bytesRecibidos < tamanoArchivo) {
                    int bytesToRead = (int) Math.min(buffer.length, tamanoArchivo - bytesRecibidos);
                    int leido = in.read(buffer, 0, bytesToRead);
                    if (leido == -1) break;
                    fos.write(buffer, 0, leido);
                    bytesRecibidos += leido;
                }
                fos.close();
                System.out.println("Archivo recibido: " + nombreArchivo);
            }
        } catch (IOException e) {
            System.out.println("Error al recibir archivo: " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (IOException ignored) {}
        }
    }
}
