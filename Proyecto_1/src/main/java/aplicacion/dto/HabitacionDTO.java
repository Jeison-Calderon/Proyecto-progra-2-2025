package aplicacion.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HabitacionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String estilo;
    private double precio;
    private String codigoHotel;

    private String estado;
    private String numero;
    private List<String> imagenes;

    public HabitacionDTO() {
        this.imagenes = new ArrayList<>();
        this.estado = "disponible";
    }

    public HabitacionDTO(String codigo, String estilo, double precio, String codigoHotel) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = codigoHotel;
        this.imagenes = new ArrayList<>();
        this.estado = "disponible";
        this.numero = "";
    }

    public HabitacionDTO(String codigo, String estilo, double precio, String codigoHotel,
                         String numero, String estado) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = codigoHotel;
        this.numero = numero;
        this.estado = estado;
        this.imagenes = new ArrayList<>();
    }

    public HabitacionDTO(String codigo, String estilo, double precio, String codigoHotel,
                         String numero, String estado, List<String> imagenes) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = codigoHotel;
        this.numero = numero;
        this.estado = estado;
        this.imagenes = imagenes != null ? new ArrayList<>(imagenes) : new ArrayList<>();
    }

    public HabitacionDTO(String codigo, String estilo, double precio) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = "";
        this.imagenes = new ArrayList<>();
        this.estado = "disponible";
        this.numero = "";
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getEstilo() { return estilo; }
    public void setEstilo(String estilo) { this.estilo = estilo; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getCodigoHotel() { return codigoHotel; }
    public void setCodigoHotel(String codigoHotel) { this.codigoHotel = codigoHotel; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public List<String> getImagenes() { return new ArrayList<>(imagenes); }
    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes != null ? new ArrayList<>(imagenes) : new ArrayList<>();
    }

    public void agregarImagen(String rutaImagen) {
        if (rutaImagen != null && !rutaImagen.trim().isEmpty()) {
            this.imagenes.add(rutaImagen);
        }
    }

    public void eliminarImagen(String rutaImagen) {
        this.imagenes.remove(rutaImagen);
    }

    public boolean tieneImagenes() {
        return !this.imagenes.isEmpty();
    }

    public String getPrimeraImagen() {
        return this.imagenes.isEmpty() ? null : this.imagenes.get(0);
    }

    public boolean estaDisponible() {
        return "disponible".equalsIgnoreCase(this.estado);
    }

    public boolean estaOcupada() {
        return "ocupada".equalsIgnoreCase(this.estado);
    }

    public boolean estaEnMantenimiento() {
        return "mantenimiento".equalsIgnoreCase(this.estado);
    }

    @Override
    public String toString() {
        return "HabitacionDTO{" +
                "codigo='" + codigo + '\'' +
                ", estilo='" + estilo + '\'' +
                ", precio=" + precio +
                ", codigoHotel='" + codigoHotel + '\'' +
                ", numero='" + numero + '\'' +
                ", estado='" + estado + '\'' +
                ", imagenes=" + imagenes.size() + " imagen(es)" +
                '}';
    }

    public String toStringCompleto() {
        return "HabitacionDTO{" +
                "codigo='" + codigo + '\'' +
                ", numero='" + numero + '\'' +
                ", estilo='" + estilo + '\'' +
                ", precio=" + precio +
                ", estado='" + estado + '\'' +
                ", codigoHotel='" + codigoHotel + '\'' +
                ", imagenes=" + imagenes +
                '}';
    }
}
