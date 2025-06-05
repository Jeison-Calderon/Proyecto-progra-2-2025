package aplicacion.servidor;

import aplicacion.data.HabitacionesData;
import aplicacion.data.HotelesData;
import aplicacion.domain.Habitacion;
import aplicacion.domain.Hotel;
import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import aplicacion.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
                case "LISTAR_HABITACIONES":
                    manejarListarHabitaciones(salida);
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
            List<Hotel> hotelesReales = HotelesData.listar();

            // Convertir a DTOs
            List<HotelDTO> hoteles = new ArrayList<>();
            for (Hotel hotel : hotelesReales) {
                hoteles.add(new HotelDTO(hotel.getCodigoHotel(), hotel.getNombre(), hotel.getUbicacion()));
            }

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

            Hotel hotelGuardado = HotelesData.buscar(codigoGenerado);

            if (hotelGuardado != null) {
                JSONObject respuesta = new JSONObject();
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Hotel guardado correctamente");

                HotelDTO hotelGuardadoDTO = new HotelDTO(
                        hotelGuardado.getCodigoHotel(),
                        hotelGuardado.getNombre(),
                        hotelGuardado.getUbicacion()
                );
                respuesta.put("hotel", JsonUtil.hotelToJson(hotelGuardadoDTO));
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

    private void manejarListarHabitaciones(DataOutputStream salida) throws IOException {
        try {
            // ✅ USAR DATOS REALES EN LUGAR DE FALSOS
            List<Habitacion> habitacionesReales = HabitacionesData.listar();

            // Convertir a DTOs
            List<HabitacionDTO> habitaciones = new ArrayList<>();
            for (Habitacion habitacion : habitacionesReales) {
                habitaciones.add(new HabitacionDTO(
                        habitacion.getCodigo(),
                        habitacion.getEstilo(),
                        habitacion.getPrecio(),
                        habitacion.getCodigoHotel() // ✅ INCLUIR CÓDIGO DE HOTEL
                ));
            }

            JSONObject respuesta = new JSONObject();
            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Habitaciones listadas correctamente");

            JSONArray habitacionesJson = new JSONArray();
            for (HabitacionDTO habitacion : habitaciones) {
                JSONObject habJson = new JSONObject();
                habJson.put("codigo", habitacion.getCodigo());
                habJson.put("estilo", habitacion.getEstilo());
                habJson.put("precio", habitacion.getPrecio());
                habJson.put("codigoHotel", habitacion.getCodigoHotel()); // ✅ INCLUIR
                habitacionesJson.put(habJson);
            }
            respuesta.put("habitaciones", habitacionesJson);

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al listar habitaciones: " + e.getMessage());
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