package aplicacion.util;

import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilitaria para conversiones entre DTOs y JSON
 */
public class JsonUtil {

    // Convertir HotelDTO a JSONObject
    public static JSONObject hotelToJson(HotelDTO hotel) {
        JSONObject json = new JSONObject();
        json.put("codigo", hotel.getCodigo());
        json.put("nombre", hotel.getNombre());
        json.put("ubicacion", hotel.getUbicacion());
        return json;
    }

    // Convertir JSONObject a HotelDTO
    public static HotelDTO jsonToHotel(JSONObject json) {
        String codigo = json.optString("codigo", "");
        String nombre = json.optString("nombre", "");
        String ubicacion = json.optString("ubicacion", "");
        return new HotelDTO(codigo, nombre, ubicacion);
    }

    // Convertir lista de HotelDTO a JSONArray
    public static JSONArray hotelesToJson(List<HotelDTO> hoteles) {
        JSONArray array = new JSONArray();
        for (HotelDTO hotel : hoteles) {
            array.put(hotelToJson(hotel));
        }
        return array;
    }

    // Convertir JSONArray a lista de HotelDTO
    public static List<HotelDTO> jsonToHoteles(JSONArray array) {
        List<HotelDTO> hoteles = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            hoteles.add(jsonToHotel(array.getJSONObject(i)));
        }
        return hoteles;
    }

    // Métodos similares para HabitacionDTO
    public static JSONObject habitacionToJson(HabitacionDTO habitacion) {
        JSONObject json = new JSONObject();
        json.put("codigo", habitacion.getCodigo());
        json.put("estilo", habitacion.getEstilo());
        json.put("precio", habitacion.getPrecio());
        return json;
    }

    public static HabitacionDTO jsonToHabitacion(JSONObject json) {
        String codigo = json.optString("codigo", "");
        String estilo = json.optString("estilo", "");
        double precio = json.optDouble("precio", 0.0);
        return new HabitacionDTO(codigo, estilo, precio);
    }

    public static JSONArray habitacionesToJson(List<HabitacionDTO> habitaciones) {
        JSONArray array = new JSONArray();
        for (HabitacionDTO habitacion : habitaciones) {
            array.put(habitacionToJson(habitacion));
        }
        return array;
    }

    public static List<HabitacionDTO> jsonToHabitaciones(JSONArray array) {
        List<HabitacionDTO> habitaciones = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            habitaciones.add(jsonToHabitacion(array.getJSONObject(i)));
        }
        return habitaciones;
    }
}