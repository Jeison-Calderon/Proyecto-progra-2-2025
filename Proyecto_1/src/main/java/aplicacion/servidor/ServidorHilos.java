package aplicacion.servidor;

import aplicacion.data.HabitacionesData;
import aplicacion.data.HotelesData;
import aplicacion.data.ReservasData;
import aplicacion.data.UsuarioDAO;
import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import aplicacion.dto.ReservaDTO;
import aplicacion.dto.Usuario;
import aplicacion.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            System.out.println("📡 " + LocalDateTime.now() + " - Operación recibida: " + operacion);

            switch (operacion) {
                case "LOGIN":
                    manejarLogin(entrada, salida);
                    break;
                case "REGISTRAR_USUARIO":
                    manejarRegistroUsuario(entrada, salida);
                    break;
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
                case "CONSULTAR_DISPONIBILIDAD":
                    manejarConsultarDisponibilidad(entrada, salida);
                    break;
                case "HABITACIONES_DISPONIBLES":
                    manejarHabitacionesDisponibles(entrada, salida);
                    break;
                case "LISTAR_RESERVAS":
                    manejarListarReservas(entrada, salida);
                    break;
                case "CREAR_RESERVA":
                    manejarCrearReserva(entrada, salida);
                    break;
                case "MODIFICAR_RESERVA":
                    manejarModificarReserva(entrada, salida);
                    break;
                case "ELIMINAR_RESERVA":
                    manejarEliminarReserva(entrada, salida);
                    break;
                case "BUSCAR_RESERVA":
                    manejarBuscarReserva(entrada, salida);
                    break;
                case "FINALIZAR_RESERVAS_VENCIDAS":
                    manejarFinalizarReservasVencidas(salida);
                    break;
                case "ENVIAR_ARCHIVO":
                    manejarEnviarArchivo(entrada, salida);
                    break;
                default:
                    enviarError(salida, "Operación no reconocida: " + operacion);
                    break;
            }

        } catch (IOException e) {
            System.err.println("❌ " + LocalDateTime.now() + " - Error manejando cliente: " + e.getMessage());
        } finally {
            try {
                cliente.close();
                System.out.println("📱 " + LocalDateTime.now() + " - Cliente desconectado");
            } catch (IOException e) {
                System.err.println("❌ " + LocalDateTime.now() + " - Error cerrando conexión: " + e.getMessage());
            }
        }
    }

    private void manejarLogin(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String loginJson = entrada.readUTF();
            JSONObject datos = new JSONObject(loginJson);

            String usuario = datos.getString("usuario");
            String contrasena = datos.getString("contrasena");

            Usuario usuarioEncontrado = UsuarioDAO.buscarPorCredenciales(usuario, contrasena);

            JSONObject respuesta = new JSONObject();
            if (usuarioEncontrado != null) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Login exitoso");
                System.out.println("✅ " + LocalDateTime.now() + " - Usuario autenticado: " + usuario);
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Usuario o contraseña incorrectos");
                System.out.println("❌ " + LocalDateTime.now() + " - Intento de login fallido para usuario: " + usuario);
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error en el login: " + e.getMessage());
        }
    }

    private void manejarRegistroUsuario(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String datosJson = entrada.readUTF();
            JSONObject datos = new JSONObject(datosJson);

            String usuario = datos.getString("usuario");
            String contrasena = datos.getString("contrasena");
            String confirmarContrasena = datos.getString("confirmarContrasena");

            // Validaciones
            if (usuario.isEmpty() || contrasena.isEmpty()) {
                enviarError(salida, "El usuario y la contraseña son requeridos");
                return;
            }

            if (!contrasena.equals(confirmarContrasena)) {
                enviarError(salida, "Las contraseñas no coinciden");
                return;
            }

            if (usuario.length() < 4) {
                enviarError(salida, "El nombre de usuario debe tener al menos 4 caracteres");
                return;
            }

            if (contrasena.length() < 6) {
                enviarError(salida, "La contraseña debe tener al menos 6 caracteres");
                return;
            }

            // Verificar usuario existente
            ArrayList<Usuario> usuarios = UsuarioDAO.cargarUsuarios();
            boolean usuarioExistente = usuarios.stream()
                    .anyMatch(u -> u.getUsername().equals(usuario));

            if (usuarioExistente) {
                enviarError(salida, "El nombre de usuario ya existe");
                return;
            }

            // Crear y guardar nuevo usuario
            Usuario nuevoUsuario = new Usuario(usuario, contrasena);
            usuarios.add(nuevoUsuario);
            UsuarioDAO.guardarUsuarios(usuarios);

            JSONObject respuesta = new JSONObject();
            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Usuario registrado correctamente");
            salida.writeUTF(respuesta.toString());

            System.out.println("✅ " + LocalDateTime.now() + " - Nuevo usuario registrado: " + usuario);

        } catch (Exception e) {
            enviarError(salida, "Error al registrar usuario: " + e.getMessage());
        }
    }


    private void manejarListarHoteles(DataOutputStream salida) throws IOException {
        try {
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

    private void manejarConsultarDisponibilidad(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String consultaJson = entrada.readUTF();
            JSONObject jsonConsulta = new JSONObject(consultaJson);

            String fechaDesdeStr = jsonConsulta.getString("fechaDesde");
            String fechaHastaStr = jsonConsulta.getString("fechaHasta");
            String codigoHotel = jsonConsulta.optString("codigoHotel", null);

            LocalDate fechaDesde = JsonUtil.stringToFecha(fechaDesdeStr);
            LocalDate fechaHasta = JsonUtil.stringToFecha(fechaHastaStr);

            if (fechaDesde == null || fechaHasta == null) {
                enviarError(salida, "Fechas inválidas en la consulta");
                return;
            }

            List<String> codigosDisponibles = ReservasData.obtenerHabitacionesDisponibles(fechaDesde, fechaHasta, codigoHotel);

            List<HabitacionDTO> todasHabitaciones = HabitacionesData.listar();
            List<HabitacionDTO> habitacionesDisponibles = todasHabitaciones.stream()
                    .filter(h -> codigosDisponibles.contains(h.getCodigo()))
                    .filter(h -> codigoHotel == null || codigoHotel.equals(h.getCodigoHotel()))
                    .filter(HabitacionDTO::estaDisponible)
                    .collect(Collectors.toList());

            List<ReservaDTO> reservasEnPeriodo = ReservasData.obtenerReservasEnPeriodo(fechaDesde, fechaHasta);
            if (codigoHotel != null) {
                reservasEnPeriodo = reservasEnPeriodo.stream()
                        .filter(r -> codigoHotel.equals(r.getCodigoHotel()))
                        .collect(Collectors.toList());
            }

            JSONObject respuesta = JsonUtil.crearRespuestaConsultaDisponibilidad(
                    habitacionesDisponibles, reservasEnPeriodo, fechaDesdeStr, fechaHastaStr, codigoHotel);

            salida.writeUTF(respuesta.toString());
            System.out.println("✅ Consulta de disponibilidad procesada: " + habitacionesDisponibles.size() + " habitaciones disponibles");

        } catch (Exception e) {
            enviarError(salida, "Error en consulta de disponibilidad: " + e.getMessage());
        }
    }

    private void manejarHabitacionesDisponibles(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String consultaJson = entrada.readUTF();
            JSONObject jsonConsulta = new JSONObject(consultaJson);

            String fechaDesdeStr = jsonConsulta.getString("fechaDesde");
            String fechaHastaStr = jsonConsulta.getString("fechaHasta");
            String codigoHotel = jsonConsulta.optString("codigoHotel", null);

            LocalDate fechaDesde = JsonUtil.stringToFecha(fechaDesdeStr);
            LocalDate fechaHasta = JsonUtil.stringToFecha(fechaHastaStr);

            if (fechaDesde == null || fechaHasta == null) {
                enviarError(salida, "Fechas inválidas");
                return;
            }

            List<String> codigosDisponibles = ReservasData.obtenerHabitacionesDisponibles(fechaDesde, fechaHasta, codigoHotel);

            List<HabitacionDTO> todasHabitaciones = HabitacionesData.listar();
            List<HabitacionDTO> habitacionesDisponibles = todasHabitaciones.stream()
                    .filter(h -> codigosDisponibles.contains(h.getCodigo()))
                    .filter(h -> codigoHotel == null || codigoHotel.equals(h.getCodigoHotel()))
                    .filter(HabitacionDTO::estaDisponible)
                    .collect(Collectors.toList());

            JSONObject respuesta = new JSONObject();
            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Habitaciones disponibles encontradas");
            respuesta.put("habitaciones", JsonUtil.habitacionesToJsonCompleto(habitacionesDisponibles));
            respuesta.put("total", habitacionesDisponibles.size());

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error obteniendo habitaciones disponibles: " + e.getMessage());
        }
    }

    private void manejarListarReservas(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String filtroJson = entrada.readUTF();
            JSONObject jsonFiltro = new JSONObject(filtroJson);
            String codigoHotel = jsonFiltro.optString("codigoHotel", null);

            List<ReservaDTO> reservas;
            if (codigoHotel != null && !codigoHotel.trim().isEmpty()) {
                reservas = ReservasData.obtenerReservasPorHotel(codigoHotel);
            } else {
                reservas = ReservasData.listar();
            }

            JSONObject respuesta = new JSONObject();
            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Reservas listadas correctamente");
            respuesta.put("reservas", JsonUtil.reservasToJson(reservas));
            respuesta.put("total", reservas.size());

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al listar reservas: " + e.getMessage());
        }
    }

    private void manejarCrearReserva(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String reservaJson = entrada.readUTF();
            JSONObject jsonReserva = new JSONObject(reservaJson);
            ReservaDTO reservaDTO = JsonUtil.jsonToReserva(jsonReserva);

            if (!reservaDTO.esValida()) {
                enviarError(salida, "Datos de reserva inválidos");
                return;
            }

            if (reservaDTO.getCodigo() == null || reservaDTO.getCodigo().trim().isEmpty()) {
                reservaDTO.setCodigo(ReservasData.generarProximoCodigo());
            }

            boolean guardada = ReservasData.guardar(reservaDTO);

            JSONObject respuesta = new JSONObject();
            if (guardada) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Reserva creada correctamente");
                respuesta.put("reserva", JsonUtil.reservaToJson(reservaDTO));
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "No se pudo crear la reserva - posible conflicto de fechas");
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al crear reserva: " + e.getMessage());
        }
    }

    private void manejarModificarReserva(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String reservaJson = entrada.readUTF();
            JSONObject jsonReserva = new JSONObject(reservaJson);
            ReservaDTO reservaDTO = JsonUtil.jsonToReserva(jsonReserva);

            boolean modificada = ReservasData.modificar(reservaDTO);

            JSONObject respuesta = new JSONObject();
            if (modificada) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Reserva modificada correctamente");
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Reserva no encontrada o conflicto de fechas");
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al modificar reserva: " + e.getMessage());
        }
    }

    private void manejarEliminarReserva(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String codigo = entrada.readUTF();
            boolean eliminada = ReservasData.eliminar(codigo);

            JSONObject respuesta = new JSONObject();
            if (eliminada) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Reserva eliminada correctamente");
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Reserva no encontrada");
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al eliminar reserva: " + e.getMessage());
        }
    }

    private void manejarBuscarReserva(DataInputStream entrada, DataOutputStream salida) throws IOException {
        try {
            String codigo = entrada.readUTF();
            ReservaDTO reserva = ReservasData.buscarPorCodigo(codigo);

            JSONObject respuesta = new JSONObject();
            if (reserva != null) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Reserva encontrada");
                respuesta.put("reserva", JsonUtil.reservaToJson(reserva));
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Reserva no encontrada");
            }

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al buscar reserva: " + e.getMessage());
        }
    }

    private void manejarFinalizarReservasVencidas(DataOutputStream salida) throws IOException {
        try {
            int finalizadas = ReservasData.finalizarReservasVencidas();

            JSONObject respuesta = new JSONObject();
            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Proceso completado");
            respuesta.put("reservasFinalizadas", finalizadas);

            salida.writeUTF(respuesta.toString());

        } catch (Exception e) {
            enviarError(salida, "Error al finalizar reservas vencidas: " + e.getMessage());
        }
    }

    private void enviarError(DataOutputStream salida, String mensaje) throws IOException {
        JSONObject respuesta = JsonUtil.crearRespuestaError(mensaje);
        salida.writeUTF(respuesta.toString());
        System.err.println("❌ " + LocalDateTime.now() + " - Error enviado al cliente: " + mensaje);
    }
}