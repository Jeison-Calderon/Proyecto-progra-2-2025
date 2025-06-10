package aplicacion.util;

import org.json.JSONObject;
import aplicacion.data.UsuarioDAO;
import aplicacion.dto.Usuario;
import aplicacion.dto.Usuario.TipoUsuario;
import java.util.ArrayList;

public class ControladorLogin {

    public String procesar(String datosJson) {
        JSONObject respuesta = new JSONObject();

        try {
            JSONObject datos = new JSONObject(datosJson);
            String usuario = datos.optString("usuario", "");
            String contrasena = datos.optString("contrasena", "");
            String tipoStr = datos.optString("tipo", "");

            // Validar que se proporcionó el tipo de usuario
            if (tipoStr.isEmpty()) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Debe especificar el tipo de usuario");
                return respuesta.toString();
            }

            // Validar el tipo de usuario
            TipoUsuario tipoSolicitado;
            try {
                tipoSolicitado = TipoUsuario.valueOf(tipoStr);
            } catch (IllegalArgumentException e) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Tipo de usuario inválido");
                return respuesta.toString();
            }

            Usuario usuarioEncontrado = UsuarioDAO.buscarPorCredenciales(usuario, contrasena);

            if (usuarioEncontrado != null) {
                // Verificar que el tipo de usuario coincida
                if (usuarioEncontrado.getTipo() == tipoSolicitado) {
                    respuesta.put("estado", "OK");
                    respuesta.put("mensaje", "Login exitoso");
                    respuesta.put("tipo", usuarioEncontrado.getTipo().toString());
                    respuesta.put("usuario", usuarioEncontrado.getUsername());
                } else {
                    respuesta.put("estado", "ERROR");
                    respuesta.put("mensaje", "Tipo de usuario incorrecto");
                }
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
            String tipoStr = datos.getString("tipo");

            // Validaciones básicas
            if (usuario.isEmpty() || contrasena.isEmpty()) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Usuario y contraseña son requeridos");
                return respuesta.toString();
            }

            if (usuario.length() < 4) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "El nombre de usuario debe tener al menos 4 caracteres");
                return respuesta.toString();
            }

            if (contrasena.length() < 6) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "La contraseña debe tener al menos 6 caracteres");
                return respuesta.toString();
            }

            if (!contrasena.equals(confirmarContrasena)) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Las contraseñas no coinciden");
                return respuesta.toString();
            }

            // Validar y convertir el tipo de usuario
            TipoUsuario tipo;
            try {
                tipo = TipoUsuario.valueOf(tipoStr);
            } catch (IllegalArgumentException e) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "Tipo de usuario inválido");
                return respuesta.toString();
            }

            // Verificar si el usuario ya existe
            ArrayList<Usuario> usuarios = UsuarioDAO.cargarUsuarios();
            if (usuarios.stream().anyMatch(u -> u.getUsername().equals(usuario))) {
                respuesta.put("estado", "ERROR");
                respuesta.put("mensaje", "El nombre de usuario ya existe");
                return respuesta.toString();
            }

            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario(usuario, contrasena, tipo);
            usuarios.add(nuevoUsuario);
            UsuarioDAO.guardarUsuarios(usuarios);

            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Usuario registrado exitosamente");
            respuesta.put("tipo", tipo.toString());

        } catch (Exception e) {
            respuesta.put("estado", "ERROR");
            respuesta.put("mensaje", "Error registrando usuario: " + e.getMessage());
        }

        return respuesta.toString();
    }
}