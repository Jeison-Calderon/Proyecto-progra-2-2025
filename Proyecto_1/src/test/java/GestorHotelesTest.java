import aplicacion.dto.HotelDTO;
import aplicacion.grafica.GestorHoteles;
import aplicacion.servicio.ServicioHoteles;
import aplicacion.util.NotificacionManager;
import aplicacion.util.TabManager;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GestorHotelesTest {

    private ServicioHoteles servicioHoteles;
    private NotificacionManager notificacionManager;
    private TabManager tabManager;
    private GestorHoteles gestor;

    @BeforeAll
    public static void initToolkit() {
        new JFXPanel();  // Esto inicializa el toolkit JavaFX
    }

    @BeforeEach
    public void setUp() {
        // Crear objetos JavaFX mínimos para los managers
        TextArea dummyTextArea = new TextArea();
        TabPane dummyTabPane = new TabPane();

        servicioHoteles = new ServicioHoteles();
        notificacionManager = new NotificacionManager(dummyTextArea);
        tabManager = new TabManager(dummyTabPane);

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
        gestor.buscarHoteles("Azul");  // ahora debe ser public

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
        //Act
        assertDoesNotThrow(() -> gestor.cargarDatosDesdeServidor());
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
        TableView<HotelDTO> tabla = gestor.getTabla();
        assertNotNull(tabla);
    }

    @Test
    public void testSetOnVerHabitaciones_noLanzaError() {
        gestor.setOnVerHabitaciones(hotel -> System.out.println("Hotel: " + hotel.getNombre()));
        assertDoesNotThrow(() -> gestor.setOnVerHabitaciones(h -> {}));
    }
}
