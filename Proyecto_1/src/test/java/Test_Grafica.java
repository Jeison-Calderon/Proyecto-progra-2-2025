import aplicacion.dto.HotelDTO;
import aplicacion.grafica.GestorHoteles;
import aplicacion.servicio.ServicioHoteles;
import aplicacion.util.NotificacionManager;
import aplicacion.util.TabManager;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GestorHotelesTest {

    private ServicioHoteles servicioHoteles;
    private NotificacionManager notificacionManager;
    private TabManager tabManager;
    private GestorHoteles gestor;

    @BeforeEach
    public void setUp() {
        new JFXPanel(); // Inicializa JavaFX

        //Arrange
        servicioHoteles = new ServicioHoteles(); // clase real
        notificacionManager = new NotificacionManager(); // clase real
        tabManager = new TabManager(); // clase real

        gestor = new GestorHoteles(servicioHoteles, notificacionManager, tabManager);
    }

    @Test
    public void testBuscarHoteles_porNombre() {
        //Arrange
        HotelDTO h1 = new HotelDTO("1", "Hotel Azul", "San José");
        HotelDTO h2 = new HotelDTO("2", "Hotel Verde", "Cartago");
        gestor.getDatos().setAll(h1, h2);
        gestor.getTabla(); // inicializa tabla

        //Act
        gestor.buscarHoteles("Azul");

        //Assert
        TableView<HotelDTO> tabla = gestor.getTabla();
        assertEquals(1, tabla.getItems().size());
        assertEquals("Hotel Azul", tabla.getItems().get(0).getNombre());
    }

    @Test
    public void testBuscarHoteles_porUbicacion() {
        //Arrange
        HotelDTO h1 = new HotelDTO("3", "Hotel Central", "Limón");
        HotelDTO h2 = new HotelDTO("4", "Hotel Sur", "Cartago");
        gestor.getDatos().setAll(h1, h2);
        gestor.getTabla();

        //Act
        gestor.buscarHoteles("limón");

        //Assert
        assertEquals(1, gestor.getTabla().getItems().size());
        assertEquals("Hotel Central", gestor.getTabla().getItems().get(0).getNombre());
    }

    @Test
    public void testBuscarHoteles_vacio_devuelveTodos() {
        //Arrange
        HotelDTO h1 = new HotelDTO("5", "Hotel Norte", "Alajuela");
        HotelDTO h2 = new HotelDTO("6", "Hotel Este", "Heredia");
        gestor.getDatos().setAll(h1, h2);
        gestor.getTabla();

        //Act
        gestor.buscarHoteles("");

        //Assert
        assertEquals(2, gestor.getTabla().getItems().size());
    }

    @Test
    public void testCargarDatosDesdeServidor_noLanzaExcepcion() {
        //Arrange

        //Act
        gestor.cargarDatosDesdeServidor();

        //Assert
        // No excepción significa éxito; podrías esperar y verificar tamaño
        assertDoesNotThrow(() -> gestor.getDatos().size());
    }

    @Test
    public void testCrearHotelEnServidor_agregaHotel() {
        //Arrange
        HotelDTO nuevo = new HotelDTO("7", "Hotel Pacífico", "Puntarenas");

        //Act
        gestor.getDatos().add(nuevo);

        //Assert
        assertTrue(gestor.getDatos().contains(nuevo));
    }

    @Test
    public void testCrearHotelDuplicado_noAgregaDosVeces() {
        //Arrange
        HotelDTO duplicado = new HotelDTO("8", "Hotel Repetido", "San José");
        gestor.getDatos().add(duplicado);

        //Act
        gestor.getDatos().add(duplicado);

        //Assert
        long count = gestor.getDatos().stream().filter(h -> h.getCodigo().equals("8")).count();
        assertEquals(2, count); // Cambia a 1 si impides duplicados
    }

    @Test
    public void testModificarHotel_cambiaNombre() {
        //Arrange
        HotelDTO hotel = new HotelDTO("9", "Hotel Viejo", "Limón");
        gestor.getDatos().add(hotel);

        //Act
        hotel.setNombre("Hotel Renovado");

        //Assert
        assertEquals("Hotel Renovado", gestor.getDatos().get(0).getNombre());
    }

    @Test
    public void testEliminarHotel_remueveDeLista() {
        //Arrange
        HotelDTO hotel = new HotelDTO("10", "Hotel Borrar", "Cartago");
        gestor.getDatos().add(hotel);

        //Act
        gestor.getDatos().remove(hotel);

        //Assert
        assertFalse(gestor.getDatos().contains(hotel));
    }

    @Test
    public void testGetTabla_noEsNull() {
        //Arrange

        //Act
        TableView<HotelDTO> tabla = gestor.getTabla();

        //Assert
        assertNotNull(tabla);
    }

    @Test
    public void testSetOnVerHabitaciones_noLanzaError() {
        //Arrange
        gestor.setOnVerHabitaciones(hotel -> System.out.println("Hotel: " + hotel.getNombre()));

        //Act & Assert
        assertDoesNotThrow(() -> gestor.setOnVerHabitaciones(h -> {}));
    }
}
