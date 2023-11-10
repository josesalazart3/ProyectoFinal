package umg.storevideojuegos;

import umg.storevideojuegos.Venta;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static umg.storevideojuegos.Venta.realizarNuevaVenta;

public class StoreVideoJuegos {

    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        Cliente cliente = new Cliente();
        ConfiguracionBaseDatos configuracion = new ConfiguracionBaseDatos("jdbc:postgresql://localhost:5432/postgres", "postgres", "1234");

        String opcionMenuPrincipal;
        String opcionSubMenu;

        do {
            System.out.println("\nMenú Principal:");
            System.out.println("1. Clientes");
            System.out.println("2. Inventario");
            System.out.println("3. Ventas");
            System.out.println("4. Salir");
            System.out.print("Selecciona una opción del menú principal: ");
            opcionMenuPrincipal = scanner.nextLine();

            switch (opcionMenuPrincipal) {
                case "1":
                    // Opción relacionada con clientes
                    System.out.println("\nMenú de Clientes:");
                    System.out.println("1. Crear un cliente");
                    System.out.println("2. Modificar un cliente");
                    System.out.println("3. Ver todos los clientes");
                    System.out.println("4. Eliminar un cliente");
                    System.out.println("5. Volver al menú principal");
                    System.out.print("Selecciona una opción del menú de clientes: ");
                    opcionSubMenu = scanner.nextLine();

                    switch (opcionSubMenu) {
                        case "1":
                            // Crear un nuevo cliente
                            cliente.crearClienteDesdeUsuario(configuracion.getUrl(), configuracion.getUsuario(), configuracion.getContraseña());
                            break;
                        case "2":
                            // Modificar un cliente
                            System.out.print("ID del cliente a modificar: ");
                            int idClienteAModificar = Integer.parseInt(scanner.nextLine());
                            System.out.print("Nuevo nombre del cliente: ");
                            String nuevoNombre = scanner.nextLine();
                            System.out.print("Nuevo NIT del cliente: ");
                            String nuevoNit = scanner.nextLine();

                            // Configurar los datos del cliente
                            cliente.setId(idClienteAModificar);
                            cliente.setNombre(nuevoNombre);
                            cliente.setNit(nuevoNit);

                            // Llamar al método para modificar el cliente
                            cliente.modificarCliente(configuracion.getUrl(), configuracion.getUsuario(), configuracion.getContraseña());
                            break;

                        case "3":
                            // Ver todos los clientes
                            System.out.println("Clientes en la base de datos:");
                            cliente.mostrarTodosLosClientes(configuracion.getUrl(), configuracion.getUsuario(), configuracion.getContraseña());
                            break;
                        case "4":
                            // Eliminar un cliente
                            System.out.print("ID del cliente a eliminar: ");
                            int clienteAEliminarID = obtenerEnteroDesdeInput(scanner);

                            break;
                        case "5":
                            break;
                        default:
                            System.out.println("Opción no válida. Inténtalo de nuevo.");
                    }
                    break;

                case "2":
                    // Opción relacionada con inventario
                    System.out.println("\nMenú de Inventario:");
                    System.out.println("1. Mostrar inventario");
                    System.out.println("2. Agregar videojuego al inventario");
                    System.out.println("3. Eliminar videojuego del inventario");
                    System.out.println("4. Encontrar videojuegos por consola");
                    System.out.println("5. Obtener total de productos en el inventario");
                    System.out.println("6. Volver al menú principal");
                    System.out.print("Selecciona una opción del menú de inventario: ");
                    opcionSubMenu = scanner.nextLine();

                    Inventario inventario = new Inventario(configuracion.getConnection());

                    switch (opcionSubMenu) {
                        case "1":
                            // Mostrar inventario
                            List<Inventario> inventarios = inventario.mostrarInventario();
                            System.out.println("Inventario:");
                            for (Inventario inv : inventarios) {
                                System.out.println("ID: " + inv.getId());
                                System.out.println("Nombre: " + inv.getNombre());
                                System.out.println("Precio: " + inv.getPrecio());
                                System.out.println("Consola: " + inv.getConsola());
                                System.out.println("Cantidad Disponible: " + inv.getCantidadDisponible());
                                System.out.println("------------------------");
                            }
                            break;

                        case "2":
                            // Agregar videojuego al inventario
                            Videojuego nuevoVideojuego = new Videojuego();
                            System.out.print("Nombre del nuevo videojuego: ");
                            nuevoVideojuego.setNombre(scanner.nextLine());
                            System.out.print("Precio del nuevo videojuego: ");
                            nuevoVideojuego.setPrecio(obtenerDoubleDesdeInput(scanner));
                            System.out.print("Consola del nuevo videojuego: ");
                            nuevoVideojuego.setConsola(scanner.nextLine());
                            System.out.print("Cantidad disponible del nuevo videojuego: ");
                            nuevoVideojuego.setCantidadDisponible(obtenerEnteroDesdeInput(scanner));

                            inventario.agregarVideojuego(nuevoVideojuego);
                            break;

                        case "3":
                            // Eliminar videojuego del inventario
                            System.out.print("ID del videojuego a eliminar: ");
                            int idVideojuegoAEliminar = Integer.parseInt(scanner.nextLine());
                            inventario.eliminarVideojuegoDelInventario(idVideojuegoAEliminar);
                            System.out.println("Videojuego eliminado del inventario.");
                            break;

                        case "4":
                            // Encontrar videojuegos por consola
                            System.out.print("Ingrese la consola: ");
                            String consolaBuscar = scanner.nextLine();
                            List<Inventario> videojuegosConsola = inventario.encontrarVideojuegosPorConsola(consolaBuscar);
                            System.out.println("Videojuegos encontrados para la consola " + consolaBuscar + ":");
                            for (Inventario inv : videojuegosConsola) {
                                System.out.println("ID: " + inv.getId());
                                System.out.println("Nombre: " + inv.getNombre());
                                System.out.println("Precio: " + inv.getPrecio());
                                System.out.println("Cantidad Disponible: " + inv.getCantidadDisponible());
                                System.out.println("------------------------");
                            }
                            break;

                        case "5":
                            // Obtener total de productos en el inventario
                            int totalProductos = inventario.obtenerTotalProductosEnInventario();
                            System.out.println("Total de productos en el inventario: " + totalProductos);
                            break;

                        case "6":
                            break; // Sal del menú de inventario

                        default:
                            System.out.println("Opción no válida. Inténtalo de nuevo.");
                    }
                    break;

                case "3":
                    // Opción relacionada con ventas
                    System.out.println("\nMenú de Ventas:");
                    System.out.println("1. Realizar una nueva venta");
                    System.out.println("2. Ver historial de ventas por cliente");
                    System.out.println("3. ver todas las ventas");
                    System.out.println("4. Volver al menú principal");
                    System.out.print("Selecciona una opción del menú de ventas: ");
                    opcionSubMenu = scanner.nextLine();

                    switch (opcionSubMenu) {
                        case "1":
                            realizarNuevaVenta(configuracion.getConnection(), scanner, configuracion);
                            break;

                        case "2":
                            Venta.verHistorialVentasPorCliente(configuracion.getConnection(), scanner);
                            break;

                        case "3":

                            System.out.print("Mostrar todas las ventas: ");
                            Venta.verTodasLasVentas(configuracion.getConnection());

                          
                            break;

                        case "4":
                            System.out.println("Volviendo al menú principal.");
                            break;

                        default:
                            System.out.println("Opción no válida. Inténtalo de nuevo.");
                    }
                    break;

                case "4":
                    System.out.println("Saliendo del programa.");
                    break;

                default:
                    System.out.println("Opción no válida. Inténtalo de nuevo.");
            }
        } while (!opcionMenuPrincipal.equals("4"));

        // Cierra el scanner al salir del programa
        scanner.close();
    }

    // Método auxiliar para obtener un entero desde el input del usuario
    private static int obtenerEnteroDesdeInput(Scanner scanner) {
        int resultado = 0;
        boolean entradaValida = false;

        while (!entradaValida) {
            try {
                resultado = Integer.parseInt(scanner.nextLine());
                entradaValida = true;
            } catch (NumberFormatException ex) {
                System.out.println("Error: Ingrese un valor entero válido.");
            }
        }

        return resultado;
    }

    // Método auxiliar para obtener un double desde el input del usuario
    private static double obtenerDoubleDesdeInput(Scanner scanner) {
        double resultado = 0;
        boolean entradaValida = false;

        while (!entradaValida) {
            try {
                resultado = Double.parseDouble(scanner.nextLine());
                entradaValida = true;
            } catch (NumberFormatException ex) {
                System.out.println("Error: Ingrese un valor numérico válido.");
            }
        }

        return resultado;
    }

}
