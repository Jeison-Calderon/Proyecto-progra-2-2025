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

public class ServicioReservas {
    private ClienteSocket cliente;

    public ServicioReservas() {
        this.cliente = new ClienteSocket();
    }

    public ResultadoConsultaDisponibilidad consultarDisponibilidad(LocalDate fechaDesde, LocalDate fechaHasta, String codigoHotel) throws IOException {
        try {
            JSONObject consulta = new JSONObject();
            consulta.put("fechaDesde", JsonUtil.fechaToString(fechaDesde));
            consulta.put("fechaHasta", JsonUtil.fechaToString(fechaHasta));
            if (codigoHotel != null && !codigoHotel.trim().isEmpty()) {
                consulta.put("codigoHotel", codigoHotel);
            }

            String respuestaJson = cliente.enviarOperacion("CONSULTAR_DISPONIBILIDAD", consulta.toString());
            JSONObject respuesta = new JSONObject(respuestaJson);

            if ("OK".equals(respuesta.getString("estado"))) {
                JSONArray habitacionesJson = respuesta.getJSONArray("habitacionesDisponibles");
                List<HabitacionDTO> habitaciones = JsonUtil.jsonToHabitaciones(habitacionesJson);

                JSONArray reservasJson = respuesta.optJSONArray("reservasEnPeriodo");
                List<ReservaDTO> reservas = reservasJson != null ? JsonUtil.jsonToReservas(reservasJson) : new ArrayList<>();

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

    public List<DisponibilidadDTO> consultarDisponibilidad(String codigoHotel, LocalDate fechaDesde, LocalDate fechaHasta) throws IOException {
        try {
            ResultadoConsultaDisponibilidad resultado = consultarDisponibilidad(fechaDesde, fechaHasta, codigoHotel);

            if (resultado.isExito()) {
                List<DisponibilidadDTO> disponibilidad = new ArrayList<>();

                for (HabitacionDTO habitacion : resultado.getHabitacionesDisponibles()) {
                    DisponibilidadDTO item = new DisponibilidadDTO();
                    item.setCodigoHabitacion(habitacion.getCodigo());
                    item.setNumeroHabitacion(habitacion.getNumero());
                    item.setEstilo(habitacion.getEstilo());
                    item.setPrecio(habitacion.getPrecio());
                    item.setCantidadImagenes(habitacion.getImagenes().size());
                    item.setEstado(habitacion.getEstado());

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

    // ✅ NUEVO MÉTODO: Crear reserva completa con todos los campos requeridos
    public ResultadoOperacion crearReservaCompleta(String nombreCliente, String recepcionista,
                                                   String codigoHabitacion, LocalDate fechaDesde,
                                                   LocalDate fechaHasta) throws IOException {
        System.out.println("\n=== CREAR RESERVA COMPLETA - DEBUG ===");

        try {
            // Validaciones previas
            if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
                return new ResultadoOperacion(false, "El nombre del cliente es requerido");
            }

            if (recepcionista == null || recepcionista.trim().isEmpty()) {
                return new ResultadoOperacion(false, "El nombre del recepcionista es requerido");
            }

            if (codigoHabitacion == null || codigoHabitacion.trim().isEmpty()) {
                return new ResultadoOperacion(false, "El código de habitación es requerido");
            }

            if (fechaDesde == null || fechaHasta == null) {
                return new ResultadoOperacion(false, "Las fechas son requeridas");
            }

            if (fechaDesde.isBefore(LocalDate.now())) {
                return new ResultadoOperacion(false, "La fecha de inicio no puede ser anterior a hoy");
            }

            if (!fechaDesde.isBefore(fechaHasta)) {
                return new ResultadoOperacion(false, "La fecha de fin debe ser posterior a la fecha de inicio");
            }

            // Generar código de reserva
            String codigoReserva;
            try {
                codigoReserva = obtenerProximoCodigoReserva();
                System.out.println("✅ Código generado: " + codigoReserva);
            } catch (Exception e) {
                System.out.println("❌ Error generando código, usando fallback");
                codigoReserva = "RES" + System.currentTimeMillis();
            }

            // Crear reserva con todos los campos
            ReservaDTO reserva = new ReservaDTO();
            reserva.setCodigo(codigoReserva);
            reserva.setClienteNombre(nombreCliente.trim());
            reserva.setRecepcionista(recepcionista.trim());  // ✅ NUEVO: Campo recepcionista
            reserva.setCodigoHabitacion(codigoHabitacion);
            reserva.setFechaDesde(fechaDesde);
            reserva.setFechaHasta(fechaHasta);
            reserva.setEstado(ReservaDTO.ESTADO_ACTIVA);
            reserva.setFechaCreacion(LocalDate.now());

            System.out.println("📋 DATOS COMPLETOS CONFIGURADOS:");
            System.out.println("   - Código: " + reserva.getCodigo());
            System.out.println("   - Cliente: " + reserva.getClienteNombre());
            System.out.println("   - Recepcionista: " + reserva.getRecepcionista());  // ✅ NUEVO
            System.out.println("   - Habitación: " + reserva.getCodigoHabitacion());
            System.out.println("   - Estado: " + reserva.getEstado());

            // Obtener información de la habitación y calcular precio
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

            // Validar reserva completa
            System.out.println("🔍 VALIDANDO RESERVA COMPLETA...");
            boolean esValida = reserva.esValida();
            System.out.println("✅ Validación: " + esValida);

            if (!esValida) {
                System.out.println("❌ RESERVA INVÁLIDA - DETALLES:");
                System.out.println("   - Código: '" + reserva.getCodigo() + "' (vacío: " + (reserva.getCodigo() == null || reserva.getCodigo().trim().isEmpty()) + ")");
                System.out.println("   - Cliente: '" + reserva.getClienteNombre() + "' (vacío: " + (reserva.getClienteNombre() == null || reserva.getClienteNombre().trim().isEmpty()) + ")");
                System.out.println("   - Recepcionista: '" + reserva.getRecepcionista() + "' (vacío: " + (reserva.getRecepcionista() == null || reserva.getRecepcionista().trim().isEmpty()) + ")");  // ✅ NUEVO
                System.out.println("   - Habitación: '" + reserva.getCodigoHabitacion() + "' (vacío: " + (reserva.getCodigoHabitacion() == null || reserva.getCodigoHabitacion().trim().isEmpty()) + ")");
                System.out.println("   - Fecha desde: " + reserva.getFechaDesde());
                System.out.println("   - Fecha hasta: " + reserva.getFechaHasta());
                System.out.println("   - Estado: '" + reserva.getEstado() + "'");
                return new ResultadoOperacion(false, "Datos de reserva inválidos después de configurar");
            }

            // Verificar disponibilidad antes de crear
            System.out.println("🔍 VERIFICANDO DISPONIBILIDAD...");
            boolean disponible = verificarDisponibilidadHabitacion(codigoHabitacion, fechaDesde, fechaHasta);
            if (!disponible) {
                return new ResultadoOperacion(false, "La habitación no está disponible en las fechas seleccionadas");
            }
            System.out.println("✅ Habitación disponible");

            // Enviar al servidor
            System.out.println("🚀 ENVIANDO RESERVA COMPLETA AL SERVIDOR...");
            ResultadoOperacion resultado = crearReserva(reserva);

            System.out.println("📊 RESULTADO: " + (resultado.isExito() ? "ÉXITO" : "ERROR"));
            System.out.println("📝 MENSAJE: " + resultado.getMensaje());

            return resultado;

        } catch (Exception e) {
            System.out.println("💥 EXCEPCIÓN: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error creando reserva completa: " + e.getMessage(), e);
        } finally {
            System.out.println("=== FIN CREAR RESERVA COMPLETA ===\n");
        }
    }

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
            System.out.println("   - Recepcionista: " + reserva.getRecepcionista());  // ✅ NUEVO: Log del recepcionista
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
                System.out.println(reservaJson.toString(2));
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

    // ✅ ACTUALIZADO: Método simplificado actualizado para mantener retrocompatibilidad
    public ResultadoOperacion crearReserva(String nombreCliente, String codigoHabitacion, LocalDate fechaDesde, LocalDate fechaHasta) throws IOException {
        // Para mantener retrocompatibilidad, usar "Sistema" como recepcionista por defecto
        return crearReservaCompleta(nombreCliente, "Sistema", codigoHabitacion, fechaDesde, fechaHasta);
    }

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

    public List<ReservaDTO> listarReservas() throws IOException {
        return listarReservasPorHotel(null);
    }

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

        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public ReservaDTO getReserva() { return reserva; }
        public boolean tieneReserva() { return reserva != null; }
    }


    public boolean verificarDisponibilidadHabitacion(String codigoHabitacion, LocalDate fechaDesde, LocalDate fechaHasta) throws IOException {
        List<HabitacionDTO> disponibles = obtenerHabitacionesDisponibles(fechaDesde, fechaHasta, null);
        return disponibles.stream().anyMatch(h -> h.getCodigo().equals(codigoHabitacion));
    }

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
                }
            }

            return String.format("RES%04d", maxNumero + 1);

        } catch (Exception e) {
            throw new IOException("Error obteniendo próximo código: " + e.getMessage(), e);
        }
    }

    public static boolean validarFechasReserva(LocalDate fechaDesde, LocalDate fechaHasta) {
        if (fechaDesde == null || fechaHasta == null) {
            return false;
        }

        LocalDate hoy = LocalDate.now();
        return !fechaDesde.isBefore(hoy) && !fechaDesde.isAfter(fechaHasta);
    }

    public static double calcularPrecioTotal(HabitacionDTO habitacion, LocalDate fechaDesde, LocalDate fechaHasta) {
        if (habitacion == null || fechaDesde == null || fechaHasta == null) {
            return 0.0;
        }

        long dias = fechaDesde.until(fechaHasta).getDays();
        return dias * habitacion.getPrecio();
    }
}