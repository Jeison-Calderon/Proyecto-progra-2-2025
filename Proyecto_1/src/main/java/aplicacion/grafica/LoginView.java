package aplicacion.grafica;

import aplicacion.cliente.ClienteSocket;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.util.function.Consumer;

public class LoginView {

    public void mostrar(Stage stage, Consumer<String> onLoginExitoso) {
        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuario");

        PasswordField txtContrasena = new PasswordField();
        txtContrasena.setPromptText("Contraseña");

        Button btnLogin = new Button("Iniciar sesión");

        btnLogin.setOnAction(e -> {
            String usuario = txtUsuario.getText().trim();
            String pass = txtContrasena.getText().trim();

            if (usuario.isEmpty() || pass.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Ingrese usuario y contraseña.").showAndWait();
                return;
            }

            try {
                JSONObject creds = new JSONObject();
                creds.put("usuario", usuario);
                creds.put("contrasena", pass);

                String respuestaJson = new ClienteSocket()
                        .enviarOperacion("LOGIN", creds.toString());
                JSONObject respuesta = new JSONObject(respuestaJson);

                if ("OK".equals(respuesta.getString("estado"))) {
                    onLoginExitoso.accept(usuario);
                } else {
                    String msg = respuesta.optString("mensaje", "Credenciales inválidas");
                    new Alert(Alert.AlertType.ERROR, msg).showAndWait();
                }

            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR,
                        "Error de conexión:\n" + ex.getMessage()
                ).showAndWait();
            }
        });

        VBox root = new VBox(10, txtUsuario, txtContrasena, btnLogin);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-alignment: center;");

        Scene escenaLogin = new Scene(root, 350, 200);
        stage.setScene(escenaLogin);
        stage.setTitle("Login");
        stage.show();
    }
}
