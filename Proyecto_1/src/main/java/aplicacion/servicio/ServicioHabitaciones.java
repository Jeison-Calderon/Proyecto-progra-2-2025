package aplicacion.servicio;

import aplicacion.cliente.ClienteSocket;
import aplicacion.dto.HabitacionDTO;
import aplicacion.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServicioHabitaciones {
    private ClienteSocket cliente;

    public ServicioHabitaciones() {
        this.cliente = new ClienteSocket();
    }

    /**
     * Lista todas las habitaciones desde el servidor
     */
    public List<HabitacionDTO> listarHabitaciones() throws IOException {
        String respuestaJson = cliente.enviarOperacion("LISTAR_HABITACIONES");
        JSONObject respuesta = new JSONObject(respuestaJson);

        if (!"OK".equals(respuesta.getString("estado"))) {
            throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
        }

        List<HabitacionDTO> habitaciones = new ArrayList<>();
        JSONArray habitacionesJson = respuesta.getJSONArray("habitaciones");

        for (int i = 0; i < habitacionesJson.length(); i++) {
            JSONObject habJson = habitacionesJson.getJSONObject(i);
            habitaciones.add(JsonUtil.jsonToHabitacion(habJson));
        }

        return habitaciones;
    }

    /**
     * Lista habitaciones de un hotel específico
     */
    public List<HabitacionDTO> listarHabitacionesPorHotel(String codigoHotel) throws IOException {
        List<HabitacionDTO> todas = listarHabitaciones();
        List<HabitacionDTO> filtradas = new ArrayList<>();

        for (HabitacionDTO hab : todas) {
            if (codigoHotel.equals(hab.getCodigoHotel())) {
                filtradas.add(hab);
            }
        }

        return filtradas;
    }

    /**
     * Guarda una nueva habitación en el servidor
     */
    public String guardarHabitacion(String estilo, double precio, String codigoHotel) throws IOException {
        // Crear JSON para enviar
        JSONObject habitacionJson = new JSONObject();
        habitacionJson.put("estilo", estilo);
        habitacionJson.put("precio", precio);
        habitacionJson.put("codigoHotel", codigoHotel);

        String respuestaJson = cliente.enviarOperacion("GUARDAR_HABITACION", habitacionJson.toString());
        JSONObject respuesta = new JSONObject(respuestaJson);

        if (!"OK".equals(respuesta.getString("estado"))) {
            if (respuesta.getString("mensaje").contains("duplicada")) {
                throw new IllegalArgumentException("Habitación duplicada");
            }
            throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
        }

        return respuesta.getString("codigo");
    }

    /**
     * Modifica una habitación existente en el servidor
     */
    public boolean modificarHabitacion(HabitacionDTO habitacion) throws IOException {
        String habitacionJson = JsonUtil.habitacionToJson(habitacion).toString();

        String respuestaJson = cliente.enviarOperacion("MODIFICAR_HABITACION", habitacionJson);
        JSONObject respuesta = new JSONObject(respuestaJson);

        if (!"OK".equals(respuesta.getString("estado"))) {
            if (respuesta.getString("mensaje").contains("no encontrada")) {
                return false;
            }
            throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
        }

        return true;
    }

    /**
     * Elimina una habitación del servidor
     */
    public boolean eliminarHabitacion(String codigo) throws IOException {
        String respuestaJson = cliente.enviarOperacion("ELIMINAR_HABITACION", codigo);
        JSONObject respuesta = new JSONObject(respuestaJson);

        if (!"OK".equals(respuesta.getString("estado"))) {
            if (respuesta.getString("mensaje").contains("no encontrada")) {
                return false;
            }
            throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
        }

        return true;
    }
}