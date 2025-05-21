package Aplicacion.Servidor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class GestorHoteles {
    private static final String ARCHIVO_HOTELES = "hoteles.dat";

    public static synchronized String generarCodigo() {
        int ultimo = obtenerUltimoCodigo();
        return String.format("H-%03d", ultimo + 1);
    }

    private static int obtenerUltimoCodigo() {
        List<Hotel> hoteles = listar();
        if (hoteles.isEmpty()) return 0;

        String ultimoCodigo = hoteles.get(hoteles.size() - 1).getCodigoHotel(); // H-001
        return Integer.parseInt(ultimoCodigo.split("-")[1]);
    }

    public static synchronized void guardar(Hotel hotel) {
        try (
                FileOutputStream fos = new FileOutputStream(ARCHIVO_HOTELES, true);
                MiObjectOutputStream oos = existeArchivo() ? new MiObjectOutputStream(fos) : new MiObjectOutputStream(fos, false)
        ) {
            oos.writeObject(hotel);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            // Si no existe el archivo, simplemente devolvemos la lista vacía
        }
        return hoteles;
    }

    private static boolean existeArchivo() {
        return new File(ARCHIVO_HOTELES).exists();
    }

    // Clase interna para evitar el encabezado al escribir más objetos
    private static class MiObjectOutputStream extends ObjectOutputStream {
        public MiObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        public MiObjectOutputStream(OutputStream out, boolean append) throws IOException {
            super(out);
            if (append) {
                reset();
            }
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            if (existeArchivo()) {
                reset(); // No escribir encabezado si ya existe el archivo
            } else {
                super.writeStreamHeader();
            }
        }
    }
}
