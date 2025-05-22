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
        } catch (FileNotFoundException e) {
            // El archivo no existe aún, retornar lista vacía
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Nuevo método para modificar habitación
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

    // Nuevo método para eliminar habitación
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