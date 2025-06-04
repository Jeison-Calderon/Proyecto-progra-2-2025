package aplicacion.dto;

import java.io.Serializable;

/**
 * Clase DTO que representa los datos de una Habitación
 */
public class HabitacionDTO implements Serializable {
    private String codigo;
    private String estilo;
    private double precio;

    // Constructor vacío
    public HabitacionDTO() {
    }

    // Constructor con parámetros
    public HabitacionDTO(String codigo, String estilo, double precio) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
    }

    // Getters y setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getEstilo() {
        return estilo;
    }

    public void setEstilo(String estilo) {
        this.estilo = estilo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "HabitacionDTO{" +
                "codigo='" + codigo + '\'' +
                ", estilo='" + estilo + '\'' +
                ", precio=" + precio +
                '}';
    }
}