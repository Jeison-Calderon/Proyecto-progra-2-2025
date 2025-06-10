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

public class JsonUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static JSONObject hotelToJson(HotelDTO hotel) {
        JSONObject json = new JSONObject();
        json.put("codigo", hotel.getCodigo());
        json.put("nombre", hotel.getNombre());
        json.put("ubicacion", hotel.getUbicacion());
        return json;
    }

    public static HotelDTO jsonToHotel(JSONObject json) {
        String codigo = json.optString("codigo", "");
        String nombre = json.optString("nombre", "");
        String ubicacion = json.optString("ubicacion", "");
        return new HotelDTO(codigo, nombre, ubicacion);
    }

    public static JSONArray hotelesToJson(List<HotelDTO> hoteles) {
        JSONArray array = new JSONArray();
        for (HotelDTO hotel : hoteles) {
            array.put(hotelToJson(hotel));
        }
        return array;
    }

    public static List<HotelDTO> jsonToHoteles(JSONArray array) {
        List<HotelDTO> hoteles = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            hoteles.add(jsonToHotel(array.getJSONObject(i)));
        }
        return hoteles;
    }

    public static JSONObject habitacionToJson(HabitacionDTO habitacion) {
        JSONObject json = new JSONObject();
        json.put("codigo", habitacion.getCodigo());
        json.put("estilo", habitacion.getEstilo());
        json.put("precio", habitacion.getPrecio());
        json.put("codigoHotel", habitacion.getCodigoHotel());
        json.put("numero", habitacion.getNumero() != null ? habitacion.getNumero() : "");
        json.put("estado", habitacion.getEstado() != null ? habitacion.getEstado() : "disponible");
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

    public static HabitacionDTO jsonToHabitacion(JSONObject json) {
        String codigo = json.optString("codigo", "");
        String estilo = json.optString("estilo", "");
        double precio = json.optDouble("precio", 0.0);
        String codigoHotel = json.optString("codigoHotel", "");
        String numero = json.optString("numero", "");
        String estado = json.optString("estado", "disponible");
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
        return new HabitacionDTO(codigo, estilo, precio, codigoHotel, numero, estado, imagenes);
    }

    public static JSONObject habitacionToJsonCompleto(HabitacionDTO habitacion) {
        JSONObject json = habitacionToJson(habitacion);
        json.put("disponible", habitacion.estaDisponible());
        json.put("ocupada", habitacion.estaOcupada());
        json.put("enMantenimiento", habitacion.estaEnMantenimiento());
        json.put("tieneImagenes", habitacion.tieneImagenes());
        json.put("primeraImagen", habitacion.getPrimeraImagen());
        json.put("cantidadImagenes", habitacion.getImagenes().size());
        return json;
    }

    public static JSONArray habitacionesToJson(List<HabitacionDTO> habitaciones) {
        JSONArray array = new JSONArray();
        for (HabitacionDTO habitacion : habitaciones) {
            array.put(habitacionToJson(habitacion));
        }
        return array;
    }

    public static JSONArray habitacionesToJsonCompleto(List<HabitacionDTO> habitaciones) {
        JSONArray array = new JSONArray();
        for (HabitacionDTO habitacion : habitaciones) {
            array.put(habitacionToJsonCompleto(habitacion));
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

    public static JSONArray habitacionesDisponibleesToJson(List<HabitacionDTO> habitaciones) {
        JSONArray array = new JSONArray();
        for (HabitacionDTO habitacion : habitaciones) {
            if (habitacion.estaDisponible()) {
                array.put(habitacionToJsonCompleto(habitacion));
            }
        }
        return array;
    }

    public static JSONArray habitacionesPorEstadoToJson(List<HabitacionDTO> habitaciones, String estado) {
        JSONArray array = new JSONArray();
        for (HabitacionDTO habitacion : habitaciones) {
            if (estado.equalsIgnoreCase(habitacion.getEstado())) {
                array.put(habitacionToJsonCompleto(habitacion));
            }
        }
        return array;
    }

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

    public static boolean validarJsonHabitacion(JSONObject json) {
        return json.has("codigo") &&
                json.has("estilo") &&
                json.has("precio") &&
                json.has("codigoHotel");
    }

    public static String habitacionToString(HabitacionDTO habitacion) {
        if (habitacion == null) return "null";
        return String.format("Habitación{código='%s', número='%s', estilo='%s', precio=%.2f, " +
                        "estado='%s', hotel='%s', imágenes=%d}",
                habitacion.getCodigo(), habitacion.getNumero(), habitacion.getEstilo(),
                habitacion.getPrecio(), habitacion.getEstado(), habitacion.getCodigoHotel(),
                habitacion.getImagenes().size());
    }

    // ✅ ACTUALIZADO: Método reservaToJson con campo recepcionista
    public static JSONObject reservaToJson(ReservaDTO reserva) {
        JSONObject json = new JSONObject();
        json.put("codigo", reserva.getCodigo() != null ? reserva.getCodigo() : "");
        json.put("codigoHabitacion", reserva.getCodigoHabitacion() != null ? reserva.getCodigoHabitacion() : "");
        json.put("codigoHotel", reserva.getCodigoHotel() != null ? reserva.getCodigoHotel() : "");
        json.put("estado", reserva.getEstado() != null ? reserva.getEstado() : ReservaDTO.ESTADO_ACTIVA);
        json.put("clienteNombre", reserva.getClienteNombre() != null ? reserva.getClienteNombre() : "");
        json.put("recepcionista", reserva.getRecepcionista() != null ? reserva.getRecepcionista() : ""); // ✅ NUEVO
        json.put("precioTotal", reserva.getPrecioTotal());
        json.put("fechaDesde", reserva.getFechaDesdeString());
        json.put("fechaHasta", reserva.getFechaHastaString());
        json.put("fechaCreacion", reserva.getFechaCreacionString());
        json.put("duracionDias", reserva.getDuracionDias());
        json.put("activa", reserva.estaActiva());
        json.put("cancelada", reserva.estaCancelada());
        json.put("finalizada", reserva.estaFinalizada());
        json.put("valida", reserva.esValida());

        return json;
    }

    // ✅ ACTUALIZADO: Método jsonToReserva con campo recepcionista
    public static ReservaDTO jsonToReserva(JSONObject json) {
        String codigo = json.optString("codigo", "");
        String codigoHabitacion = json.optString("codigoHabitacion", "");
        String codigoHotel = json.optString("codigoHotel", "");
        String estado = json.optString("estado", ReservaDTO.ESTADO_ACTIVA);
        String clienteNombre = json.optString("clienteNombre", "");
        String recepcionista = json.optString("recepcionista", "Sistema"); // ✅ NUEVO: Con valor por defecto
        double precioTotal = json.optDouble("precioTotal", 0.0);

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

        // ✅ ACTUALIZADO: Crear ReservaDTO con el nuevo constructor que incluye recepcionista
        ReservaDTO reserva = new ReservaDTO();
        reserva.setCodigo(codigo);
        reserva.setCodigoHabitacion(codigoHabitacion);
        reserva.setCodigoHotel(codigoHotel);
        reserva.setFechaDesde(fechaDesde);
        reserva.setFechaHasta(fechaHasta);
        reserva.setEstado(estado);
        reserva.setClienteNombre(clienteNombre);
        reserva.setRecepcionista(recepcionista); // ✅ NUEVO
        reserva.setPrecioTotal(precioTotal);

        if (fechaCreacion != null) {
            reserva.setFechaCreacion(fechaCreacion);
        }

        return reserva;
    }

    public static JSONArray reservasToJson(List<ReservaDTO> reservas) {
        JSONArray array = new JSONArray();
        for (ReservaDTO reserva : reservas) {
            array.put(reservaToJson(reserva));
        }
        return array;
    }

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

    public static JSONArray reservasActivasToJson(List<ReservaDTO> reservas) {
        JSONArray array = new JSONArray();
        for (ReservaDTO reserva : reservas) {
            if (reserva.estaActiva()) {
                array.put(reservaToJson(reserva));
            }
        }
        return array;
    }

    public static JSONArray reservasPorEstadoToJson(List<ReservaDTO> reservas, String estado) {
        JSONArray array = new JSONArray();
        for (ReservaDTO reserva : reservas) {
            if (estado.equalsIgnoreCase(reserva.getEstado())) {
                array.put(reservaToJson(reserva));
            }
        }
        return array;
    }

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

        respuesta.put("habitacionesDisponibles", habitacionesDisponibleesToJson(habitaciones));
        respuesta.put("totalDisponibles", (int) habitaciones.stream()
                .filter(HabitacionDTO::estaDisponible).count());
        respuesta.put("totalHabitaciones", habitaciones.size());
        respuesta.put("reservasEnPeriodo", reservasActivasToJson(reservas));
        respuesta.put("totalReservas", reservas.size());

        return respuesta;
    }

    public static boolean validarJsonReserva(JSONObject json) {
        return json.has("codigo") &&
                json.has("codigoHabitacion") &&
                json.has("fechaDesde") &&
                json.has("fechaHasta") &&
                !json.optString("codigo", "").trim().isEmpty() &&
                !json.optString("codigoHabitacion", "").trim().isEmpty();
    }

    public static String fechaToString(LocalDate fecha) {
        return fecha != null ? fecha.format(DATE_FORMATTER) : "";
    }

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

    // ✅ ACTUALIZADO: Método reservaToString con campo recepcionista
    public static String reservaToString(ReservaDTO reserva) {
        if (reserva == null) return "null";

        return String.format("Reserva{código='%s', habitación='%s', cliente='%s', recepcionista='%s', " +
                        "desde='%s', hasta='%s', estado='%s', precio=%.2f, duración=%d días}",
                reserva.getCodigo(), reserva.getCodigoHabitacion(),
                reserva.getClienteNombre(), reserva.getRecepcionista(), // ✅ NUEVO
                reserva.getFechaDesdeString(), reserva.getFechaHastaString(), reserva.getEstado(),
                reserva.getPrecioTotal(), reserva.getDuracionDias());
    }

    public static JSONObject crearRespuestaError(String mensaje) {
        JSONObject respuesta = new JSONObject();
        respuesta.put("estado", "ERROR");
        respuesta.put("mensaje", mensaje);
        return respuesta;
    }

    public static JSONObject crearRespuestaExito(String mensaje) {
        JSONObject respuesta = new JSONObject();
        respuesta.put("estado", "OK");
        respuesta.put("mensaje", mensaje);
        return respuesta;
    }
}