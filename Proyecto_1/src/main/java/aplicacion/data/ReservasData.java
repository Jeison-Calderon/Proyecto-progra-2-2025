package aplicacion.data;

import aplicacion.dto.ReservaDTO;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReservasData {
    private static final String ARCHIVO_RESERVAS = "reservas.dat";

    //Lista todas las reservas desde el archivo

    public static List<ReservaDTO> listar() {
        List<ReservaDTO> reservas = new ArrayList<>();
        File archivo = new File(ARCHIVO_RESERVAS);

        if (!archivo.exists()) {
            System.out.println("📁 Archivo reservas.dat no existe, retornando lista vacía");
            return reservas;
        }

        try (FileInputStream fis = new FileInputStream(archivo);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            while (true) {
                try {
                    ReservaDTO reserva = (ReservaDTO) ois.readObject();
                    if (reserva != null) {
                        reservas.add(reserva);
                    }
                } catch (EOFException e) {
                    // Fin del archivo alcanzado
                    break;
                }
            }
            System.out.println("✅ Cargadas " + reservas.size() + " reservas desde " + ARCHIVO_RESERVAS);

        } catch (Exception e) {
            System.err.println("❌ Error cargando reservas: " + e.getMessage());
            e.printStackTrace();
        }

        return reservas;
    }

     //Guarda una reserva en el archivo
    public static boolean guardar(ReservaDTO reserva) {
        if (reserva == null || !reserva.esValida()) {
            System.err.println("❌ Reserva inválida para guardar");
            return false;
        }

        List<ReservaDTO> reservas = listar();

        // Verificar si ya existe una reserva con el mismo código
        boolean existe = reservas.stream()
                .anyMatch(r -> r.getCodigo().equals(reserva.getCodigo()));

        if (existe) {
            System.err.println("❌ Ya existe una reserva con código: " + reserva.getCodigo());
            return false;
        }

        // Verificar disponibilidad de la habitación en las fechas
        if (hayConflictoFechas(reserva, reservas)) {
            System.err.println("❌ Conflicto de fechas para habitación: " + reserva.getCodigoHabitacion());
            return false;
        }

        reservas.add(reserva);
        return guardarTodas(reservas);
    }


     //Modifica una reserva existente
    public static boolean modificar(ReservaDTO reservaModificada) {
        if (reservaModificada == null || !reservaModificada.esValida()) {
            System.err.println("❌ Reserva inválida para modificar");
            return false;
        }

        List<ReservaDTO> reservas = listar();
        boolean encontrada = false;

        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i).getCodigo().equals(reservaModificada.getCodigo())) {
                // Verificar conflictos excluyendo la reserva actual
                List<ReservaDTO> otrasReservas = new ArrayList<>(reservas);
                otrasReservas.remove(i);

                if (hayConflictoFechas(reservaModificada, otrasReservas)) {
                    System.err.println("❌ Conflicto de fechas al modificar reserva: " + reservaModificada.getCodigo());
                    return false;
                }

                reservas.set(i, reservaModificada);
                encontrada = true;
                break;
            }
        }

        if (!encontrada) {
            System.err.println("❌ Reserva no encontrada para modificar: " + reservaModificada.getCodigo());
            return false;
        }

        return guardarTodas(reservas);
    }

     //Elimina una reserva por código
    public static boolean eliminar(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            System.err.println("❌ Código de reserva inválido para eliminar");
            return false;
        }

        List<ReservaDTO> reservas = listar();
        boolean eliminada = reservas.removeIf(r -> r.getCodigo().equals(codigo));

        if (!eliminada) {
            System.err.println("❌ Reserva no encontrada para eliminar: " + codigo);
            return false;
        }

        return guardarTodas(reservas);
    }

    //Busca una reserva por código
    public static ReservaDTO buscarPorCodigo(String codigo) {
        return listar().stream()
                .filter(r -> r.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);
    }

    //Obtener habitaciones disponibles en un rango de fechas
    public static List<String> obtenerHabitacionesDisponibles(LocalDate fechaDesde, LocalDate fechaHasta, String codigoHotel) {
        List<ReservaDTO> reservasActivas = listar().stream()
                .filter(ReservaDTO::estaActiva)
                .filter(r -> codigoHotel == null || codigoHotel.equals(r.getCodigoHotel()))
                .collect(Collectors.toList());

        //Obtener todas las habitaciones del hotel
        List<String> todasHabitaciones = obtenerTodasHabitacionesDelHotel(codigoHotel);
        List<String> habitacionesOcupadas = new ArrayList<>();

        //Encontrar habitaciones ocupadas en el rango de fechas
        for (ReservaDTO reserva : reservasActivas) {
            if (reserva.seSuperponeConPeriodo(fechaDesde, fechaHasta)) {
                habitacionesOcupadas.add(reserva.getCodigoHabitacion());
            }
        }

        return todasHabitaciones.stream()
                .filter(h -> !habitacionesOcupadas.contains(h))
                .collect(Collectors.toList());
    }

    //Verifica si una habitación específica está disponible
    public static boolean estaHabitacionDisponible(String codigoHabitacion, LocalDate fechaDesde, LocalDate fechaHasta) {
        return listar().stream()
                .filter(ReservaDTO::estaActiva)
                .filter(r -> r.getCodigoHabitacion().equals(codigoHabitacion))
                .noneMatch(r -> r.seSuperponeConPeriodo(fechaDesde, fechaHasta));
    }

    //Obtiene reservas de una habitación específica
    public static List<ReservaDTO> obtenerReservasPorHabitacion(String codigoHabitacion) {
        return listar().stream()
                .filter(r -> r.getCodigoHabitacion().equals(codigoHabitacion))
                .filter(ReservaDTO::estaActiva)
                .collect(Collectors.toList());
    }

    //Obtiene reservas de un hotel específico
    public static List<ReservaDTO> obtenerReservasPorHotel(String codigoHotel) {
        return listar().stream()
                .filter(r -> codigoHotel.equals(r.getCodigoHotel()))
                .collect(Collectors.toList());
    }

    //Obtiene reservas en un rango de fechas
    public static List<ReservaDTO> obtenerReservasEnPeriodo(LocalDate fechaDesde, LocalDate fechaHasta) {
        return listar().stream()
                .filter(r -> r.seSuperponeConPeriodo(fechaDesde, fechaHasta))
                .collect(Collectors.toList());
    }

    //Guarda todas las reservas en el archivo
    private static boolean guardarTodas(List<ReservaDTO> reservas) {
        try (FileOutputStream fos = new FileOutputStream(ARCHIVO_RESERVAS);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            for (ReservaDTO reserva : reservas) {
                oos.writeObject(reserva);
            }

            System.out.println("✅ Guardadas " + reservas.size() + " reservas en " + ARCHIVO_RESERVAS);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error guardando reservas: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean hayConflictoFechas(ReservaDTO nuevaReserva, List<ReservaDTO> reservasExistentes) {
        return reservasExistentes.stream()
                .filter(ReservaDTO::estaActiva)
                .filter(r -> r.getCodigoHabitacion().equals(nuevaReserva.getCodigoHabitacion()))
                .anyMatch(r -> r.seSuperponeConPeriodo(nuevaReserva.getFechaDesde(), nuevaReserva.getFechaHasta()));
    }

    private static List<String> obtenerTodasHabitacionesDelHotel(String codigoHotel) {
        try {
            return HabitacionesData.listar().stream()
                    .filter(h -> codigoHotel == null || codigoHotel.equals(h.getCodigoHotel()))
                    .map(h -> h.getCodigo())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo habitaciones del hotel: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //Codigos de reservas
    public static String generarProximoCodigo() {
        List<ReservaDTO> reservas = listar();
        int maxNumero = 0;

        for (ReservaDTO reserva : reservas) {
            try {
                String codigo = reserva.getCodigo();
                if (codigo.startsWith("RES")) {
                    int numero = Integer.parseInt(codigo.substring(3));
                    maxNumero = Math.max(maxNumero, numero);
                }
            } catch (NumberFormatException e) {
                // Ignorar códigos que no sigan el formato RESxxxx
            }
        }

        return String.format("RES%04d", maxNumero + 1);
    }

    public static long contarReservasActivas() {
        return listar().stream()
                .filter(ReservaDTO::estaActiva)
                .count();
    }

    //Finalizar reservas vencidas automáticamente
    public static int finalizarReservasVencidas() {
        List<ReservaDTO> reservas = listar();
        LocalDate hoy = LocalDate.now();
        int finalizadas = 0;

        for (ReservaDTO reserva : reservas) {
            if (reserva.estaActiva() && reserva.getFechaHasta().isBefore(hoy)) {
                reserva.finalizar();
                finalizadas++;
            }
        }

        if (finalizadas > 0) {
            guardarTodas(reservas);
            System.out.println("✅ Finalizadas " + finalizadas + " reservas vencidas");
        }

        return finalizadas;
    }
}