package aplicacion.util;

import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import aplicacion.dto.ReservaDTO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilitaria para conversiones entre DTOs y JSON
 */
public class JsonUtil {

    // ✅ FORMATTER PARA FECHAS
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ✅ MÉTODOS DE HOTEL (sin cambios)

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

    // ✅ MÉTODOS DE HABITACIÓN ACTUALIZADOS CON NUEVOS CAMPOS

    // Convertir HabitacionDTO a JSONObject
    public static JSONObject habitacionToJson(HabitacionDTO habitacion) {
        JSONObject json = new JSONObject();

        // ✅ Campos existentes
        json.put("codigo", habitacion.getCodigo());
        json.put("estilo", habitacion.getEstilo());
        json.put("precio", habitacion.getPrecio());
        json.put("codigoHotel", habitacion.getCodigoHotel());

        // ✅ NUEVOS campos agregados
        json.put("numero", habitacion.getNumero() != null ? habitacion.getNumero() : "");
        json.put("estado", habitacion.getEstado() != null ? habitacion.getEstado() : "disponible");

        // ✅ Manejar lista de imágenes
        JSONArray imagenesArray = new JSONArray();
        if (habitacion.getImagenes() != null) {
            for (String imagen : habitacion.getImagenes()) {
                if (imagen != null && !imagen.trim().isEmpty()) {
                    imagenesArray.put(imagen);
                }
            }
        }
        json.put("imagenes", imagenesArray);

        return json;
    }

    // Convertir JSONObject a HabitacionDTO
    public static HabitacionDTO jsonToHabitacion(JSONObject json) {
        // ✅ Campos existentes
        String codigo = json.optString("codigo", "");
        String estilo = json.optString("estilo", "");
        double precio = json.optDouble("precio", 0.0);
        String codigoHotel = json.optString("codigoHotel", "");

        // ✅ NUEVOS campos con valores por defecto
        String numero = json.optString("numero", "");
        String estado = json.optString("estado", "disponible");

        // ✅ Manejar lista de imágenes
        List<String> imagenes = new ArrayList<>();
        JSONArray imagenesArray = json.optJSONArray("imagenes");
        if (imagenesArray != null) {
            for (int i = 0; i < imagenesArray.length(); i++) {
                String imagen = imagenesArray.optString(i);
                if (imagen != null && !imagen.trim().isEmpty()) {
                    imagenes.add(imagen);
                }
            }
        }

        // ✅ Crear HabitacionDTO con constructor completo
        return new HabitacionDTO(codigo, estilo, precio, codigoHotel, numero, estado, imagenes);
    }

    // ✅ MÉTODO EXTENDIDO: Habitación con información completa
    public static JSONObject habitacionToJsonCompleto(HabitacionDTO habitacion) {
        JSONObject json = habitacionToJson(habitacion);

        // ✅ Agregar información adicional útil para la UI
        json.put("disponible", habitacion.estaDisponible());
        json.put("ocupada", habitacion.estaOcupada());
        json.put("enMantenimiento", habitacion.estaEnMantenimiento());
        json.put("tieneImagenes", habitacion.tieneImagenes());
        json.put("primeraImagen", habitacion.getPrimeraImagen());
        json.put("cantidadImagenes", habitacion.getImagenes().size());

        return json;
    }

    // Convertir lista de HabitacionDTO a JSONArray
    public static JSONArray habitacionesToJson(List<HabitacionDTO> habitaciones) {
        JSONArray array = new JSONArray();
        for (HabitacionDTO habitacion : habitaciones) {
            array.put(habitacionToJson(habitacion));
        }
        return array;
    }

    // ✅ NUEVO: Convertir lista con información completa
    public static JSONArray habitacionesToJsonCompleto(List<HabitacionDTO> habitaciones) {
        JSONArray array = new JSONArray();
        for (HabitacionDTO habitacion : habitaciones) {
            array.put(habitacionToJsonCompleto(habitacion));
        }
        return array;
    }

    // Convertir JSONArray a lista de HabitacionDTO
    public static List<HabitacionDTO> jsonToHabitaciones(JSONArray array) {
        List<HabitacionDTO> habitaciones = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            habitaciones.add(jsonToHabitacion(array.getJSONObject(i)));
        }
        return habitaciones;
    }

    // ✅ NUEVO: Filtrar solo habitaciones disponibles
    public static JSONArray habitacionesDisponibleesToJson(List<HabitacionDTO> habitaciones) {
        JSONArray array = new JSONArray();
        for (HabitacionDTO habitacion : habitaciones) {
            if (habitacion.estaDisponible()) {
                array.put(habitacionToJsonCompleto(habitacion));
            }
        }
        return array;
    }

    // ✅ NUEVO: Filtrar habitaciones por estado
    public static JSONArray habitacionesPorEstadoToJson(List<HabitacionDTO> habitaciones, String estado) {
        JSONArray array = new JSONArray();
        for (HabitacionDTO habitacion : habitaciones) {
            if (estado.equalsIgnoreCase(habitacion.getEstado())) {
                array.put(habitacionToJsonCompleto(habitacion));
            }
        }
        return array;
    }

    // ✅ NUEVO: Método utilitario para crear respuesta de disponibilidad
    public static JSONObject crearRespuestaDisponibilidad(List<HabitacionDTO> habitaciones,
                                                          String fechaDesde, String fechaHasta) {
        JSONObject respuesta = new JSONObject();
        respuesta.put("estado", "OK");
        respuesta.put("fechaDesde", fechaDesde);
        respuesta.put("fechaHasta", fechaHasta);
        respuesta.put("habitacionesDisponibles", habitacionesDisponibleesToJson(habitaciones));
        respuesta.put("totalDisponibles", (int) habitaciones.stream()
                .filter(HabitacionDTO::estaDisponible).count());
        respuesta.put("totalHabitaciones", habitaciones.size());
        return respuesta;
    }

    // ✅ NUEVO: Método para validar JSON de habitación
    public static boolean validarJsonHabitacion(JSONObject json) {
        return json.has("codigo") &&
                json.has("estilo") &&
                json.has("precio") &&
                json.has("codigoHotel");
    }

    // ✅ NUEVO: Método para debugging - convertir DTO a string legible
    public static String habitacionToString(HabitacionDTO habitacion) {
        if (habitacion == null) return "null";

        return String.format("Habitación{código='%s', número='%s', estilo='%s', precio=%.2f, " +
                        "estado='%s', hotel='%s', imágenes=%d}",
                habitacion.getCodigo(), habitacion.getNumero(), habitacion.getEstilo(),
                habitacion.getPrecio(), habitacion.getEstado(), habitacion.getCodigoHotel(),
                habitacion.getImagenes().size());
    }

    // ✅ ===================== MÉTODOS DE RESERVA (NUEVOS) =====================

    /**
     * Convertir ReservaDTO a JSONObject
     */
    public static JSONObject reservaToJson(ReservaDTO reserva) {
        JSONObject json = new JSONObject();

        // ✅ Campos principales
        json.put("codigo", reserva.getCodigo() != null ? reserva.getCodigo() : "");
        json.put("codigoHabitacion", reserva.getCodigoHabitacion() != null ? reserva.getCodigoHabitacion() : "");
        json.put("codigoHotel", reserva.getCodigoHotel() != null ? reserva.getCodigoHotel() : "");
        json.put("estado", reserva.getEstado() != null ? reserva.getEstado() : ReservaDTO.ESTADO_ACTIVA);

        // ✅ Campos adicionales
        json.put("clienteNombre", reserva.getClienteNombre() != null ? reserva.getClienteNombre() : "");
        json.put("precioTotal", reserva.getPrecioTotal());

        // ✅ Fechas como strings para JSON
        json.put("fechaDesde", reserva.getFechaDesdeString());
        json.put("fechaHasta", reserva.getFechaHastaString());
        json.put("fechaCreacion", reserva.getFechaCreacionString());

        // ✅ Información calculada
        json.put("duracionDias", reserva.getDuracionDias());
        json.put("activa", reserva.estaActiva());
        json.put("cancelada", reserva.estaCancelada());
        json.put("finalizada", reserva.estaFinalizada());
        json.put("valida", reserva.esValida());

        return json;
    }

    /**
     * Convertir JSONObject a ReservaDTO
     */
    public static ReservaDTO jsonToReserva(JSONObject json) {
        // ✅ Campos principales
        String codigo = json.optString("codigo", "");
        String codigoHabitacion = json.optString("codigoHabitacion", "");
        String codigoHotel = json.optString("codigoHotel", "");
        String estado = json.optString("estado", ReservaDTO.ESTADO_ACTIVA);
        String clienteNombre = json.optString("clienteNombre", "");
        double precioTotal = json.optDouble("precioTotal", 0.0);

        // ✅ Parsear fechas desde strings
        LocalDate fechaDesde = null;
        LocalDate fechaHasta = null;
        LocalDate fechaCreacion = null;

        try {
            String fechaDesdeStr = json.optString("fechaDesde", "");
            if (!fechaDesdeStr.isEmpty()) {
                fechaDesde = LocalDate.parse(fechaDesdeStr, DATE_FORMATTER);
            }

            String fechaHastaStr = json.optString("fechaHasta", "");
            if (!fechaHastaStr.isEmpty()) {
                fechaHasta = LocalDate.parse(fechaHastaStr, DATE_FORMATTER);
            }

            String fechaCreacionStr = json.optString("fechaCreacion", "");
            if (!fechaCreacionStr.isEmpty()) {
                fechaCreacion = LocalDate.parse(fechaCreacionStr, DATE_FORMATTER);
            } else {
                fechaCreacion = LocalDate.now();
            }
        } catch (Exception e) {
            System.err.println("❌ Error parseando fechas en JSON: " + e.getMessage());
        }

        // ✅ Crear ReservaDTO con constructor completo
        ReservaDTO reserva = new ReservaDTO(codigo, codigoHabitacion, codigoHotel,
                fechaDesde, fechaHasta, estado,
                clienteNombre, precioTotal);

        if (fechaCreacion != null) {
            reserva.setFechaCreacion(fechaCreacion);
        }

        return reserva;
    }

    /**
     * Convertir lista de ReservaDTO a JSONArray
     */
    public static JSONArray reservasToJson(List<ReservaDTO> reservas) {
        JSONArray array = new JSONArray();
        for (ReservaDTO reserva : reservas) {
            array.put(reservaToJson(reserva));
        }
        return array;
    }

    /**
     * Convertir JSONArray a lista de ReservaDTO
     */
    public static List<ReservaDTO> jsonToReservas(JSONArray array) {
        List<ReservaDTO> reservas = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            ReservaDTO reserva = jsonToReserva(array.getJSONObject(i));
            if (reserva != null) {
                reservas.add(reserva);
            }
        }
        return reservas;
    }

    /**
     * ✅ NUEVO: Filtrar solo reservas activas
     */
    public static JSONArray reservasActivasToJson(List<ReservaDTO> reservas) {
        JSONArray array = new JSONArray();
        for (ReservaDTO reserva : reservas) {
            if (reserva.estaActiva()) {
                array.put(reservaToJson(reserva));
            }
        }
        return array;
    }

    /**
     * ✅ NUEVO: Filtrar reservas por estado
     */
    public static JSONArray reservasPorEstadoToJson(List<ReservaDTO> reservas, String estado) {
        JSONArray array = new JSONArray();
        for (ReservaDTO reserva : reservas) {
            if (estado.equalsIgnoreCase(reserva.getEstado())) {
                array.put(reservaToJson(reserva));
            }
        }
        return array;
    }

    /**
     * ✅ NUEVO: Crear respuesta de consulta de disponibilidad completa
     */
    public static JSONObject crearRespuestaConsultaDisponibilidad(
            List<HabitacionDTO> habitaciones,
            List<ReservaDTO> reservas,
            String fechaDesde,
            String fechaHasta,
            String codigoHotel) {

        JSONObject respuesta = new JSONObject();
        respuesta.put("estado", "OK");
        respuesta.put("fechaDesde", fechaDesde);
        respuesta.put("fechaHasta", fechaHasta);
        respuesta.put("codigoHotel", codigoHotel != null ? codigoHotel : "");

        // ✅ Habitaciones disponibles
        respuesta.put("habitacionesDisponibles", habitacionesDisponibleesToJson(habitaciones));
        respuesta.put("totalDisponibles", (int) habitaciones.stream()
                .filter(HabitacionDTO::estaDisponible).count());
        respuesta.put("totalHabitaciones", habitaciones.size());

        // ✅ Reservas en el período
        respuesta.put("reservasEnPeriodo", reservasActivasToJson(reservas));
        respuesta.put("totalReservas", reservas.size());

        return respuesta;
    }

    /**
     * ✅ NUEVO: Validar JSON de reserva
     */
    public static boolean validarJsonReserva(JSONObject json) {
        return json.has("codigo") &&
                json.has("codigoHabitacion") &&
                json.has("fechaDesde") &&
                json.has("fechaHasta") &&
                !json.optString("codigo", "").trim().isEmpty() &&
                !json.optString("codigoHabitacion", "").trim().isEmpty();
    }

    /**
     * ✅ NUEVO: Convertir fecha LocalDate a String
     */
    public static String fechaToString(LocalDate fecha) {
        return fecha != null ? fecha.format(DATE_FORMATTER) : "";
    }

    /**
     * ✅ NUEVO: Convertir String a LocalDate
     */
    public static LocalDate stringToFecha(String fechaStr) {
        try {
            return fechaStr != null && !fechaStr.trim().isEmpty()
                    ? LocalDate.parse(fechaStr, DATE_FORMATTER)
                    : null;
        } catch (Exception e) {
            System.err.println("❌ Error parseando fecha: " + fechaStr + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * ✅ NUEVO: Método para debugging - convertir ReservaDTO a string legible
     */
    public static String reservaToString(ReservaDTO reserva) {
        if (reserva == null) return "null";

        return String.format("Reserva{código='%s', habitación='%s', cliente='%s', " +
                        "desde='%s', hasta='%s', estado='%s', precio=%.2f, duración=%d días}",
                reserva.getCodigo(), reserva.getCodigoHabitacion(),
                reserva.getClienteNombre(), reserva.getFechaDesdeString(),
                reserva.getFechaHastaString(), reserva.getEstado(),
                reserva.getPrecioTotal(), reserva.getDuracionDias());
    }

    /**
     * ✅ NUEVO: Crear respuesta de error estándar
     */
    public static JSONObject crearRespuestaError(String mensaje) {
        JSONObject respuesta = new JSONObject();
        respuesta.put("estado", "ERROR");
        respuesta.put("mensaje", mensaje);
        return respuesta;
    }

    /**
     * ✅ NUEVO: Crear respuesta exitosa estándar
     */
    public static JSONObject crearRespuestaExito(String mensaje) {
        JSONObject respuesta = new JSONObject();
        respuesta.put("estado", "OK");
        respuesta.put("mensaje", mensaje);
        return respuesta;
    }
}