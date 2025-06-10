package aplicacion.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;

    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
        this.fechaRegistro = LocalDateTime.now();
        this.ultimoAcceso = LocalDateTime.now();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }

    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    public boolean autenticar(String user, String pass) {
        if (this.username.equals(user) && this.password.equals(pass)) {
            this.actualizarUltimoAcceso();
            return true;
        }
        return false;
    }
}