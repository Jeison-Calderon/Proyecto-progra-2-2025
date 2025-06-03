package aplicacion.domain;

import java.io.Serializable;

public class Hotel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigoHotel;
    private String nombre;
    private String ubicacion;

    public Hotel(String codigoHotel, String nombre, String ubicacion) {
        this.codigoHotel = codigoHotel;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
    }

    public String getCodigoHotel() {
        return codigoHotel;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    @Override
    public String toString() {
        return codigoHotel + " - " + nombre + " (" + ubicacion + ")";
    }
}