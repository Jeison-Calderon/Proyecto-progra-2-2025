package aplicacion.grafica;

import aplicacion.cliente.ClienteSocket;
import aplicacion.dto.Usuario.TipoUsuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.util.function.BiConsumer;

public class LoginView {

    public void mostrar(Stage stage, BiConsumer<String, TipoUsuario> onLoginExitoso) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");
        root.setAlignment(Pos.CENTER);

        // Título
        Label titulo = new Label("Sistema Hotelero");
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

        // ComboBox para tipo de usuario
        ComboBox<String> comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll("Huésped", "Recepcionista");
        comboTipo.setPromptText("Seleccione tipo de usuario");
        comboTipo.setMaxWidth(300);
        comboTipo.setStyle("-fx-padding: 10px;");

        // Botones
        Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 10 20;");
        btnLogin.setMaxWidth(300);

        Button btnRegistro = new Button("Crear cuenta nueva");
        btnRegistro.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 20;");
        btnRegistro.setMaxWidth(300);

        // Acción del botón de login
        btnLogin.setOnAction(e -> {
            if (comboTipo.getValue() == null) {
                mostrarError("Seleccione el tipo de usuario");
                return;
            }

            TipoUsuario tipo = comboTipo.getValue().equals("Huésped") ?
                    TipoUsuario.HUESPED : TipoUsuario.RECEPCIONISTA;

            realizarLogin(txtUsuario.getText(), txtContrasena.getText(), tipo, onLoginExitoso);
        });

        // Acción del botón de registro
        btnRegistro.setOnAction(e -> mostrarFormularioRegistro(stage));

        root.getChildren().addAll(titulo, txtUsuario, txtContrasena, comboTipo, btnLogin, btnRegistro);

        Scene escenaLogin = new Scene(root, 400, 350);
        stage.setScene(escenaLogin);
        stage.setTitle("Login - Sistema Hotelero");
        stage.show();
    }

    private void realizarLogin(String usuario, String pass, TipoUsuario tipo,
                               BiConsumer<String, TipoUsuario> onLoginExitoso) {
        if (usuario.isEmpty() || pass.isEmpty()) {
            mostrarError("Ingrese usuario y contraseña.");
            return;
        }

        try {
            JSONObject creds = new JSONObject();
            creds.put("usuario", usuario);
            creds.put("contrasena", pass);
            creds.put("tipo", tipo.toString());

            String respuestaJson = new ClienteSocket()
                    .enviarOperacion("LOGIN", creds.toString());
            JSONObject respuesta = new JSONObject(respuestaJson);

            if ("OK".equals(respuesta.getString("estado"))) {
                onLoginExitoso.accept(usuario, tipo);
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

        // ComboBox para tipo de usuario en registro
        ComboBox<String> comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll("Huésped", "Recepcionista");
        comboTipo.setPromptText("Seleccione tipo de usuario");
        comboTipo.setMaxWidth(300);

        Button btnCrearCuenta = new Button("Crear Cuenta");
        btnCrearCuenta.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 20;");
        btnCrearCuenta.setMaxWidth(300);

        btnCrearCuenta.setOnAction(e -> {
            if (comboTipo.getValue() == null) {
                mostrarError("Seleccione el tipo de usuario");
                return;
            }

            try {
                JSONObject datos = new JSONObject();
                datos.put("usuario", txtNuevoUsuario.getText());
                datos.put("contrasena", txtNuevaContrasena.getText());
                datos.put("confirmarContrasena", txtConfirmarContrasena.getText());
                datos.put("tipo", comboTipo.getValue().equals("Huésped") ?
                        "HUESPED" : "RECEPCIONISTA");

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
                comboTipo,
                btnCrearCuenta
        );

        Scene scene = new Scene(root, 400, 350);
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