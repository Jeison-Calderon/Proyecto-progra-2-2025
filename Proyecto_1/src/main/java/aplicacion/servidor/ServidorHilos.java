package aplicacion.servidor;

import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import aplicacion.dto.RespuestaDTO;
import aplicacion.util.JsonUtil;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class ServidorHilos extends Thread {
    private Socket socket;
    private static final String CARPETA_ARCHIVOS = "archivos_recibidos";

    public ServidorHilos(Socket socket) {
        super("ServidorHilos");
        this.socket = socket;
    }

    public void run() {
        try (
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream())
        ) {
            // Leer el tipo de operación solicitada
            String operacion = entrada.readUTF();
            System.out.println("Operación solicitada: " + operacion);

            // Procesar según el tipo de operación
            switch(operacion) {
                case "ENVIAR_ARCHIVO":
                    procesarEnvioArchivo(entrada, salida);
                    break;
                case "LISTAR_HOTELES":
                    enviarListaHoteles(salida);
                    break;
                case "GUARDAR_HOTEL":
                    procesarGuardarHotel(entrada, salida);
                    break;
                case "ELIMINAR_HOTEL":
                    procesarEliminarHotel(entrada, salida);
                    break;
                case "LISTAR_HABITACIONES":
                    enviarListaHabitaciones(salida);
                    break;
                default:
                    RespuestaDTO<Object> respuestaError = new RespuestaDTO<>("ERROR", "Operación no reconocida");
                    salida.writeUTF(new JSONObject(respuestaError).toString());
            }

        } catch (IOException e) {
            System.err.println("Error en hilo del servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

    private void procesarEnvioArchivo(DataInputStream entrada, DataOutputStream salida) throws IOException {
        // Recibir nombre de archivo
        String nombreArchivo = entrada.readUTF();
        System.out.println("Recibiendo archivo: " + nombreArchivo);

        // Confirmar al cliente que estamos listos para recibir
        salida.writeUTF("OK");

        // Guardar el archivo recibido
        File archivo = new File(CARPETA_ARCHIVOS + File.separator + nombreArchivo);
        FileOutputStream fos = new FileOutputStream(archivo);
        byte[] buffer = new byte[4096];
        int leido;

        while ((leido = entrada.read(buffer)) > 0) {
            fos.write(buffer, 0, leido);
        }

        fos.close();
        System.out.println("Archivo recibido: " + nombreArchivo);

        // Enviar respuesta de éxito
        RespuestaDTO<String> respuesta = new RespuestaDTO<>("OK", "Archivo recibido correctamente", nombreArchivo);
        salida.writeUTF(new JSONObject(respuesta).toString());
    }

    private void enviarListaHoteles(DataOutputStream salida) throws IOException {
        // Crear lista de hoteles de ejemplo
        List<HotelDTO> hoteles = new ArrayList<>();

        // Agregar algunos hoteles de ejemplo
        hoteles.add(new HotelDTO("H001", "Hotel Paraíso", "Playa del Carmen"));
        hoteles.add(new HotelDTO("H002", "Gran Hotel", "Ciudad de México"));
        hoteles.add(new HotelDTO("H003", "Hotel Boutique", "San Miguel de Allende"));

        // Crear respuesta con la lista de hoteles
        RespuestaDTO<List<HotelDTO>> respuesta = new RespuestaDTO<>("OK", "Lista de hoteles obtenida", hoteles);

        // Convertir a JSON y enviar
        JSONObject jsonRespuesta = new JSONObject();
        jsonRespuesta.put("estado", respuesta.getEstado());
        jsonRespuesta.put("mensaje", respuesta.getMensaje());
        jsonRespuesta.put("hoteles", JsonUtil.hotelesToJson(hoteles));

        salida.writeUTF(jsonRespuesta.toString());
    }

    private void enviarListaHabitaciones(DataOutputStream salida) throws IOException {
        // Crear lista de habitaciones de ejemplo
        List<HabitacionDTO> habitaciones = new ArrayList<>();

        // Agregar habitaciones de ejemplo
        habitaciones.add(new HabitacionDTO("HAB001", "Suite", 1500.0));
        habitaciones.add(new HabitacionDTO("HAB002", "Doble", 800.0));
        habitaciones.add(new HabitacionDTO("HAB003", "Individual", 500.0));

        // Crear respuesta con la lista de habitaciones
        RespuestaDTO<List<HabitacionDTO>> respuesta = new RespuestaDTO<>("OK", "Lista de habitaciones obtenida", habitaciones);

        // Convertir a JSON y enviar
        JSONObject jsonRespuesta = new JSONObject();
        jsonRespuesta.put("estado", respuesta.getEstado());
        jsonRespuesta.put("mensaje", respuesta.getMensaje());
        jsonRespuesta.put("habitaciones", JsonUtil.habitacionesToJson(habitaciones));

        salida.writeUTF(jsonRespuesta.toString());
    }

    private void procesarGuardarHotel(DataInputStream entrada, DataOutputStream salida) throws IOException {
        // Leer datos del hotel
        String datoHotel = entrada.readUTF();
        JSONObject jsonHotel = new JSONObject(datoHotel);

        // Convertir a DTO
        HotelDTO hotel = JsonUtil.jsonToHotel(jsonHotel);

        // En una implementación real, aquí guardaríamos en base de datos
        System.out.println("Guardando hotel: " + hotel.toString());

        // Generar código si no existe
        if (hotel.getCodigo() == null || hotel.getCodigo().isEmpty()) {
            hotel.setCodigo("H" + System.currentTimeMillis() % 10000);
        }

        // Crear respuesta de éxito
        RespuestaDTO<HotelDTO> respuesta = new RespuestaDTO<>("OK",
                "Hotel guardado con código " + hotel.getCodigo(), hotel);

        // Convertir a JSON y enviar
        JSONObject jsonRespuesta = new JSONObject();
        jsonRespuesta.put("estado", respuesta.getEstado());
        jsonRespuesta.put("mensaje", respuesta.getMensaje());
        jsonRespuesta.put("hotel", JsonUtil.hotelToJson(hotel));

        salida.writeUTF(jsonRespuesta.toString());
    }

    private void procesarEliminarHotel(DataInputStream entrada, DataOutputStream salida) throws IOException {
        // Leer código del hotel a eliminar
        String codigoHotel = entrada.readUTF();

        // En una implementación real, eliminaríamos de la base de datos
        System.out.println("Eliminando hotel con código: " + codigoHotel);

        // Crear respuesta de éxito
        RespuestaDTO<String> respuesta = new RespuestaDTO<>("OK",
                "Hotel eliminado correctamente", codigoHotel);

        // Convertir a JSON y enviar
        JSONObject jsonRespuesta = new JSONObject();
        jsonRespuesta.put("estado", respuesta.getEstado());
        jsonRespuesta.put("mensaje", respuesta.getMensaje());
        jsonRespuesta.put("codigo", respuesta.getDatos());

        salida.writeUTF(jsonRespuesta.toString());
    }
}