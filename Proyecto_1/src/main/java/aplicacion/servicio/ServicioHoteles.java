package aplicacion.servicio;

import aplicacion.cliente.ClienteSocket;
import aplicacion.dto.HotelDTO;
import aplicacion.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServicioHoteles {
    private ClienteSocket cliente;

    public ServicioHoteles() {
        this.cliente = new ClienteSocket();
    }

    /**
     * Lista todos los hoteles desde el servidor
     */
    public List<HotelDTO> listarHoteles() throws IOException {
        String respuestaJson = cliente.enviarOperacion("LISTAR_HOTELES");
        JSONObject respuesta = new JSONObject(respuestaJson);

        if (!"OK".equals(respuesta.getString("estado"))) {
            throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
        }

        List<HotelDTO> hoteles = new ArrayList<>();
        JSONArray hotelesJson = respuesta.getJSONArray("hoteles");

        for (int i = 0; i < hotelesJson.length(); i++) {
            JSONObject hotelJson = hotelesJson.getJSONObject(i);
            hoteles.add(JsonUtil.jsonToHotel(hotelJson));
        }

        return hoteles;
    }

    /**
     * Guarda un nuevo hotel en el servidor
     */
    public HotelDTO guardarHotel(String nombre, String ubicacion) throws IOException {
        // Crear DTO temporal para enviar
        HotelDTO hotelTemp = new HotelDTO("", nombre, ubicacion);
        String hotelJson = JsonUtil.hotelToJson(hotelTemp).toString();

        String respuestaJson = cliente.enviarOperacion("GUARDAR_HOTEL", hotelJson);
        JSONObject respuesta = new JSONObject(respuestaJson);

        if (!"OK".equals(respuesta.getString("estado"))) {
            if (respuesta.getString("mensaje").contains("duplicado")) {
                throw new IllegalArgumentException("Hotel duplicado: ya existe un hotel con ese nombre y ubicación");
            }
            throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
        }

        // Retornar el hotel guardado con su código generado
        JSONObject hotelGuardado = respuesta.getJSONObject("hotel");
        return JsonUtil.jsonToHotel(hotelGuardado);
    }

    /**
     * Modifica un hotel existente en el servidor
     */
    public boolean modificarHotel(HotelDTO hotel) throws IOException {
        String hotelJson = JsonUtil.hotelToJson(hotel).toString();

        String respuestaJson = cliente.enviarOperacion("MODIFICAR_HOTEL", hotelJson);
        JSONObject respuesta = new JSONObject(respuestaJson);

        if (!"OK".equals(respuesta.getString("estado"))) {
            if (respuesta.getString("mensaje").contains("no encontrado")) {
                return false;
            }
            throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
        }

        return true;
    }

    /**
     * Elimina un hotel del servidor
     */
    public boolean eliminarHotel(String codigo) throws IOException {
        String respuestaJson = cliente.enviarOperacion("ELIMINAR_HOTEL", codigo);
        JSONObject respuesta = new JSONObject(respuestaJson);

        if (!"OK".equals(respuesta.getString("estado"))) {
            if (respuesta.getString("mensaje").contains("no encontrado")) {
                return false;
            }
            throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
        }

        return true;
    }

    /**
     * Obtiene el nombre de un hotel por su código
     */
    public String obtenerNombreHotel(String codigoHotel) throws IOException {
        try {
            List<HotelDTO> hoteles = listarHoteles();

            for (HotelDTO hotel : hoteles) {
                if (hotel.getCodigo().equals(codigoHotel)) {
                    return hotel.getNombre();
                }
            }

            return null;

        } catch (Exception e) {
            throw new IOException("Error obteniendo nombre del hotel: " + e.getMessage(), e);
        }
    }
}