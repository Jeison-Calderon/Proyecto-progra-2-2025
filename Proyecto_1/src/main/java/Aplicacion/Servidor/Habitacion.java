package Aplicacion.Servidor;

import java.io.Serializable;

public class Habitacion implements Serializable {
    private static final long serialVersionUID = 1L;
    private String codigo;
    private String estilo;
    private double precio;

    public Habitacion(String codigo, String estilo, double precio) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
    }

    public String getCodigo() { return codigo; }
    public String getEstilo() { return estilo; }
    public double getPrecio() { return precio; }
}

