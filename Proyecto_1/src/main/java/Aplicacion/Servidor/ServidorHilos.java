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
                        String nombre = datosHotel.getString("nombre");
                        String ubicacion = datosHotel.getString("ubicacion");

                        String resultado = GestorHoteles.guardar(nombre, ubicacion);

                        if (resultado.equals("duplicado")) {
                            respuesta.put("estado", "error");
                            respuesta.put("mensaje", "Ya existe un hotel con ese nombre y ubicación");
                        } else {
                            respuesta.put("estado", "ok");
                            respuesta.put("mensaje", "Hotel guardado con código " + resultado);
                        }


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

                    case "eliminarHotel":
                        String codigoEliminar = jsonEntrada.getString("codigo");
                        boolean eliminado = GestorHoteles.eliminar(codigoEliminar);
                        if (eliminado) {
                            respuesta.put("estado", "ok");
                            respuesta.put("mensaje", "Hotel eliminado correctamente");
                        } else {
                            respuesta.put("estado", "error");
                            respuesta.put("mensaje", "Hotel no encontrado");
                        }
                        break;

                    case "modificarHotel":
                        JSONObject hotelModificar = jsonEntrada.getJSONObject("hotel");
                        String codMod = hotelModificar.getString("codigo");
                        String nomMod = hotelModificar.getString("nombre");
                        String ubiMod = hotelModificar.getString("ubicacion");
                        boolean modificado = GestorHoteles.modificar(new Hotel(codMod, nomMod, ubiMod));
                        if (modificado) {
                            respuesta.put("estado", "ok");
                            respuesta.put("mensaje", "Hotel modificado correctamente");
                        } else {
                            respuesta.put("estado", "error");
                            respuesta.put("mensaje", "Hotel no encontrado");
                        }
                        break;

                    case "crearHabitacion":
                        JSONObject datosHab = jsonEntrada.getJSONObject("habitacion");
                        String estilo = datosHab.getString("estilo");
                        double precio = datosHab.getDouble("precio");

                        String codHab = GestorHabitaciones.guardar(estilo, precio);
                        if (codHab.equals("duplicado")) {
                            respuesta.put("estado", "error");
                            respuesta.put("mensaje", "Habitación ya existe con ese estilo y precio.");
                        } else {
                            respuesta.put("estado", "ok");
                            respuesta.put("mensaje", "Habitación registrada con código " + codHab);
                        }
                        break;


                    default:
                        respuesta.put("estado", "error");
                        respuesta.put("mensaje", "Operación no reconocida");
                        break;
                }

                writer.println(respuesta.toString());

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
