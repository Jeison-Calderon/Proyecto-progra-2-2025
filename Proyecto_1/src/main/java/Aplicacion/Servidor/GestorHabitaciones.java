package Aplicacion.Servidor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorHabitaciones {
    private static final String ARCHIVO = "habitaciones.dat";

    public static synchronized String guardar(String estilo, double precio) {
        List<Habitacion> habitaciones = listar();
        for (Habitacion h : habitaciones) {
            if (h.getEstilo().equalsIgnoreCase(estilo) && h.getPrecio() == precio) {
                return "duplicado";
            }
        }
        String codigo = String.format("R-%03d", habitaciones.size() + 1);
        habitaciones.add(new Habitacion(codigo, estilo, precio));
        sobrescribirArchivo(habitaciones);
        return codigo;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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

