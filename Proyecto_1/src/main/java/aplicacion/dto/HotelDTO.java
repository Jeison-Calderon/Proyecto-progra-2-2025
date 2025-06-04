package aplicacion.dto;

import java.io.Serializable;

/**
 * Clase DTO que representa los datos de un Hotel
 */
public class HotelDTO implements Serializable {
    private String codigo;
    private String nombre;
    private String ubicacion;

    // Constructor vacío
    public HotelDTO() {
    }

    // Constructor con parámetros
    public HotelDTO(String codigo, String nombre, String ubicacion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
    }

    // Getters y setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    @Override
    public String toString() {
        return "HotelDTO{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                '}';
    }
}