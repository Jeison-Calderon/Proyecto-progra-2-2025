package aplicacion.dto;

import java.io.Serializable;
import java.util.List;

public class RespuestaDTO<T> implements Serializable {
    private String estado;
    private String mensaje;
    private T datos;

    public RespuestaDTO() {
    }

    public RespuestaDTO(String estado, String mensaje) {
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public RespuestaDTO(String estado, String mensaje, T datos) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getDatos() {
        return datos;
    }

    public void setDatos(T datos) {
        this.datos = datos;
    }

    @Override
    public String toString() {
        return "RespuestaDTO{" +
                "estado='" + estado + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", datos=" + datos +
                '}';
    }
}