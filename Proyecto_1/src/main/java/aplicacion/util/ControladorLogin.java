package aplicacion.util;

import org.json.JSONObject;
import aplicacion.data.UsuarioDAO;
import aplicacion.dto.Usuario;
import java.util.ArrayList;

public class ControladorLogin {

    public String procesar(String datosJson) {
        JSONObject respuesta = new JSONObject();

        try {
            JSONObject datos = new JSONObject(datosJson);
            String usuario = datos.optString("usuario", "");
            String contrasena = datos.optString("contrasena", "");

            Usuario usuarioEncontrado = UsuarioDAO.buscarPorCredenciales(usuario, contrasena);

            if (usuarioEncontrado != null) {
                respuesta.put("estado", "OK");
                respuesta.put("mensaje", "Login exitoso");
            } else {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Usuario o contraseña incorrectos");
            }

        } catch (Exception e) {
            respuesta.put("estado", "ERROR");
            respuesta.put("mensaje", "Error procesando login: " + e.getMessage());
        }

        return respuesta.toString();
    }

    public String registrarUsuario(String datosJson) {
        JSONObject respuesta = new JSONObject();

        try {
            JSONObject datos = new JSONObject(datosJson);
            String usuario = datos.getString("usuario");
            String contrasena = datos.getString("contrasena");
            String confirmarContrasena = datos.getString("confirmarContrasena");

            // Validaciones
            if (usuario.isEmpty() || contrasena.isEmpty()) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Usuario y contraseña son requeridos");
                return respuesta.toString();
            }

            if (!contrasena.equals(confirmarContrasena)) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Las contraseñas no coinciden");
                return respuesta.toString();
            }

            // Verificar si el usuario ya existe
            ArrayList<Usuario> usuarios = UsuarioDAO.cargarUsuarios();
            for (Usuario u : usuarios) {
                if (u.getUsername().equals(usuario)) {
                    respuesta.put("estado", "ERROR");
                    respuesta.put("mensaje", "El nombre de usuario ya existe");
                    return respuesta.toString();
                }
            }

            // Crear nuevo usuario
            usuarios.add(new Usuario(usuario, contrasena));
            UsuarioDAO.guardarUsuarios(usuarios);

            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Usuario registrado exitosamente");

        } catch (Exception e) {
            respuesta.put("estado", "ERROR");
            respuesta.put("mensaje", "Error registrando usuario: " + e.getMessage());
        }

        return respuesta.toString();
    }
}