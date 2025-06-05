package aplicacion.domain;

import java.io.Serializable;

public class Habitacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String estilo;
    private double precio;
    private String codigoHotel;

    // Constructor vacío
    public Habitacion() {}

    // Constructor con todos los campos (ACTUALIZADO)
    public Habitacion(String codigo, String estilo, double precio, String codigoHotel) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = codigoHotel;
    }

    // Constructor para compatibilidad (SIN codigoHotel)
    public Habitacion(String codigo, String estilo, double precio) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = ""; // Valor por defecto
    }

    // Getters y Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getEstilo() { return estilo; }
    public void setEstilo(String estilo) { this.estilo = estilo; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }


    public String getCodigoHotel() { return codigoHotel; }
    public void setCodigoHotel(String codigoHotel) { this.codigoHotel = codigoHotel; }

    @Override
    public String toString() {
        return "Habitacion{" +
                "codigo='" + codigo + '\'' +
                ", estilo='" + estilo + '\'' +
                ", precio=" + precio +
                ", codigoHotel='" + codigoHotel + '\'' +
                '}';
    }
}