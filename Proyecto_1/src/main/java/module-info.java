module org.example.proyecto_1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens org.example.proyecto_1 to javafx.fxml;
}
