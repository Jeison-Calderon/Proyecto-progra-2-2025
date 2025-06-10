package aplicacion.data;

import aplicacion.dto.HabitacionDTO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HabitacionesData {
    private static final String ARCHIVO = "habitaciones.dat";

    public static synchronized String guardar(String estilo, double precio, String codigoHotel) {
        List<HabitacionDTO> habitaciones = listar();

        for (HabitacionDTO h : habitaciones) {
            if (h.getCodigoHotel().equals(codigoHotel)
                    && h.getEstilo().equalsIgnoreCase(estilo)
                    && h.getPrecio() == precio) {
                return "duplicado";
            }
        }

        String codigo = generarCodigoHabitacion(habitaciones, codigoHotel);

        habitaciones.add(new HabitacionDTO(codigo, estilo, precio, codigoHotel));
        sobrescribirArchivo(habitaciones);
        return codigo;
    }

    private static String generarCodigoHabitacion(List<HabitacionDTO> habitaciones, String codigoHotel) {
        int maxNumero = 0;
        String prefijo = codigoHotel.replace("H-", "R") + "-";  // H-001 → R001-

        for (HabitacionDTO h : habitaciones) {
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

    public static List<HabitacionDTO> listar() {
        List<HabitacionDTO> list = new ArrayList<>();
        try (
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO))
        ) {
            while (true) {
                list.add((HabitacionDTO) ois.readObject());
            }
        } catch (EOFException ignored) {
        } catch (FileNotFoundException e) {
            // El archivo no existe aún, retornar lista vacía
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static synchronized boolean modificar(HabitacionDTO habitacionModificada) {
        List<HabitacionDTO> habitaciones = listar();
        boolean encontrada = false;

        for (int i = 0; i < habitaciones.size(); i++) {
            HabitacionDTO h = habitaciones.get(i);
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
        List<HabitacionDTO> habitaciones = listar();
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

    private static void sobrescribirArchivo(List<HabitacionDTO> habitaciones) {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO))
        ) {
            for (HabitacionDTO h : habitaciones) {
                oos.writeObject(h);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}