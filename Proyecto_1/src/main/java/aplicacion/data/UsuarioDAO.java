package aplicacion.data;

import aplicacion.dto.Usuario;
import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class UsuarioDAO {
    private static final String RUTA_ARCHIVO = "usuarios.dat";

    public static ArrayList<Usuario> cargarUsuarios() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RUTA_ARCHIVO))) {
            return (ArrayList<Usuario>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("⚠️ Archivo de usuarios no encontrado - se creará uno nuevo");
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Error cargando usuarios: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void guardarUsuarios(ArrayList<Usuario> usuarios) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_ARCHIVO))) {
            oos.writeObject(usuarios);
            System.out.println("✅ Usuarios guardados correctamente");
        } catch (IOException e) {
            System.err.println("❌ Error guardando usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Usuario buscarPorCredenciales(String username, String password) {
        ArrayList<Usuario> usuarios = cargarUsuarios();
        Optional<Usuario> usuario = usuarios.stream()
                .filter(u -> u.autenticar(username, password))
                .findFirst();

        if (usuario.isPresent()) {
            guardarUsuarios(usuarios); // Guardar la actualización del último acceso
            return usuario.get();
        }
        return null;
    }

    public static boolean existeUsuario(String username) {
        return cargarUsuarios().stream()
                .anyMatch(u -> u.getUsername().equals(username));
    }
}