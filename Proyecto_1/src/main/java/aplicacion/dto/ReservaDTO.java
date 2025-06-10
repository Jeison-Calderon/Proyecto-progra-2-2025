package aplicacion.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ReservaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String codigoHabitacion;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String estado;

    private String codigoHotel;
    private String clienteNombre;
    private double precioTotal;
    private LocalDate fechaCreacion;

    public static final String ESTADO_ACTIVA = "activa";
    public static final String ESTADO_CANCELADA = "cancelada";
    public static final String ESTADO_FINALIZADA = "finalizada";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReservaDTO() {
        this.fechaCreacion = LocalDate.now();
        this.estado = ESTADO_ACTIVA;
        this.precioTotal = 0.0;
    }

    public ReservaDTO(String codigo, String codigoHabitacion, LocalDate fechaDesde, LocalDate fechaHasta) {
        this();
        this.codigo = codigo;
        this.codigoHabitacion = codigoHabitacion;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public ReservaDTO(String codigo, String codigoHabitacion, String codigoHotel,
                      LocalDate fechaDesde, LocalDate fechaHasta, String estado,
                      String clienteNombre, double precioTotal) {
        this.codigo = codigo;
        this.codigoHabitacion = codigoHabitacion;
        this.codigoHotel = codigoHotel;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.estado = estado != null ? estado : ESTADO_ACTIVA;
        this.clienteNombre = clienteNombre;
        this.precioTotal = precioTotal;
        this.fechaCreacion = LocalDate.now();
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getCodigoHabitacion() { return codigoHabitacion; }
    public void setCodigoHabitacion(String codigoHabitacion) { this.codigoHabitacion = codigoHabitacion; }

    public String getCodigoHotel() { return codigoHotel; }
    public void setCodigoHotel(String codigoHotel) { this.codigoHotel = codigoHotel; }

    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }

    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(double precioTotal) { this.precioTotal = precioTotal; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean estaActiva() {
        return ESTADO_ACTIVA.equalsIgnoreCase(this.estado);
    }

    public boolean estaCancelada() {
        return ESTADO_CANCELADA.equalsIgnoreCase(this.estado);
    }

    public boolean estaFinalizada() {
        return ESTADO_FINALIZADA.equalsIgnoreCase(this.estado);
    }

    public void activar() {
        this.estado = ESTADO_ACTIVA;
    }

    public void cancelar() {
        this.estado = ESTADO_CANCELADA;
    }

    public void finalizar() {
        this.estado = ESTADO_FINALIZADA;
    }

    public long getDuracionDias() {
        if (fechaDesde == null || fechaHasta == null) return 0;
        return fechaDesde.until(fechaHasta).getDays();
    }

    public boolean incluyeFecha(LocalDate fecha) {
        if (fechaDesde == null || fechaHasta == null || fecha == null) return false;
        return !fecha.isBefore(fechaDesde) && !fecha.isAfter(fechaHasta);
    }

    public boolean seSuperponeConPeriodo(LocalDate desde, LocalDate hasta) {
        if (fechaDesde == null || fechaHasta == null || desde == null || hasta == null) return false;
        return !(fechaHasta.isBefore(desde) || fechaDesde.isAfter(hasta));
    }

    public boolean esValida() {
        return codigo != null && !codigo.trim().isEmpty() &&
                codigoHabitacion != null && !codigoHabitacion.trim().isEmpty() &&
                fechaDesde != null && fechaHasta != null &&
                !fechaDesde.isAfter(fechaHasta);
    }

    public String getFechaDesdeString() {
        return fechaDesde != null ? fechaDesde.format(FORMATTER) : "";
    }

    public String getFechaHastaString() {
        return fechaHasta != null ? fechaHasta.format(FORMATTER) : "";
    }

    public String getFechaCreacionString() {
        return fechaCreacion != null ? fechaCreacion.format(FORMATTER) : "";
    }

    public void setFechaDesdeString(String fechaString) {
        try {
            this.fechaDesde = LocalDate.parse(fechaString, FORMATTER);
        } catch (Exception e) {
            this.fechaDesde = null;
        }
    }

    public void setFechaHastaString(String fechaString) {
        try {
            this.fechaHasta = LocalDate.parse(fechaString, FORMATTER);
        } catch (Exception e) {
            this.fechaHasta = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservaDTO that = (ReservaDTO) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "ReservaDTO{" +
                "codigo='" + codigo + '\'' +
                ", codigoHabitacion='" + codigoHabitacion + '\'' +
                ", codigoHotel='" + codigoHotel + '\'' +
                ", fechaDesde=" + fechaDesde +
                ", fechaHasta=" + fechaHasta +
                ", estado='" + estado + '\'' +
                ", duracion=" + getDuracionDias() + " días" +
                ", precioTotal=" + precioTotal +
                '}';
    }

    public String toStringCompleto() {
        return "ReservaDTO{" +
                "codigo='" + codigo + '\'' +
                ", habitación='" + codigoHabitacion + '\'' +
                ", hotel='" + codigoHotel + '\'' +
                ", cliente='" + clienteNombre + '\'' +
                ", desde=" + fechaDesde +
                ", hasta=" + fechaHasta +
                ", duración=" + getDuracionDias() + " días" +
                ", estado='" + estado + '\'' +
                ", precio=" + precioTotal +
                ", creada=" + fechaCreacion +
                '}';
    }
}