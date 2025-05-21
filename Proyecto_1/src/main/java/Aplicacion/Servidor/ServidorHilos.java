package Aplicacion.Servidor;

import java.io.*;
import java.net.Socket;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONObject;

public class ServidorHilos extends Thread {
    private Socket socket;

    public ServidorHilos(Socket socket) {
        super("Aplicación.Servidor.ServidorHilos");
        this.socket = socket;
    }

    public void run() {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String entrada;

            while ((entrada = reader.readLine()) != null) {
                System.out.println("JSON recibido del cliente: " + entrada);

                JSONObject jsonEntrada = new JSONObject(entrada);
                String operacion = jsonEntrada.optString("operacion");

                JSONObject respuesta = new JSONObject();

                switch (operacion) {
                    case "crearHotel":
                        JSONObject datosHotel = jsonEntrada.getJSONObject("hotel");
                        String codigo = GestorHoteles.generarCodigo();
                        String nombre = datosHotel.getString("nombre");
                        String ubicacion = datosHotel.getString("ubicacion");

                        Hotel hotel = new Hotel(codigo, nombre, ubicacion);
                        GestorHoteles.guardar(hotel);

                        respuesta.put("estado", "ok");
                        respuesta.put("mensaje", "Aplicación.Servidor.Hotel guardado con código " + codigo);
                        break;

                    case "listarHoteles":
                        List<Hotel> hoteles = GestorHoteles.listar();
                        JSONArray arr = new JSONArray();
                        for (Hotel h : hoteles) {
                            JSONObject obj = new JSONObject();
                            obj.put("codigo", h.getCodigoHotel());
                            obj.put("nombre", h.getNombre());
                            obj.put("ubicacion", h.getUbicacion());
                            arr.put(obj);
                        }
                        respuesta.put("estado", "ok");
                        respuesta.put("hoteles", arr);
                        break;

                    default:
                        respuesta.put("estado", "error");
                        respuesta.put("mensaje", "Operación no reconocida");
                        break;
                }

                writer.println(respuesta.toString());

                // Si quieres que "Chao!" finalice la conexión
                if (operacion.equalsIgnoreCase("salir")) {
                    break;
                }
            }

            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}