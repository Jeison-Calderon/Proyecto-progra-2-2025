package aplicacion.util;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ControladorLogin {

    // Usuarios de ejemplo (usuario: contraseña)
    private static final Map<String, String> USUARIOS = new HashMap<>();

    static {
        USUARIOS.put("admin", "1234");
        USUARIOS.put("usuario", "pass");
    }

    /**
     * Procesa una solicitud de login.
     * @param datosJson JSON con usuario y contraseña
     * @return JSON con el resultado
     */
    public String procesar(String datosJson) {
        JSONObject respuesta = new JSONObject();

        try {
            JSONObject datos = new JSONObject(datosJson);
            String usuario = datos.optString("usuario", "");
            String contrasena = datos.optString("contrasena", "");

            if (USUARIOS.containsKey(usuario) && USUARIOS.get(usuario).equals(contrasena)) {
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
}
