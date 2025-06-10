package aplicacion.dto;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String username;
    private String password;

    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public boolean autenticar(String user, String pass) {
        return this.username.equals(user) && this.password.equals(pass);
    }
}
