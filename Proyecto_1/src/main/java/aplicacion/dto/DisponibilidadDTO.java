package aplicacion.dto;

public class DisponibilidadDTO {

    private String codigoHabitacion;
    private String numeroHabitacion;
    private String estilo;
    private double precio;
    private String nombreHotel;
    private String codigoHotel;
    private int cantidadImagenes;
    private String estado;

    public DisponibilidadDTO() {}

    public DisponibilidadDTO(String codigoHabitacion, String numeroHabitacion, String estilo,
                             double precio, String nombreHotel, String codigoHotel,
                             int cantidadImagenes, String estado) {
        this.codigoHabitacion = codigoHabitacion;
        this.numeroHabitacion = numeroHabitacion;
        this.estilo = estilo;
        this.precio = precio;
        this.nombreHotel = nombreHotel;
        this.codigoHotel = codigoHotel;
        this.cantidadImagenes = cantidadImagenes;
        this.estado = estado;
    }

    public String getCodigoHabitacion() {
        return codigoHabitacion;
    }

    public void setCodigoHabitacion(String codigoHabitacion) {
        this.codigoHabitacion = codigoHabitacion;
    }

    public String getNumeroHabitacion() {
        return numeroHabitacion;
    }

    public void setNumeroHabitacion(String numeroHabitacion) {
        this.numeroHabitacion = numeroHabitacion;
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

    public String getNombreHotel() {
        return nombreHotel;
    }

    public void setNombreHotel(String nombreHotel) {
        this.nombreHotel = nombreHotel;
    }

    public String getCodigoHotel() {
        return codigoHotel;
    }

    public void setCodigoHotel(String codigoHotel) {
        this.codigoHotel = codigoHotel;
    }

    public int getCantidadImagenes() {
        return cantidadImagenes;
    }

    public void setCantidadImagenes(int cantidadImagenes) {
        this.cantidadImagenes = cantidadImagenes;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "DisponibilidadDTO{" +
                "codigoHabitacion='" + codigoHabitacion + '\'' +
                ", numeroHabitacion='" + numeroHabitacion + '\'' +
                ", estilo='" + estilo + '\'' +
                ", precio=" + precio +
                ", nombreHotel='" + nombreHotel + '\'' +
                ", codigoHotel='" + codigoHotel + '\'' +
                ", cantidadImagenes=" + cantidadImagenes +
                ", estado='" + estado + '\'' +
                '}';
    }
}