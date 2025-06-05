module org.example.proyecto_1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.json;
    requires java.desktop;

    // ✅ ACTUALIZADO: Solo exportar paquetes que existen
    exports aplicacion.grafica;
    exports aplicacion.dto;
    exports aplicacion.util;
    exports aplicacion.servidor;
    exports aplicacion.cliente;
    exports aplicacion.data;
    exports aplicacion.servicio;  // ✅ NUEVO: Agregar paquete servicio

    // ✅ CRÍTICO: Abrir DTOs para JavaFX (ya no domain porque se eliminó)
    opens aplicacion.dto to javafx.base, javafx.fxml;
    opens aplicacion.grafica to javafx.fxml;

    // ✅ OPCIONAL: Abrir data si necesitas serialización avanzada
    opens aplicacion.data to javafx.base;
}