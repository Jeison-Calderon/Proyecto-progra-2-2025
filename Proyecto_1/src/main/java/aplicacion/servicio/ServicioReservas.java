package aplicacion.servicio;

import aplicacion.cliente.ClienteSocket;
import aplicacion.dto.DisponibilidadDTO;
import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.ReservaDTO;
import aplicacion.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestión de reservas y consultas de disponibilidad
 * Maneja toda la comunicación con el servidor para operaciones de reservas
 */
public class ServicioReservas {
    private ClienteSocket cliente;

    public ServicioReservas() {
        this.cliente = new ClienteSocket();
    }

    /**
     * ✅ CONSULTA COMPLETA DE DISPONIBILIDAD
     * Obtiene habitaciones disponibles en un período de fechas con información detallada
     */
    public ResultadoConsultaDisponibilidad consultarDisponibilidad(LocalDate fechaDesde, LocalDate fechaHasta, String codigoHotel) throws IOException {
        try {
            // ✅ Preparar datos de consulta
            JSONObject consulta = new JSONObject();
            consulta.put("fechaDesde", JsonUtil.fechaToString(fechaDesde));
            consulta.put("fechaHasta", JsonUtil.fechaToString(fechaHasta));
            if (codigoHotel != null && !codigoHotel.trim().isEmpty()) {
                consulta.put("codigoHotel", codigoHotel);
            }

            // ✅ Enviar al servidor
            String respuestaJson = cliente.enviarOperacion("CONSULTAR_DISPONIBILIDAD", consulta.toString());
            JSONObject respuesta = new JSONObject(respuestaJson);

            if ("OK".equals(respuesta.getString("estado"))) {
                // ✅ Procesar habitaciones disponibles
                JSONArray habitacionesJson = respuesta.getJSONArray("habitacionesDisponibles");
                List<HabitacionDTO> habitaciones = JsonUtil.jsonToHabitaciones(habitacionesJson);

                // ✅ Procesar reservas en período
                JSONArray reservasJson = respuesta.optJSONArray("reservasEnPeriodo");
                List<ReservaDTO> reservas = reservasJson != null ? JsonUtil.jsonToReservas(reservasJson) : new ArrayList<>();

                // ✅ Crear resultado
                return new ResultadoConsultaDisponibilidad(
                        true,
                        "Consulta exitosa",
                        habitaciones,
                        reservas,
                        respuesta.optInt("totalDisponibles", 0),
                        respuesta.optInt("totalHabitaciones", 0),
                        fechaDesde,
                        fechaHasta,
                        codigoHotel
                );
            } else {
                return new ResultadoConsultaDisponibilidad(false, respuesta.getString("mensaje"));
            }

        } catch (Exception e) {
            throw new IOException("Error en consulta de disponibilidad: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ MÉTODO SOBRECARGADO - Orden de parámetros para JavaFX
     * Compatible con ConsultaDisponibilidad
     */
    public List<DisponibilidadDTO> consultarDisponibilidad(String codigoHotel, LocalDate fechaDesde, LocalDate fechaHasta) throws IOException {
        try {
            // Usar el método principal
            ResultadoConsultaDisponibilidad resultado = consultarDisponibilidad(fechaDesde, fechaHasta, codigoHotel);

            if (resultado.isExito()) {
                // Convertir HabitacionDTO a DisponibilidadDTO
                List<DisponibilidadDTO> disponibilidad = new ArrayList<>();

                for (HabitacionDTO habitacion : resultado.getHabitacionesDisponibles()) {
                    DisponibilidadDTO item = new DisponibilidadDTO();
                    item.setCodigoHabitacion(habitacion.getCodigo());
                    item.setNumeroHabitacion(habitacion.getNumero());
                    item.setEstilo(habitacion.getEstilo());
                    item.setPrecio(habitacion.getPrecio());
                    item.setCantidadImagenes(habitacion.getImagenes().size());
                    item.setEstado(habitacion.getEstado());

                    // Obtener nombre del hotel
                    try {
                        ServicioHoteles servicioHoteles = new ServicioHoteles();
                        String nombreHotel = servicioHoteles.obtenerNombreHotel(habitacion.getCodigoHotel());
                        item.setNombreHotel(nombreHotel != null ? nombreHotel : "Hotel desconocido");
                        item.setCodigoHotel(habitacion.getCodigoHotel());
                    } catch (Exception e) {
                        item.setNombreHotel("Hotel desconocido");
                        item.setCodigoHotel(habitacion.getCodigoHotel());
                    }

                    disponibilidad.add(item);
                }

                return disponibilidad;
            } else {
                throw new IOException(resultado.getMensaje());
            }

        } catch (Exception e) {
            throw new IOException("Error en consulta de disponibilidad: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ LISTA SIMPLE DE HABITACIONES DISPONIBLES
     * Más rápido que la consulta completa, solo retorna habitaciones
     */
    public List<HabitacionDTO> obtenerHabitacionesDisponibles(LocalDate fechaDesde, LocalDate fechaHasta, String codigoHotel) throws IOException {
        try {
            JSONObject consulta = new JSONObject();
            consulta.put("fechaDesde", JsonUtil.fechaToString(fechaDesde));
            consulta.put("fechaHasta", JsonUtil.fechaToString(fechaHasta));
            if (codigoHotel != null && !codigoHotel.trim().isEmpty()) {
                consulta.put("codigoHotel", codigoHotel);
            }

            String respuestaJson = cliente.enviarOperacion("HABITACIONES_DISPONIBLES", consulta.toString());
            JSONObject respuesta = new JSONObject(respuestaJson);

            if ("OK".equals(respuesta.getString("estado"))) {
                JSONArray habitacionesJson = respuesta.getJSONArray("habitaciones");
                return JsonUtil.jsonToHabitaciones(habitacionesJson);
            } else {
                throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
            }

        } catch (Exception e) {
            throw new IOException("Error obteniendo habitaciones disponibles: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ CREAR NUEVA RESERVA - MÉTODO PRINCIPAL CON DEBUG
     */
    public ResultadoOperacion crearReserva(ReservaDTO reserva) throws IOException {
        System.out.println("\n=== INICIO DEBUG CREAR RESERVA ===");

        try {
            if (reserva == null) {
                System.out.println("❌ ERROR: reserva es null");
                return new ResultadoOperacion(false, "Reserva es null");
            }

            System.out.println("📋 RESERVA RECIBIDA:");
            System.out.println("   - Código: " + reserva.getCodigo());
            System.out.println("   - Cliente: " + reserva.getClienteNombre());
            System.out.println("   - Habitación: " + reserva.getCodigoHabitacion());
            System.out.println("   - Desde: " + reserva.getFechaDesde());
            System.out.println("   - Hasta: " + reserva.getFechaHasta());
            System.out.println("   - Estado: " + reserva.getEstado());
            System.out.println("   - Precio: " + reserva.getPrecioTotal());

            boolean esValida = reserva.esValida();
            System.out.println("✅ VALIDACIÓN esValida(): " + esValida);

            if (!esValida) {
                System.out.println("❌ ERROR: Datos de reserva inválidos según validación");
                return new ResultadoOperacion(false, "Datos de reserva inválidos - Validación falló");
            }

            System.out.println("🔄 CONVIRTIENDO A JSON...");
            JSONObject reservaJson;
            try {
                reservaJson = JsonUtil.reservaToJson(reserva);
                System.out.println("✅ JSON CREADO EXITOSAMENTE:");
                System.out.println(reservaJson.toString(2)); // Pretty print
            } catch (Exception e) {
                System.out.println("❌ ERROR CREANDO JSON: " + e.getMessage());
                e.printStackTrace();
                return new ResultadoOperacion(false, "Error creando JSON: " + e.getMessage());
            }

            System.out.println("📡 ENVIANDO AL SERVIDOR...");
            System.out.println("   - Operación: CREAR_RESERVA");
            System.out.println("   - Datos: " + reservaJson.toString());

            String respuestaJson;
            try {
                respuestaJson = cliente.enviarOperacion("CREAR_RESERVA", reservaJson.toString());
                System.out.println("📨 RESPUESTA DEL SERVIDOR:");
                System.out.println(respuestaJson);
            } catch (Exception e) {
                System.out.println("❌ ERROR COMUNICÁNDOSE CON SERVIDOR: " + e.getMessage());
                e.printStackTrace();
                return new ResultadoOperacion(false, "Error comunicándose con servidor: " + e.getMessage());
            }

            System.out.println("🔍 PROCESANDO RESPUESTA...");
            JSONObject respuesta = new JSONObject(respuestaJson);

            boolean exito = "OK".equals(respuesta.getString("estado"));
            String mensaje = respuesta.getString("mensaje");

            System.out.println("📊 RESULTADO:");
            System.out.println("   - Éxito: " + exito);
            System.out.println("   - Mensaje: " + mensaje);

            if (exito && respuesta.has("reserva")) {
                System.out.println("RESERVA CREADA EXITOSAMENTE");
                JSONObject reservaGuardada = respuesta.getJSONObject("reserva");
                ReservaDTO reservaActualizada = JsonUtil.jsonToReserva(reservaGuardada);
                return new ResultadoOperacion(true, mensaje, reservaActualizada);
            } else if (!exito) {
                System.out.println("SERVIDOR RECHAZÓ LA RESERVA: " + mensaje);
            }

            return new ResultadoOperacion(exito, mensaje);

        } catch (Exception e) {
            System.out.println("EXCEPCIÓN EN CREAR RESERVA: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error creando reserva: " + e.getMessage(), e);
        } finally {
            System.out.println("=== FIN DEBUG CREAR RESERVA ===\n");
        }
    }

    /**
     * ✅ CREAR RESERVA SIMPLIFICADA - VERSIÓN CORREGIDA CON DEBUG
     */
    public ResultadoOperacion crearReserva(String nombreCliente, String codigoHabitacion, LocalDate fechaDesde, LocalDate fechaHasta) throws IOException {
        System.out.println("\n=== CREAR RESERVA SIMPLIFICADA - DEBUG ===");

        try {
            // ✅ GENERAR CÓDIGO DE RESERVA PRIMERO
            String codigoReserva;
            try {
                codigoReserva = obtenerProximoCodigoReserva();
                System.out.println("✅ Código generado: " + codigoReserva);
            } catch (Exception e) {
                System.out.println("❌ Error generando código, usando fallback");
                codigoReserva = "RES" + System.currentTimeMillis();
            }

            // ✅ Crear ReservaDTO COMPLETA
            ReservaDTO reserva = new ReservaDTO();
            reserva.setCodigo(codigoReserva);                    // ✅ CÓDIGO REQUERIDO
            reserva.setClienteNombre(nombreCliente);
            reserva.setCodigoHabitacion(codigoHabitacion);
            reserva.setFechaDesde(fechaDesde);
            reserva.setFechaHasta(fechaHasta);
            reserva.setEstado(ReservaDTO.ESTADO_ACTIVA);     // ✅ USAR CONSTANTE
            reserva.setFechaCreacion(LocalDate.now());           // ✅ FECHA CREACIÓN

            System.out.println("📋 DATOS BÁSICOS CONFIGURADOS:");
            System.out.println("   - Código: " + reserva.getCodigo());
            System.out.println("   - Cliente: " + reserva.getClienteNombre());
            System.out.println("   - Habitación: " + reserva.getCodigoHabitacion());
            System.out.println("   - Estado: " + reserva.getEstado());

            // ✅ Calcular precio
            try {
                System.out.println("💰 CALCULANDO PRECIO...");
                ServicioHabitaciones servicioHabitaciones = new ServicioHabitaciones();
                List<HabitacionDTO> todasHabitaciones = servicioHabitaciones.listarHabitaciones();

                HabitacionDTO habitacionSeleccionada = null;
                for (HabitacionDTO hab : todasHabitaciones) {
                    if (hab.getCodigo().equals(codigoHabitacion)) {
                        habitacionSeleccionada = hab;
                        break;
                    }
                }

                if (habitacionSeleccionada != null) {
                    double precioTotal = calcularPrecioTotal(habitacionSeleccionada, fechaDesde, fechaHasta);
                    reserva.setPrecioTotal(precioTotal);

                    // ✅ ESTABLECER CÓDIGO DE HOTEL SI ESTÁ DISPONIBLE
                    reserva.setCodigoHotel(habitacionSeleccionada.getCodigoHotel());

                    System.out.println("✅ Precio calculado: $" + precioTotal);
                    System.out.println("✅ Hotel: " + habitacionSeleccionada.getCodigoHotel());
                } else {
                    System.out.println("❌ Habitación no encontrada: " + codigoHabitacion);
                    return new ResultadoOperacion(false, "Habitación no encontrada: " + codigoHabitacion);
                }

            } catch (Exception e) {
                System.out.println("❌ Error calculando precio: " + e.getMessage());
                return new ResultadoOperacion(false, "Error calculando precio: " + e.getMessage());
            }

            // ✅ VALIDAR ANTES DE ENVIAR
            System.out.println("🔍 VALIDANDO RESERVA...");
            boolean esValida = reserva.esValida();
            System.out.println("✅ Validación: " + esValida);

            if (!esValida) {
                System.out.println("❌ RESERVA INVÁLIDA - DETALLES:");
                System.out.println("   - Código: '" + reserva.getCodigo() + "' (vacío: " + (reserva.getCodigo() == null || reserva.getCodigo().trim().isEmpty()) + ")");
                System.out.println("   - Habitación: '" + reserva.getCodigoHabitacion() + "' (vacío: " + (reserva.getCodigoHabitacion() == null || reserva.getCodigoHabitacion().trim().isEmpty()) + ")");
                System.out.println("   - Fecha desde: " + reserva.getFechaDesde());
                System.out.println("   - Fecha hasta: " + reserva.getFechaHasta());
                System.out.println("   - Estado: '" + reserva.getEstado() + "'");
                return new ResultadoOperacion(false, "Datos de reserva inválidos después de configurar");
            }

            System.out.println("🚀 ENVIANDO RESERVA AL SERVIDOR...");

            // ✅ Usar el método principal de crear reserva
            ResultadoOperacion resultado = crearReserva(reserva);

            System.out.println("📊 RESULTADO: " + (resultado.isExito() ? "ÉXITO" : "ERROR"));
            System.out.println("📝 MENSAJE: " + resultado.getMensaje());

            return resultado;

        } catch (Exception e) {
            System.out.println("💥 EXCEPCIÓN: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error creando reserva: " + e.getMessage(), e);
        } finally {
            System.out.println("=== FIN CREAR RESERVA SIMPLIFICADA ===\n");
        }
    }

    /**
     * ✅ MODIFICAR RESERVA EXISTENTE
     */
    public ResultadoOperacion modificarReserva(ReservaDTO reserva) throws IOException {
        try {
            if (reserva == null || !reserva.esValida()) {
                return new ResultadoOperacion(false, "Datos de reserva inválidos");
            }

            JSONObject reservaJson = JsonUtil.reservaToJson(reserva);
            String respuestaJson = cliente.enviarOperacion("MODIFICAR_RESERVA", reservaJson.toString());
            JSONObject respuesta = new JSONObject(respuestaJson);

            boolean exito = "OK".equals(respuesta.getString("estado"));
            String mensaje = respuesta.getString("mensaje");

            return new ResultadoOperacion(exito, mensaje);

        } catch (Exception e) {
            throw new IOException("Error modificando reserva: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ ELIMINAR/CANCELAR RESERVA
     */
    public ResultadoOperacion eliminarReserva(String codigoReserva) throws IOException {
        try {
            if (codigoReserva == null || codigoReserva.trim().isEmpty()) {
                return new ResultadoOperacion(false, "Código de reserva requerido");
            }

            String respuestaJson = cliente.enviarOperacion("ELIMINAR_RESERVA", codigoReserva);
            JSONObject respuesta = new JSONObject(respuestaJson);

            boolean exito = "OK".equals(respuesta.getString("estado"));
            String mensaje = respuesta.getString("mensaje");

            return new ResultadoOperacion(exito, mensaje);

        } catch (Exception e) {
            throw new IOException("Error eliminando reserva: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ BUSCAR RESERVA POR CÓDIGO
     */
    public ReservaDTO buscarReserva(String codigoReserva) throws IOException {
        try {
            if (codigoReserva == null || codigoReserva.trim().isEmpty()) {
                return null;
            }

            String respuestaJson = cliente.enviarOperacion("BUSCAR_RESERVA", codigoReserva);
            JSONObject respuesta = new JSONObject(respuestaJson);

            if ("OK".equals(respuesta.getString("estado")) && respuesta.has("reserva")) {
                JSONObject reservaJson = respuesta.getJSONObject("reserva");
                return JsonUtil.jsonToReserva(reservaJson);
            }

            return null;

        } catch (Exception e) {
            throw new IOException("Error buscando reserva: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ LISTAR TODAS LAS RESERVAS
     */
    public List<ReservaDTO> listarReservas() throws IOException {
        return listarReservasPorHotel(null);
    }

    /**
     * ✅ LISTAR RESERVAS DE UN HOTEL ESPECÍFICO
     */
    public List<ReservaDTO> listarReservasPorHotel(String codigoHotel) throws IOException {
        try {
            JSONObject filtro = new JSONObject();
            if (codigoHotel != null && !codigoHotel.trim().isEmpty()) {
                filtro.put("codigoHotel", codigoHotel);
            }

            String respuestaJson = cliente.enviarOperacion("LISTAR_RESERVAS", filtro.toString());
            JSONObject respuesta = new JSONObject(respuestaJson);

            if ("OK".equals(respuesta.getString("estado"))) {
                JSONArray reservasJson = respuesta.getJSONArray("reservas");
                return JsonUtil.jsonToReservas(reservasJson);
            } else {
                throw new IOException("Error del servidor: " + respuesta.getString("mensaje"));
            }

        } catch (Exception e) {
            throw new IOException("Error listando reservas: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ FINALIZAR RESERVAS VENCIDAS
     */
    public ResultadoOperacion finalizarReservasVencidas() throws IOException {
        try {
            String respuestaJson = cliente.enviarOperacion("FINALIZAR_RESERVAS_VENCIDAS", "");
            JSONObject respuesta = new JSONObject(respuestaJson);

            boolean exito = "OK".equals(respuesta.getString("estado"));
            String mensaje = respuesta.getString("mensaje");
            int finalizadas = respuesta.optInt("reservasFinalizadas", 0);

            return new ResultadoOperacion(exito, mensaje + " (Finalizadas: " + finalizadas + ")");

        } catch (Exception e) {
            throw new IOException("Error finalizando reservas: " + e.getMessage(), e);
        }
    }

    // ✅ =================== CLASES AUXILIARES ===================

    /**
     * Resultado completo de consulta de disponibilidad
     */
    public static class ResultadoConsultaDisponibilidad {
        private final boolean exito;
        private final String mensaje;
        private final List<HabitacionDTO> habitacionesDisponibles;
        private final List<ReservaDTO> reservasEnPeriodo;
        private final int totalDisponibles;
        private final int totalHabitaciones;
        private final LocalDate fechaDesde;
        private final LocalDate fechaHasta;
        private final String codigoHotel;

        public ResultadoConsultaDisponibilidad(boolean exito, String mensaje) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.habitacionesDisponibles = new ArrayList<>();
            this.reservasEnPeriodo = new ArrayList<>();
            this.totalDisponibles = 0;
            this.totalHabitaciones = 0;
            this.fechaDesde = null;
            this.fechaHasta = null;
            this.codigoHotel = null;
        }

        public ResultadoConsultaDisponibilidad(boolean exito, String mensaje,
                                               List<HabitacionDTO> habitacionesDisponibles,
                                               List<ReservaDTO> reservasEnPeriodo,
                                               int totalDisponibles, int totalHabitaciones,
                                               LocalDate fechaDesde, LocalDate fechaHasta,
                                               String codigoHotel) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.habitacionesDisponibles = habitacionesDisponibles != null ? habitacionesDisponibles : new ArrayList<>();
            this.reservasEnPeriodo = reservasEnPeriodo != null ? reservasEnPeriodo : new ArrayList<>();
            this.totalDisponibles = totalDisponibles;
            this.totalHabitaciones = totalHabitaciones;
            this.fechaDesde = fechaDesde;
            this.fechaHasta = fechaHasta;
            this.codigoHotel = codigoHotel;
        }

        // ✅ Getters
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public List<HabitacionDTO> getHabitacionesDisponibles() { return habitacionesDisponibles; }
        public List<ReservaDTO> getReservasEnPeriodo() { return reservasEnPeriodo; }
        public int getTotalDisponibles() { return totalDisponibles; }
        public int getTotalHabitaciones() { return totalHabitaciones; }
        public LocalDate getFechaDesde() { return fechaDesde; }
        public LocalDate getFechaHasta() { return fechaHasta; }
        public String getCodigoHotel() { return codigoHotel; }

        public boolean tieneHabitacionesDisponibles() {
            return !habitacionesDisponibles.isEmpty();
        }

        public double getPorcentajeOcupacion() {
            if (totalHabitaciones == 0) return 0;
            return ((double) (totalHabitaciones - totalDisponibles) / totalHabitaciones) * 100;
        }
    }

    /**
     * Resultado general de operaciones
     */
    public static class ResultadoOperacion {
        private final boolean exito;
        private final String mensaje;
        private final ReservaDTO reserva;

        public ResultadoOperacion(boolean exito, String mensaje) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.reserva = null;
        }

        public ResultadoOperacion(boolean exito, String mensaje, ReservaDTO reserva) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.reserva = reserva;
        }

        // ✅ Getters
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public ReservaDTO getReserva() { return reserva; }
        public boolean tieneReserva() { return reserva != null; }
    }

    // ✅ =================== MÉTODOS UTILITARIOS ===================

    /**
     * Verifica si una habitación está disponible en fechas específicas
     */
    public boolean verificarDisponibilidadHabitacion(String codigoHabitacion, LocalDate fechaDesde, LocalDate fechaHasta) throws IOException {
        List<HabitacionDTO> disponibles = obtenerHabitacionesDisponibles(fechaDesde, fechaHasta, null);
        return disponibles.stream().anyMatch(h -> h.getCodigo().equals(codigoHabitacion));
    }

    /**
     * Obtiene el próximo código de reserva disponible
     */
    public String obtenerProximoCodigoReserva() throws IOException {
        try {
            List<ReservaDTO> reservas = listarReservas();
            int maxNumero = 0;

            for (ReservaDTO reserva : reservas) {
                try {
                    String codigo = reserva.getCodigo();
                    if (codigo.startsWith("RES")) {
                        int numero = Integer.parseInt(codigo.substring(3));
                        maxNumero = Math.max(maxNumero, numero);
                    }
                } catch (NumberFormatException e) {
                    // Ignorar códigos que no sigan el formato
                }
            }

            return String.format("RES%04d", maxNumero + 1);

        } catch (Exception e) {
            throw new IOException("Error obteniendo próximo código: " + e.getMessage(), e);
        }
    }

    /**
     * Valida fechas de reserva
     */
    public static boolean validarFechasReserva(LocalDate fechaDesde, LocalDate fechaHasta) {
        if (fechaDesde == null || fechaHasta == null) {
            return false;
        }

        LocalDate hoy = LocalDate.now();
        return !fechaDesde.isBefore(hoy) && !fechaDesde.isAfter(fechaHasta);
    }

    /**
     * Calcula precio total de reserva
     */
    public static double calcularPrecioTotal(HabitacionDTO habitacion, LocalDate fechaDesde, LocalDate fechaHasta) {
        if (habitacion == null || fechaDesde == null || fechaHasta == null) {
            return 0.0;
        }

        long dias = fechaDesde.until(fechaHasta).getDays();
        return dias * habitacion.getPrecio();
    }
}