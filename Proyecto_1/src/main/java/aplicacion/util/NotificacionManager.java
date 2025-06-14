package aplicacion.util;

import javafx.animation.PauseTransition;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

public class NotificacionManager {

    private TextArea areaNotificaciones;
    private static final String ESTILO_EXITO = "-fx-control-inner-background: #d4edda;";
    private static final String ESTILO_ERROR = "-fx-control-inner-background: #f8d7da;";
    private static final String ESTILO_INFO = "-fx-control-inner-background: #d1ecf1;";
    private static final String ESTILO_NORMAL = "-fx-control-inner-background: #f8f9fa;";

    public NotificacionManager(TextArea areaNotificaciones) {
        this.areaNotificaciones = areaNotificaciones;
        configurarArea();
    }

    private void configurarArea() {
        areaNotificaciones.setEditable(false);
        areaNotificaciones.setWrapText(true);
        areaNotificaciones.setPrefHeight(80);
        areaNotificaciones.setStyle(ESTILO_NORMAL);
    }

    public void mostrarExito(String mensaje) {
        mostrarNotificacion(mensaje, ESTILO_EXITO, 3.0);
    }

    public void mostrarError(String mensaje) {
        mostrarNotificacion(mensaje, ESTILO_ERROR, 5.0);
    }

    public void mostrarInfo(String mensaje) {
        mostrarNotificacion(mensaje, ESTILO_INFO, 3.0);
    }

    public void mostrarNotificacion(String mensaje, boolean esExito) {
        if (esExito) {
            mostrarExito(mensaje);
        } else {
            mostrarError(mensaje);
        }
    }

    private void mostrarNotificacion(String mensaje, String estilo, double duracionSegundos) {
        areaNotificaciones.clear();
        areaNotificaciones.appendText(mensaje + "\n");
        areaNotificaciones.setStyle(estilo);

        PauseTransition delay = new PauseTransition(Duration.seconds(duracionSegundos));
        delay.setOnFinished(event -> limpiarNotificacion());
        delay.play();
    }

    public void limpiarNotificacion() {
        areaNotificaciones.clear();
        areaNotificaciones.setStyle(ESTILO_NORMAL);
    }

    public void hotelCreado(String codigo) {
        mostrarExito("Hotel registrado con código: " + codigo);
    }

    public void hotelModificado() {
        mostrarExito("Hotel modificado correctamente");
    }

    public void hotelEliminado() {
        mostrarExito("Hotel eliminado correctamente");
    }

    public void habitacionCreada(String codigo) {
        mostrarExito("Habitación registrada con código: " + codigo);
    }

    public void habitacionModificada() {
        mostrarExito("Habitación modificada correctamente");
    }

    public void habitacionEliminada() {
        mostrarExito("Habitación eliminada correctamente");
    }

    public void errorConexion(String detalle) {
        mostrarError("Error conectando al servidor: " + detalle);
    }

    public void errorDuplicado(String entidad) {
        mostrarError("Error: " + entidad + " duplicado");
    }

    public void errorCamposObligatorios() {
        mostrarError("Todos los campos son obligatorios");
    }

    public void errorPrecioInvalido() {
        mostrarError("El precio debe ser un número válido");
    }

    public void errorGenerico(String mensaje) {
        mostrarError("Error: " + mensaje);
    }
}