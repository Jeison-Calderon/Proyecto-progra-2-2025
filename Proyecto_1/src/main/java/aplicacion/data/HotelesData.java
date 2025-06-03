package aplicacion.data;

import aplicacion.domain.Hotel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HotelesData {
    private static final String ARCHIVO_HOTELES = "hoteles.dat";

    public static synchronized String generarCodigo() {
        int ultimo = obtenerUltimoCodigo();
        return String.format("H-%03d", ultimo + 1);
    }

    private static int obtenerUltimoCodigo() {
        List<Hotel> hoteles = listar();
        if (hoteles.isEmpty()) return 0;

        String ultimoCodigo = hoteles.get(hoteles.size() - 1).getCodigoHotel();
        return Integer.parseInt(ultimoCodigo.split("-")[1]);
    }

    public static synchronized String guardar(String nombre, String ubicacion) {
        List<Hotel> hoteles = listar();

        for (Hotel h : hoteles) {
            if (h.getNombre().equalsIgnoreCase(nombre) &&
                    h.getUbicacion().equalsIgnoreCase(ubicacion)) {
                return "duplicado";
            }
        }

        String codigo = generarCodigo();
        Hotel nuevo = new Hotel(codigo, nombre, ubicacion);
        hoteles.add(nuevo);
        sobrescribirArchivo(hoteles);
        return codigo;
    }

    public static List<Hotel> listar() {
        List<Hotel> hoteles = new ArrayList<>();
        try (
                FileInputStream fis = new FileInputStream(ARCHIVO_HOTELES);
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            while (true) {
                try {
                    Hotel hotel = (Hotel) ois.readObject();
                    hoteles.add(hotel);
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
        }
        return hoteles;
    }

    public static synchronized boolean eliminar(String codigo) {
        List<Hotel> hoteles = listar();
        boolean encontrado = hoteles.removeIf(h -> h.getCodigoHotel().equals(codigo));
        if (encontrado) {
            sobrescribirArchivo(hoteles);
        }
        return encontrado;
    }

    public static synchronized boolean modificar(Hotel hotelModificado) {
        List<Hotel> hoteles = listar();
        boolean encontrado = false;
        for (int i = 0; i < hoteles.size(); i++) {
            if (hoteles.get(i).getCodigoHotel().equals(hotelModificado.getCodigoHotel())) {
                hoteles.set(i, hotelModificado);
                encontrado = true;
                break;
            }
        }
        if (encontrado) {
            sobrescribirArchivo(hoteles);
        }
        return encontrado;
    }
    public static synchronized Hotel buscar(String codigo) {
        List<Hotel> hoteles = listar();
        for (Hotel hotel : hoteles) {
            if (hotel.getCodigoHotel().equalsIgnoreCase(codigo)) {
                return hotel;
            }
        }
        return null;
    }

    private static void sobrescribirArchivo(List<Hotel> hoteles) {
        try (
                FileOutputStream fos = new FileOutputStream(ARCHIVO_HOTELES);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            for (Hotel h : hoteles) {
                oos.writeObject(h);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}