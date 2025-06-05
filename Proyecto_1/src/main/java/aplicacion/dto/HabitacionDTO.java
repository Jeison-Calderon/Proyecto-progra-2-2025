package aplicacion.dto;

public class HabitacionDTO {
    private String codigo;
    private String estilo;
    private double precio;
    private String codigoHotel;

    // Constructor vacío
    public HabitacionDTO() {}

    // Constructor
    public HabitacionDTO(String codigo, String estilo, double precio, String codigoHotel) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = codigoHotel;
    }

    // Constructor para compatibilidad
    public HabitacionDTO(String codigo, String estilo, double precio) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = "";
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
        return "HabitacionDTO{" +
                "codigo='" + codigo + '\'' +
                ", estilo='" + estilo + '\'' +
                ", precio=" + precio +
                ", codigoHotel='" + codigoHotel + '\'' +
                '}';
    }
}