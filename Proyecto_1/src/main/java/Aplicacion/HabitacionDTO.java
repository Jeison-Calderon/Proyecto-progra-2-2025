package Aplicacion;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HabitacionDTO {
    private final StringProperty codigo;
    private final StringProperty estilo;
    private final StringProperty precio;

    public HabitacionDTO(String codigo, String estilo, String precio) {
        this.codigo = new SimpleStringProperty(codigo);
        this.estilo = new SimpleStringProperty(estilo);
        this.precio = new SimpleStringProperty(precio);
    }

    public String getCodigo() {
        return codigo.get();
    }

    public StringProperty codigoProperty() {
        return codigo;
    }

    public String getEstilo() {
        return estilo.get();
    }

    public StringProperty estiloProperty() {
        return estilo;
    }

    public String getPrecio() {
        return precio.get();
    }

    public StringProperty precioProperty() {
        return precio;
    }
}
