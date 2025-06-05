package aplicacion.servidor;

import aplicacion.data.HabitacionesData;
import aplicacion.data.HotelesData;
import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import aplicacion.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServidorHilos extends Thread {
    private Socket cliente;

    public ServidorHilos(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    public void run() {
        try (DataInputStream entrada = new DataInputStream(cliente.getInputStream());
             DataOutputStream salida = new DataOutputStream(cliente.getOutputStream())) {

            String operacion = entrada.readUTF();
            System.out.println("Operación recibida: " + operacion);

            switch (operacion) {
                case "LISTAR_HOTELES":
                    manejarListarHoteles(salida);
                    break;
                case "GUARDAR_HOTEL":
                    manejarGuardarHotel(entrada, salida);
                    break;
                case "ELIMINAR_HOTEL":
                    manejarEliminarHotel(entrada, salida);
                    break;
                case "MODIFICAR_HOTEL":
                    manejarModificarHotel(entrada, salida);
                    break;
                case "LISTAR_HABITACIONES":
                    manejarListarHabitaciones(salida);
                    break;
                case "GUARDAR_HABITACION":
                    manejarGuardarHabitacion(entrada, salida);
                    break;
                case "ELIMINAR_HABITACION":
                    manejarEliminarHabitacion(entrada, salida);
                    break;
                case "MODIFICAR_HABITACION":
                    manejarModificarHabitacion(entrada, salida);
                    break;
                case "ENVIAR_ARCHIVO":
                    manejarEnviarArchivo(entrada, salida);
                    break;
                default:
                    enviarError(salida, "Operación no reconocida: " + operacion);
                    break;
            }

        } catch (IOException e) {
            System.out.println("Error manejando cliente: " + e.getMessage());
        } finally {
            try {
                cliente.close();
                System.out.println("Cliente desconectado");
            } catch (IOException e) {
                System.out.println("Error cerrando conexión: " + e.getMessage());
            }
        }
    }

    private void manejarListarHoteles(DataOutputStream salida) throws IOException {
        try {
            // ✅ CORREGIDO: Usar directamente DTOs
            List<HotelDTO> hoteles = HotelesData.listar();

            JSONObject respuesta = new JSONObject();
            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Hoteles listados correctamente");

            JSONArray hotelesJson = new JSONArray();
            for (HotelDTO hotel : hoteles) {
                hotelesJson.put(JsonUtil.hotelToJson(hotel));
            }
            respuesta.put("hoteles", hotelesJson);

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al listar hoteles: " + e.getMessage());
        }
    }

    private void manejarGuardarHotel(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String hotelJson = entrada.readUTF();
            JSONObject jsonHotel = new JSONObject(hotelJson);
            HotelDTO hotelDTO = JsonUtil.jsonToHotel(jsonHotel);

            String codigoGenerado = HotelesData.guardar(hotelDTO.getNombre(), hotelDTO.getUbicacion());

            if ("duplicado".equals(codigoGenerado)) {
                JSONObject respuesta = new JSONObject();
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Hotel duplicado: ya existe un hotel con ese nombre y ubicación");
                salida.writeUTF(respuesta.toString());
                return;
            }

            // ✅ CORREGIDO: Usar directamente DTOs
            HotelDTO hotelGuardado = HotelesData.buscar(codigoGenerado);

            if (hotelGuardado != null) {
                JSONObject respuesta = new JSONObject();
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Hotel guardado correctamente");
                respuesta.put("hotel", JsonUtil.hotelToJson(hotelGuardado));
                salida.writeUTF(respuesta.toString());
            } else {
                enviarError(salida, "Error: No se pudo recuperar el hotel guardado");
            }

        } catch (Exception e) {
            enviarError(salida, "Error al guardar hotel: " + e.getMessage());
        }
    }

    private void manejarEliminarHotel(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String codigo = entrada.readUTF();
            boolean eliminado = HotelesData.eliminar(codigo);

            JSONObject respuesta = new JSONObject();
            if (eliminado) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Hotel eliminado correctamente");
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Hotel no encontrado");
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al eliminar hotel: " + e.getMessage());
        }
    }

    // ✅ NUEVO: Método para modificar hotel
    private void manejarModificarHotel(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String hotelJson = entrada.readUTF();
            JSONObject jsonHotel = new JSONObject(hotelJson);
            HotelDTO hotelDTO = JsonUtil.jsonToHotel(jsonHotel);

            boolean modificado = HotelesData.modificar(hotelDTO);

            JSONObject respuesta = new JSONObject();
            if (modificado) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Hotel modificado correctamente");
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Hotel no encontrado");
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al modificar hotel: " + e.getMessage());
        }
    }

    private void manejarListarHabitaciones(DataOutputStream salida) throws IOException {
        try {
            // ✅ CORREGIDO: Usar directamente DTOs
            List<HabitacionDTO> habitaciones = HabitacionesData.listar();

            JSONObject respuesta = new JSONObject();
            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Habitaciones listadas correctamente");

            JSONArray habitacionesJson = new JSONArray();
            for (HabitacionDTO habitacion : habitaciones) {
                habitacionesJson.put(JsonUtil.habitacionToJson(habitacion));
            }
            respuesta.put("habitaciones", habitacionesJson);

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al listar habitaciones: " + e.getMessage());
        }
    }

    // ✅ NUEVO: Método para guardar habitación
    private void manejarGuardarHabitacion(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String habitacionJson = entrada.readUTF();
            JSONObject jsonHab = new JSONObject(habitacionJson);

            String estilo = jsonHab.getString("estilo");
            double precio = jsonHab.getDouble("precio");
            String codigoHotel = jsonHab.getString("codigoHotel");

            String codigo = HabitacionesData.guardar(estilo, precio, codigoHotel);

            JSONObject respuesta = new JSONObject();
            if ("duplicado".equals(codigo)) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Habitación duplicada");
            } else {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Habitación guardada correctamente");
                respuesta.put("codigo", codigo);
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al guardar habitación: " + e.getMessage());
        }
    }

    // ✅ NUEVO: Método para eliminar habitación
    private void manejarEliminarHabitacion(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String codigo = entrada.readUTF();
            boolean eliminada = HabitacionesData.eliminar(codigo);

            JSONObject respuesta = new JSONObject();
            if (eliminada) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Habitación eliminada correctamente");
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Habitación no encontrada");
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al eliminar habitación: " + e.getMessage());
        }
    }

    // ✅ NUEVO: Método para modificar habitación
    private void manejarModificarHabitacion(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String habitacionJson = entrada.readUTF();
            JSONObject jsonHab = new JSONObject(habitacionJson);
            HabitacionDTO habitacionDTO = JsonUtil.jsonToHabitacion(jsonHab);

            boolean modificada = HabitacionesData.modificar(habitacionDTO);

            JSONObject respuesta = new JSONObject();
            if (modificada) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Habitación modificada correctamente");
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Habitación no encontrada");
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al modificar habitación: " + e.getMessage());
        }
    }

    private void manejarEnviarArchivo(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String nombreArchivo = entrada.readUTF();
            salida.writeUTF("OK");

            String rutaDestino = "archivos_recibidos/" + nombreArchivo;
            try (FileOutputStream fos = new FileOutputStream(rutaDestino)) {
                byte[] buffer = new byte[4096];
                int leido;
                while ((leido = entrada.read(buffer)) > 0) {
                    fos.write(buffer, 0, leido);
                }
            }

            JSONObject respuesta = new JSONObject();
            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Archivo recibido: " + nombreArchivo);
            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al recibir archivo: " + e.getMessage());
        }
    }

    private void enviarError(DataOutputStream salida, String mensaje) throws IOException {
        JSONObject respuesta = new JSONObject();
        respuesta.put("estado", "ERROR");
        respuesta.put("mensaje", mensaje);
        salida.writeUTF(respuesta.toString());
    }
}