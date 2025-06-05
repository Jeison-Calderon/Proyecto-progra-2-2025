package aplicacion.cliente;

import aplicacion.dto.HabitacionDTO;
import aplicacion.dto.HotelDTO;
import aplicacion.dto.RespuestaDTO;
import aplicacion.util.JsonUtil;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class EchoCliente {
    private static final String HOST = "10.59.18.141"; // o la IP del servidor
    private static final int PUERTO = 5001; // mismo puerto que ActivarServidor

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Cliente de Gestión de Hoteles");
        System.out.println("-----------------------------");

        boolean continuar = true;

        while (continuar) {
            mostrarMenu();
            System.out.print("Seleccione una opción: ");
            try {
                int opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        listarHoteles();
                        break;
                    case 2:
                        guardarHotel(scanner);
                        break;
                    case 3:
                        eliminarHotel(scanner);
                        break;
                    case 4:
                        listarHabitaciones();
                        break;
                    case 5:
                        enviarArchivo(scanner);
                        break;
                    case 0:
                        System.out.println("Saliendo...");
                        continuar = false;
                        break;
                    default:
                        System.out.println("Opción no válida");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingrese un número válido");
            } catch (IOException e) {
                System.out.println("Error de comunicación: " + e.getMessage());
                e.printStackTrace();
            }

            if (continuar) {
                System.out.println("\nPresione ENTER para continuar...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ ---");
        System.out.println("1. Listar hoteles");
        System.out.println("2. Registrar hotel");
        System.out.println("3. Eliminar hotel");
        System.out.println("4. Listar habitaciones");
        System.out.println("5. Enviar archivo");
        System.out.println("0. Salir");
    }

    private static void listarHoteles() throws IOException {
        System.out.println("\n--- LISTADO DE HOTELES ---");

        try (Socket socket = new Socket(HOST, PUERTO)) {
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // Solicitar listado de hoteles
            salida.writeUTF("LISTAR_HOTELES");

            // Recibir respuesta
            String respuestaStr = entrada.readUTF();
            JSONObject jsonRespuesta = new JSONObject(respuestaStr);

            String estado = jsonRespuesta.getString("estado");
            String mensaje = jsonRespuesta.getString("mensaje");

            if ("OK".equals(estado)) {
                JSONArray hotelesJson = jsonRespuesta.getJSONArray("hoteles");
                List<HotelDTO> hoteles = JsonUtil.jsonToHoteles(hotelesJson);

                System.out.println("Hoteles disponibles:");
                System.out.println("------------------------------------------------------------");
                System.out.printf("%-10s %-30s %-20s\n", "CÓDIGO", "NOMBRE", "UBICACIÓN");
                System.out.println("------------------------------------------------------------");

                for (HotelDTO hotel : hoteles) {
                    System.out.printf("%-10s %-30s %-20s\n",
                            hotel.getCodigo(),
                            hotel.getNombre(),
                            hotel.getUbicacion());
                }
                System.out.println("------------------------------------------------------------");
                System.out.println("Total de hoteles: " + hoteles.size());
            } else {
                System.out.println("Error: " + mensaje);
            }

        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
            throw e;
        }
    }

    private static void guardarHotel(Scanner scanner) throws IOException {
        System.out.println("\n--- REGISTRAR HOTEL ---");

        System.out.print("Nombre del hotel: ");
        String nombre = scanner.nextLine();

        System.out.print("Ubicación: ");
        String ubicacion = scanner.nextLine();

        // Crear el objeto DTO
        HotelDTO nuevoHotel = new HotelDTO("", nombre, ubicacion);

        try (Socket socket = new Socket(HOST, PUERTO)) {
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // Solicitar guardar hotel
            salida.writeUTF("GUARDAR_HOTEL");

            // Enviar datos del hotel
            JSONObject hotelJson = JsonUtil.hotelToJson(nuevoHotel);
            salida.writeUTF(hotelJson.toString());

            // Recibir respuesta
            String respuestaStr = entrada.readUTF();
            JSONObject jsonRespuesta = new JSONObject(respuestaStr);

            String estado = jsonRespuesta.getString("estado");
            String mensaje = jsonRespuesta.getString("mensaje");

            if ("OK".equals(estado)) {
                System.out.println("Éxito: " + mensaje);

                // Mostrar el hotel guardado
                if (jsonRespuesta.has("hotel")) {
                    HotelDTO hotelGuardado = JsonUtil.jsonToHotel(jsonRespuesta.getJSONObject("hotel"));
                    System.out.println("Detalles del hotel guardado:");
                    System.out.println("Código: " + hotelGuardado.getCodigo());
                    System.out.println("Nombre: " + hotelGuardado.getNombre());
                    System.out.println("Ubicación: " + hotelGuardado.getUbicacion());
                }
            } else {
                System.out.println("Error: " + mensaje);
            }
        }
    }

    private static void eliminarHotel(Scanner scanner) throws IOException {
        System.out.println("\n--- ELIMINAR HOTEL ---");

        System.out.print("Código del hotel a eliminar: ");
        String codigo = scanner.nextLine();

        try (Socket socket = new Socket(HOST, PUERTO)) {
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // Solicitar eliminar hotel
            salida.writeUTF("ELIMINAR_HOTEL");

            // Enviar código del hotel
            salida.writeUTF(codigo);

            // Recibir respuesta
            String respuestaStr = entrada.readUTF();
            JSONObject jsonRespuesta = new JSONObject(respuestaStr);

            String estado = jsonRespuesta.getString("estado");
            String mensaje = jsonRespuesta.getString("mensaje");

            System.out.println(estado.equals("OK") ? "Éxito: " + mensaje : "Error: " + mensaje);
        }
    }

    private static void listarHabitaciones() throws IOException {
        System.out.println("\n--- LISTADO DE HABITACIONES ---");

        try (Socket socket = new Socket(HOST, PUERTO)) {
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // Solicitar listado de habitaciones
            salida.writeUTF("LISTAR_HABITACIONES");

            // Recibir respuesta
            String respuestaStr = entrada.readUTF();
            JSONObject jsonRespuesta = new JSONObject(respuestaStr);

            String estado = jsonRespuesta.getString("estado");
            String mensaje = jsonRespuesta.getString("mensaje");

            if ("OK".equals(estado)) {
                JSONArray habitacionesJson = jsonRespuesta.getJSONArray("habitaciones");
                List<HabitacionDTO> habitaciones = JsonUtil.jsonToHabitaciones(habitacionesJson);

                System.out.println("Habitaciones disponibles:");
                System.out.println("------------------------------------------------------------");
                System.out.printf("%-10s %-20s %10s\n", "CÓDIGO", "ESTILO", "PRECIO");
                System.out.println("------------------------------------------------------------");

                for (HabitacionDTO hab : habitaciones) {
                    System.out.printf("%-10s %-20s %10.2f\n",
                            hab.getCodigo(),
                            hab.getEstilo(),
                            hab.getPrecio());
                }

                System.out.println("------------------------------------------------------------");
                System.out.println("Total de habitaciones: " + habitaciones.size());
            } else {
                System.out.println("Error: " + mensaje);
            }
        }
    }

    private static void enviarArchivo(Scanner scanner) throws IOException {
        System.out.println("\n--- ENVIAR ARCHIVO ---");

        System.out.print("Ruta del archivo a enviar: ");
        String rutaArchivo = scanner.nextLine();

        File archivo = new File(rutaArchivo);
        if (!archivo.exists() || !archivo.isFile()) {
            System.out.println("Error: El archivo no existe o no es válido");
            return;
        }

        try (Socket socket = new Socket(HOST, PUERTO)) {
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // Indicar que vamos a enviar un archivo
            salida.writeUTF("ENVIAR_ARCHIVO");

            // Enviar el nombre del archivo
            salida.writeUTF(archivo.getName());

            // Esperar confirmación
            String confirmacion = entrada.readUTF();
            if ("OK".equals(confirmacion)) {
                // Enviar el archivo
                try (FileInputStream fis = new FileInputStream(archivo)) {
                    byte[] buffer = new byte[4096];
                    int leido;
                    while ((leido = fis.read(buffer)) > 0) {
                        salida.write(buffer, 0, leido);
                    }
                    salida.flush();
                }

                // Socket se cierra automáticamente al salir del try-with-resources

                // Leer respuesta
                System.out.println("Archivo enviado correctamente");

                try {
                    String respuestaStr = entrada.readUTF();
                    JSONObject jsonRespuesta = new JSONObject(respuestaStr);
                    String mensaje = jsonRespuesta.getString("mensaje");
                    System.out.println("Respuesta del servidor: " + mensaje);
                } catch (IOException e) {
                    // Podría darse que el servidor ya haya cerrado la conexión
                    System.out.println("No se pudo leer la respuesta del servidor");
                }
            } else {
                System.out.println("Error: El servidor rechazó la transferencia");
            }
        }
    }
}