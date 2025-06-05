package aplicacion.data;

import aplicacion.dto.HotelDTO;

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
        List<HotelDTO> hoteles = listar();
        if (hoteles.isEmpty()) return 0;

        String ultimoCodigo = hoteles.get(hoteles.size() - 1).getCodigo();
        return Integer.parseInt(ultimoCodigo.split("-")[1]);
    }

    public static synchronized String guardar(String nombre, String ubicacion) {
        List<HotelDTO> hoteles = listar();

        for (HotelDTO h : hoteles) {
            if (h.getNombre().equalsIgnoreCase(nombre) &&
                    h.getUbicacion().equalsIgnoreCase(ubicacion)) {
                return "duplicado";
            }
        }

        String codigo = generarCodigo();
        HotelDTO nuevo = new HotelDTO(codigo, nombre, ubicacion);
        hoteles.add(nuevo);
        sobrescribirArchivo(hoteles);
        return codigo;
    }

    public static List<HotelDTO> listar() {
        List<HotelDTO> hoteles = new ArrayList<>();
        try (
                FileInputStream fis = new FileInputStream(ARCHIVO_HOTELES);
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            while (true) {
                try {
                    HotelDTO hotel = (HotelDTO) ois.readObject();
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
        List<HotelDTO> hoteles = listar();
        boolean encontrado = hoteles.removeIf(h -> h.getCodigo().equals(codigo));
        if (encontrado) {
            sobrescribirArchivo(hoteles);
        }
        return encontrado;
    }

    public static synchronized boolean modificar(HotelDTO hotelModificado) {
        List<HotelDTO> hoteles = listar();
        boolean encontrado = false;
        for (int i = 0; i < hoteles.size(); i++) {
            if (hoteles.get(i).getCodigo().equals(hotelModificado.getCodigo())) {
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

    public static synchronized HotelDTO buscar(String codigo) {
        List<HotelDTO> hoteles = listar();
        for (HotelDTO hotel : hoteles) {
            if (hotel.getCodigo().equalsIgnoreCase(codigo)) {
                return hotel;
            }
        }
        return null;
    }

    private static void sobrescribirArchivo(List<HotelDTO> hoteles) {
        try (
                FileOutputStream fos = new FileOutputStream(ARCHIVO_HOTELES);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            for (HotelDTO h : hoteles) {
                oos.writeObject(h);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}