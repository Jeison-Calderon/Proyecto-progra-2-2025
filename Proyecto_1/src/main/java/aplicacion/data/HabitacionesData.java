package aplicacion.data;

import aplicacion.domain.Habitacion;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HabitacionesData {
    private static final String ARCHIVO = "habitaciones.dat";

    public static synchronized String guardar(String estilo, double precio, String codigoHotel) {
        List<Habitacion> habitaciones = listar();

        // ✅ CORREGIDO: Validar duplicados SOLO dentro del mismo hotel
        for (Habitacion h : habitaciones) {
            if (h.getCodigoHotel().equals(codigoHotel)
                    && h.getEstilo().equalsIgnoreCase(estilo)
                    && h.getPrecio() == precio) {
                return "duplicado";
            }
        }

        // ✅ CORREGIDO: Generar código específico por hotel
        String codigo = generarCodigoHabitacion(habitaciones, codigoHotel);

        // ✅ CORREGIDO: Constructor completo con codigoHotel
        habitaciones.add(new Habitacion(codigo, estilo, precio, codigoHotel));
        sobrescribirArchivo(habitaciones);
        return codigo;
    }

    // ✅ NUEVO: Generar código específico por hotel
    private static String generarCodigoHabitacion(List<Habitacion> habitaciones, String codigoHotel) {
        int maxNumero = 0;
        String prefijo = codigoHotel.replace("H-", "R") + "-";  // H-001 → R001-

        for (Habitacion h : habitaciones) {
            if (h.getCodigoHotel().equals(codigoHotel)) {
                String codigo = h.getCodigo();
                if (codigo.startsWith(prefijo)) {
                    try {
                        int numero = Integer.parseInt(codigo.substring(prefijo.length()));
                        maxNumero = Math.max(maxNumero, numero);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return String.format("%s%03d", prefijo, maxNumero + 1);
    }

    public static List<Habitacion> listar() {
        List<Habitacion> list = new ArrayList<>();
        try (
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO))
        ) {
            while (true) {
                list.add((Habitacion) ois.readObject());
            }
        } catch (EOFException ignored) {
        } catch (FileNotFoundException e) {
            // El archivo no existe aún, retornar lista vacía
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static synchronized boolean modificar(Habitacion habitacionModificada) {
        List<Habitacion> habitaciones = listar();
        boolean encontrada = false;

        for (int i = 0; i < habitaciones.size(); i++) {
            Habitacion h = habitaciones.get(i);
            if (h.getCodigo().equals(habitacionModificada.getCodigo())) {
                habitaciones.set(i, habitacionModificada);
                encontrada = true;
                break;
            }
        }

        if (encontrada) {
            sobrescribirArchivo(habitaciones);
        }
        return encontrada;
    }

    public static synchronized boolean eliminar(String codigo) {
        List<Habitacion> habitaciones = listar();
        boolean encontrada = false;

        for (int i = 0; i < habitaciones.size(); i++) {
            if (habitaciones.get(i).getCodigo().equals(codigo)) {
                habitaciones.remove(i);
                encontrada = true;
                break;
            }
        }

        if (encontrada) {
            sobrescribirArchivo(habitaciones);
        }
        return encontrada;
    }

    private static void sobrescribirArchivo(List<Habitacion> habitaciones) {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO))
        ) {
            for (Habitacion h : habitaciones) {
                oos.writeObject(h);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}