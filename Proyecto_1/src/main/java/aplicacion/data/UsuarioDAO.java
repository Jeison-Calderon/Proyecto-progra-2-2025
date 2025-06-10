package aplicacion.data;

import aplicacion.dto.Usuario;

import java.io.*;
import java.util.ArrayList;

public class UsuarioDAO {
    private static final String RUTA_ARCHIVO = "usuarios.dat";

    public static ArrayList<Usuario> cargarUsuarios() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RUTA_ARCHIVO))) {
            return (ArrayList<Usuario>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void guardarUsuarios(ArrayList<Usuario> usuarios) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_ARCHIVO))) {
            oos.writeObject(usuarios);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Usuario buscarPorCredenciales(String username, String password) {
        for (Usuario u : cargarUsuarios()) {
            if (u.autenticar(username, password)) return u;
        }
        return null;
    }
}
