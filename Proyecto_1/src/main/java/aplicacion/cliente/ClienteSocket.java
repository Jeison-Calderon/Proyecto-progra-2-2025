package aplicacion.cliente;

import org.json.JSONObject;
import java.io.*;
import java.net.Socket;

public class ClienteSocket {
    private static final String SERVIDOR_IP = "10.59.18.226";
    private static final int SERVIDOR_PUERTO = 5001;

    public String enviarOperacion(String operacion) throws IOException {
        return enviarOperacion(operacion, null);
    }

    public String enviarOperacion(String operacion, String datos) throws IOException {
        try (Socket socket = new Socket(SERVIDOR_IP, SERVIDOR_PUERTO);
             DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
             DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

            // Enviar operación
            salida.writeUTF(operacion);

            // Enviar datos si los hay
            if (datos != null && !datos.isEmpty()) {
                salida.writeUTF(datos);
            }

            // Recibir respuesta
            return entrada.readUTF();

        } catch (IOException e) {
            throw new IOException("Error conectando al servidor en " + SERVIDOR_IP + ":" + SERVIDOR_PUERTO + " - " + e.getMessage(), e);
        }
    }
}