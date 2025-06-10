package aplicacion.grafica;

import aplicacion.cliente.ClienteSocket;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.util.function.Consumer;

public class LoginView {

    public void mostrar(Stage stage, Consumer<String> onLoginExitoso) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");
        root.setAlignment(Pos.CENTER);

        // Título
        Label titulo = new Label("Bienvenido al Sistema");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Campos de login
        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuario");
        txtUsuario.setMaxWidth(300);
        txtUsuario.setStyle("-fx-padding: 10px;");

        PasswordField txtContrasena = new PasswordField();
        txtContrasena.setPromptText("Contraseña");
        txtContrasena.setMaxWidth(300);
        txtContrasena.setStyle("-fx-padding: 10px;");

        // Botones
        Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 10 20;");
        btnLogin.setMaxWidth(300);

        Button btnRegistro = new Button("Crear cuenta nueva");
        btnRegistro.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 20;");
        btnRegistro.setMaxWidth(300);

        // Acción del botón de login
        btnLogin.setOnAction(e -> realizarLogin(txtUsuario.getText(), txtContrasena.getText(), onLoginExitoso));

        // Acción del botón de registro
        btnRegistro.setOnAction(e -> mostrarFormularioRegistro(stage));

        root.getChildren().addAll(titulo, txtUsuario, txtContrasena, btnLogin, btnRegistro);

        Scene escenaLogin = new Scene(root, 400, 300);
        stage.setScene(escenaLogin);
        stage.setTitle("Login");
        stage.show();
    }

    private void realizarLogin(String usuario, String pass, Consumer<String> onLoginExitoso) {
        if (usuario.isEmpty() || pass.isEmpty()) {
            mostrarError("Ingrese usuario y contraseña.");
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
                mostrarError(respuesta.optString("mensaje", "Credenciales inválidas"));
            }

        } catch (IOException ex) {
            mostrarError("Error de conexión:\n" + ex.getMessage());
        }
    }

    private void mostrarFormularioRegistro(Stage ownerStage) {
        Stage ventanaRegistro = new Stage();
        ventanaRegistro.initOwner(ownerStage);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("Crear Nueva Cuenta");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField txtNuevoUsuario = new TextField();
        txtNuevoUsuario.setPromptText("Usuario");
        txtNuevoUsuario.setMaxWidth(300);

        PasswordField txtNuevaContrasena = new PasswordField();
        txtNuevaContrasena.setPromptText("Contraseña");
        txtNuevaContrasena.setMaxWidth(300);

        PasswordField txtConfirmarContrasena = new PasswordField();
        txtConfirmarContrasena.setPromptText("Confirmar Contraseña");
        txtConfirmarContrasena.setMaxWidth(300);

        Button btnCrearCuenta = new Button("Crear Cuenta");
        btnCrearCuenta.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 20;");
        btnCrearCuenta.setMaxWidth(300);

        btnCrearCuenta.setOnAction(e -> {
            try {
                JSONObject datos = new JSONObject();
                datos.put("usuario", txtNuevoUsuario.getText());
                datos.put("contrasena", txtNuevaContrasena.getText());
                datos.put("confirmarContrasena", txtConfirmarContrasena.getText());

                // Aquí es importante que la operación sea exactamente "REGISTRAR_USUARIO"
                String respuestaJson = new ClienteSocket()
                        .enviarOperacion("REGISTRAR_USUARIO", datos.toString());
                JSONObject respuesta = new JSONObject(respuestaJson);

                if ("OK".equals(respuesta.getString("estado"))) {
                    mostrarInfo("Registro exitoso",
                            "¡Usuario creado correctamente!\nAhora puede iniciar sesión.");
                    ventanaRegistro.close();
                } else {
                    mostrarError(respuesta.getString("mensaje"));
                }

            } catch (IOException ex) {
                mostrarError("Error de conexión:\n" + ex.getMessage());
            }
        });

        root.getChildren().addAll(
                titulo,
                txtNuevoUsuario,
                txtNuevaContrasena,
                txtConfirmarContrasena,
                btnCrearCuenta
        );

        Scene scene = new Scene(root, 400, 300);
        ventanaRegistro.setScene(scene);
        ventanaRegistro.setTitle("Registro de Usuario");
        ventanaRegistro.show();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}