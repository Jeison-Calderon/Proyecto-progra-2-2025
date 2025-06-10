package aplicacion.util;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class TabManager {

    private TabPane tabPane;

    public TabManager(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    public Tab crearPestana(String titulo, boolean esCerrable) {
        Tab tab = new Tab(titulo);
        tab.setClosable(esCerrable);
        return tab;
    }

    public void agregarYSeleccionar(Tab tab) {
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public void cerrarPestanasTemporales() {
        while (tabPane.getTabs().size() > 1) {
            tabPane.getTabs().remove(1);
        }
        tabPane.getSelectionModel().selectFirst();
    }

    public Tab buscarPestanaPorTitulo(String prefijo) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().startsWith(prefijo)) {
                return tab;
            }
        }
        return null;
    }

    public boolean seleccionarPestana(String prefijo) {
        Tab tab = buscarPestanaPorTitulo(prefijo);
        if (tab != null) {
            tabPane.getSelectionModel().select(tab);
            return true;
        }
        return false;
    }

    public void volverAPrincipal() {
        tabPane.getSelectionModel().selectFirst();
    }

    public void removerPestana(Tab tab) {
        tabPane.getTabs().remove(tab);
    }

    public boolean existePestana(String prefijo) {
        return buscarPestanaPorTitulo(prefijo) != null;
    }

    public Tab getPestanaActiva() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    public void reemplazarConNueva(Tab nuevaPestana) {
        cerrarPestanasTemporales();
        agregarYSeleccionar(nuevaPestana);
    }
}