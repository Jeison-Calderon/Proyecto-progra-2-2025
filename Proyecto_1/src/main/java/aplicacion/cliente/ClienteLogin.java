package aplicacion.cliente;

import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class ClienteLogin {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.print("Usuario: ");
            String username = teclado.readLine();

            System.out.print("Contraseña: ");
            String password = teclado.readLine();

            JSONObject request = new JSONObject();
            request.put("tipo", "login");
            request.put("username", username);
            request.put("password", password);

            out.println(request.toString());

            String respuesta = in.readLine();
            JSONObject response = new JSONObject(respuesta);

            System.out.println("Servidor: " + response.getString("mensaje"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
