package aplicacion.util;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class TabManager {

    private TabPane tabPane;

    public TabManager(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    // ✅ CREAR PESTAÑA
    public Tab crearPestana(String titulo, boolean esCerrable) {
        Tab tab = new Tab(titulo);
        tab.setClosable(esCerrable);
        return tab;
    }

    // ✅ AGREGAR PESTAÑA Y SELECCIONARLA
    public void agregarYSeleccionar(Tab tab) {
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    // ✅ CERRAR PESTAÑAS TEMPORALES (mantener solo la primera)
    public void cerrarPestanasTemporales() {
        while (tabPane.getTabs().size() > 1) {
            tabPane.getTabs().remove(1);
        }
        tabPane.getSelectionModel().selectFirst();
    }

    // ✅ BUSCAR PESTAÑA POR PREFIJO EN EL TÍTULO
    public Tab buscarPestanaPorTitulo(String prefijo) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().startsWith(prefijo)) {
                return tab;
            }
        }
        return null;
    }

    // ✅ SELECCIONAR PESTAÑA POR PREFIJO
    public boolean seleccionarPestana(String prefijo) {
        Tab tab = buscarPestanaPorTitulo(prefijo);
        if (tab != null) {
            tabPane.getSelectionModel().select(tab);
            return true;
        }
        return false;
    }

    // ✅ VOLVER A LA PESTAÑA PRINCIPAL
    public void volverAPrincipal() {
        tabPane.getSelectionModel().selectFirst();
    }

    // ✅ REMOVER PESTAÑA ESPECÍFICA
    public void removerPestana(Tab tab) {
        tabPane.getTabs().remove(tab);
    }

    // ✅ VERIFICAR SI EXISTE PESTAÑA
    public boolean existePestana(String prefijo) {
        return buscarPestanaPorTitulo(prefijo) != null;
    }

    // ✅ OBTENER PESTAÑA ACTIVA
    public Tab getPestanaActiva() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    // ✅ REEMPLAZAR PESTAÑAS TEMPORALES (cerrar todas y abrir nueva)
    public void reemplazarConNueva(Tab nuevaPestana) {
        cerrarPestanasTemporales();
        agregarYSeleccionar(nuevaPestana);
    }
}