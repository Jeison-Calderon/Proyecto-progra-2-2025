package aplicacion.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HabitacionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // ✅ Campos existentes
    private String codigo;
    private String estilo;
    private double precio;
    private String codigoHotel;

    // ✅ NUEVOS campos para disponibilidad
    private String estado;              // disponible, ocupada, mantenimiento
    private String numero;              // número de habitación (ej: 101, 102, 201)
    private List<String> imagenes;      // paths/URLs de imágenes de la habitación

    // ✅ Constructor vacío
    public HabitacionDTO() {
        this.imagenes = new ArrayList<>();
        this.estado = "disponible"; // Estado por defecto
    }

    // ✅ Constructor original (mantiene compatibilidad)
    public HabitacionDTO(String codigo, String estilo, double precio, String codigoHotel) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = codigoHotel;
        this.imagenes = new ArrayList<>();
        this.estado = "disponible"; // Estado por defecto
        this.numero = ""; // Se puede asignar después
    }

    // ✅ Constructor extendido NUEVO
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

    // ✅ Constructor completo NUEVO
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

    // ✅ Constructor para compatibilidad (mantiene funcionalidad existente)
    public HabitacionDTO(String codigo, String estilo, double precio) {
        this.codigo = codigo;
        this.estilo = estilo;
        this.precio = precio;
        this.codigoHotel = "";
        this.imagenes = new ArrayList<>();
        this.estado = "disponible";
        this.numero = "";
    }

    // ✅ Getters y Setters EXISTENTES
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getEstilo() { return estilo; }
    public void setEstilo(String estilo) { this.estilo = estilo; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getCodigoHotel() { return codigoHotel; }
    public void setCodigoHotel(String codigoHotel) { this.codigoHotel = codigoHotel; }

    // ✅ NUEVOS Getters y Setters
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public List<String> getImagenes() { return new ArrayList<>(imagenes); }
    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes != null ? new ArrayList<>(imagenes) : new ArrayList<>();
    }

    // ✅ MÉTODOS AUXILIARES para manejo de imágenes
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

    // ✅ MÉTODOS AUXILIARES para estado
    public boolean estaDisponible() {
        return "disponible".equalsIgnoreCase(this.estado);
    }

    public boolean estaOcupada() {
        return "ocupada".equalsIgnoreCase(this.estado);
    }

    public boolean estaEnMantenimiento() {
        return "mantenimiento".equalsIgnoreCase(this.estado);
    }

    // ✅ ToString actualizado
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

    // ✅ MÉTODO para mostrar información completa
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