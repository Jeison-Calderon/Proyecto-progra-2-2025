
package Aplicacion;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HotelDTO {
    private final StringProperty codigo;
    private final StringProperty nombre;
    private final StringProperty ubicacion;

    public HotelDTO(String codigo, String nombre, String ubicacion) {
        this.codigo = new SimpleStringProperty(codigo);
        this.nombre = new SimpleStringProperty(nombre);
        this.ubicacion = new SimpleStringProperty(ubicacion);
    }

    public String getCodigo() {
        return codigo.get();
    }

    public StringProperty codigoProperty() {
        return codigo;
    }

    public String getNombre() {
        return nombre.get();
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion.get();
    }

    public StringProperty ubicacionProperty() {
        return ubicacion;
    }
}
