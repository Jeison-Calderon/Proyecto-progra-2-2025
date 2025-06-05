module org.example.proyecto_1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.json;
    requires java.desktop;

    exports aplicacion.grafica;
    exports aplicacion.domain;
    exports aplicacion.data;
    exports aplicacion.dto;
    exports aplicacion.util;
    exports aplicacion.servidor;
    exports aplicacion.cliente;

    // ✅ CRÍTICO: Estas líneas permiten que JavaFX acceda a tus clases
    opens aplicacion.domain to javafx.base, javafx.fxml;
    opens aplicacion.dto to javafx.base, javafx.fxml;
    opens aplicacion.grafica to javafx.fxml;
}