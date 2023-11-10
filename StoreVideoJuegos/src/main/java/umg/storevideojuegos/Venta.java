package umg.storevideojuegos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static umg.storevideojuegos.Facturacion.generarFactura;

public class Venta {

    public static void realizarNuevaVenta(Connection connection, Scanner scanner, ConfiguracionBaseDatos configuracion) {
        try {
            // Obtener datos necesarios para la venta
            System.out.println("Ingrese el ID del cliente:");
            int clienteId = scanner.nextInt();
            scanner.nextLine(); // Consumir la línea después del entero

            // Obtener productos del carrito
            List<ProductoVenta> productos = obtenerProductosCarrito(connection, scanner);

            // Mostrar listado de productos y total
            System.out.println("Listado de productos a comprar:");
            mostrarListadoProductos(productos);
            BigDecimal total = calcularTotal(productos);
            System.out.println("Total: " + total);

            // Confirmar la transacción
            System.out.println("¿Desea realizar la transacción? (S/N)");
            String confirmacion = scanner.next();
            if (confirmacion.equalsIgnoreCase("S")) {
                // Obtener método de pago
                System.out.println("Seleccione el tipo de pago:");
                System.out.println("1. Pago en efectivo");
                System.out.println("2. Pago con tarjeta");
                System.out.println("3. Transferencia");
                int tipoPago = scanner.nextInt();

                String metodoPago = obtenerTipoPago(tipoPago);

                // Realizar la venta y actualizar inventario
                int ventaId = realizarVenta(connection, clienteId, productos, metodoPago);

                System.out.println("Venta realizada exitosamente.");

                // Preguntar y registrar el tipo de pago
                System.out.println("¿Desea registrar el tipo de pago? (S/N)");
                String registrarPago = scanner.next();
                if (registrarPago.equalsIgnoreCase("S")) {
                    registrarTipoPago(connection, metodoPago, total, ventaId);
                    System.out.println("Tipo de pago registrado exitosamente.");

                    // Preguntar si desea generar factura
                    System.out.println("¿Desea generar la factura? (S/N)");
                    String generarFactura = scanner.next();
                    if (generarFactura.equalsIgnoreCase("S")) {
                        generarFactura(connection, ventaId, metodoPago, obtenerDireccionEnvio(scanner));
                        System.out.println("Factura generada exitosamente.");
                    } else {
                        System.out.println("Factura no generada.");
                    }
                } else {
                    System.out.println("Tipo de pago no registrado.");
                }
            } else {
                System.out.println("Transacción cancelada.");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // o manejo de la excepción según tus necesidades
        }
    }

    private static String obtenerDireccionEnvio(Scanner scanner) {
        System.out.println("Ingrese la dirección de envío:");
        scanner.nextLine(); // Consumir el salto de línea pendiente
        return scanner.nextLine();
    }

    private static List<ProductoVenta> obtenerProductosCarrito(Connection connection, Scanner scanner) throws SQLException {
        List<ProductoVenta> productos = new ArrayList<>();

        while (true) {
            System.out.println("Ingrese el ID del producto (Ingrese 0 para finalizar):");
            int productoId = scanner.nextInt();

            if (productoId == 0) {
                break;
            }

            ProductoVenta producto = obtenerProductoPorId(connection, productoId);

            if (producto != null) {
                System.out.println("Producto: " + producto.getNombre() + ", Precio: " + producto.getPrecio());

                int cantidad = ingresarCantidad(scanner);

                producto.setCantidad(cantidad);
                productos.add(producto);

                // Preguntar si desea comprar más unidades
                System.out.println("¿Desea comprar más unidades de este producto? (S/N)");
                String respuesta = scanner.next();
                if (!respuesta.equalsIgnoreCase("S")) {
                    break;
                }
            } else {
                System.out.println("Producto no encontrado. Ingrese un ID válido.");
            }
        }

        return productos;
    }

    private static void mostrarListadoProductos(List<ProductoVenta> productos) {
        for (ProductoVenta producto : productos) {
            System.out.println("ID: " + producto.getId() + ", Nombre: " + producto.getNombre()
                    + ", Cantidad: " + producto.getCantidad() + ", Precio Unitario: " + producto.getPrecio()
                    + ", Subtotal: " + producto.getSubtotal());
        }
    }

    private static ProductoVenta obtenerProductoPorId(Connection connection, int productoId) throws SQLException {
        String query = "SELECT id, nombre, precio, cantidad FROM public.inventario WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, productoId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String nombre = resultSet.getString("nombre");
                    BigDecimal precio = resultSet.getBigDecimal("precio");
                    int cantidad = resultSet.getInt("cantidad");

                    return new ProductoVenta(id, nombre, precio, cantidad);
                } else {
                    return null; // Producto no encontrado
                }
            }
        }
    }

    private static int realizarVenta(Connection connection, int clienteId, List<ProductoVenta> productos, String metodoPago) throws SQLException {
        try {
            connection.setAutoCommit(false);

            // Insertar en la tabla ventas
            int ventaId = insertarVenta(connection, clienteId, productos, metodoPago);

            // Insertar en la tabla detalles_venta
            insertarDetallesVenta(connection, ventaId, productos);

            // Actualizar inventario
            actualizarInventario(connection, productos);

            connection.commit();

            return ventaId;
        } catch (SQLException e) {
            connection.rollback();
            throw e; // Relanzar la excepción después de realizar el rollback
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static int insertarVenta(Connection connection, int clienteId, List<ProductoVenta> productos, String metodoPago) throws SQLException {
        String query = "INSERT INTO public.ventas (cliente_id, fecha, total, metodo_pago, estado) VALUES (?, now(), ?, ?, ?) RETURNING id";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            BigDecimal total = calcularTotal(productos);

            preparedStatement.setInt(1, clienteId);
            preparedStatement.setBigDecimal(2, total);
            preparedStatement.setString(3, metodoPago);
            preparedStatement.setString(4, "Completado");

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La inserción en la tabla ventas falló, no se generó ninguna fila.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("La inserción en la tabla ventas falló, no se generó ningún ID.");
                }
            }
        }
    }

    private static void insertarDetallesVenta(Connection connection, int ventaId, List<ProductoVenta> productos) throws SQLException {
        String query = "INSERT INTO public.detalles_venta (venta_id, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (ProductoVenta producto : productos) {
                preparedStatement.setInt(1, ventaId);
                preparedStatement.setInt(2, producto.getCantidad());
                preparedStatement.setBigDecimal(3, producto.getPrecio());
                preparedStatement.setBigDecimal(4, producto.getSubtotal());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private static void actualizarInventario(Connection connection, List<ProductoVenta> productos) throws SQLException {
        String query = "UPDATE public.inventario SET cantidad = cantidad - ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (ProductoVenta producto : productos) {
                preparedStatement.setInt(1, producto.getCantidad());
                preparedStatement.setInt(2, producto.getId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private static BigDecimal calcularTotal(List<ProductoVenta> productos) {
        BigDecimal total = BigDecimal.ZERO;
        for (ProductoVenta producto : productos) {
            total = total.add(producto.getSubtotal());
        }
        return total;
    }

    private static int ingresarCantidad(Scanner scanner) {
        while (true) {
            System.out.println("Ingrese la cantidad de unidades:");
            int cantidad = scanner.nextInt();

            if (cantidad > 0) {
                return cantidad;
            } else {
                System.out.println("La cantidad debe ser mayor que 0. Intente nuevamente.");
            }
        }
    }

    public static class ProductoVenta {

        private int id;
        private String nombre;
        private BigDecimal precio;
        private int cantidad;
        private BigDecimal subtotal;

        public ProductoVenta(int id, String nombre, BigDecimal precio, int cantidad) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
            this.subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public BigDecimal getPrecio() {
            return precio;
        }

        public int getCantidad() {
            return cantidad;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
            this.subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private static String obtenerTipoPago(int tipoPago) {
        switch (tipoPago) {
            case 1:
                return "Pago en efectivo";
            case 2:
                return "Pago con tarjeta";
            case 3:
                return "Transferencia";
            default:
                return "Desconocido";
        }
    }

    private static void registrarTipoPago(Connection connection, String tipoPago, BigDecimal total, int ventaId) throws SQLException {
        String query = "INSERT INTO public.pago (tipo_pago, total_pagado, venta_id) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tipoPago);
            preparedStatement.setBigDecimal(2, total);
            preparedStatement.setInt(3, ventaId);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La inserción en la tabla pago falló, no se generó ninguna fila.");
            }
        }
    }

    public static void verHistorialVentasPorCliente(Connection connection, Scanner scanner) {
        try {
            // Obtener el ID del cliente
            System.out.println("Ingrese el ID del cliente para ver su historial de ventas:");
            int clienteId = scanner.nextInt();

            // Obtener el historial de ventas del cliente
            List<VentaInfo> historialVentas = obtenerHistorialVentasPorCliente(connection, clienteId);

            // Mostrar el historial de ventas
            if (!historialVentas.isEmpty()) {
                System.out.println("Historial de ventas para el cliente con ID " + clienteId + ":");
                for (VentaInfo venta : historialVentas) {
                    System.out.println("ID de Venta: " + venta.getIdVenta()
                            + ", Fecha: " + venta.getFecha()
                            + ", Total: " + venta.getTotal()
                            + ", Estado: " + venta.getEstado());
                }
            } else {
                System.out.println("No hay historial de ventas para el cliente con ID " + clienteId);
            }

        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }

    private static List<VentaInfo> obtenerHistorialVentasPorCliente(Connection connection, int clienteId) throws SQLException {
        List<VentaInfo> historialVentas = new ArrayList<>();

        String query = "SELECT v.id, v.fecha, v.total, v.estado "
                + "FROM public.ventas v "
                + "WHERE v.cliente_id = ? "
                + "ORDER BY v.fecha DESC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, clienteId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int idVenta = resultSet.getInt("id");
                    String fecha = resultSet.getString("fecha");
                    BigDecimal total = resultSet.getBigDecimal("total");
                    String estado = resultSet.getString("estado");

                    historialVentas.add(new VentaInfo(idVenta, fecha, total, estado));
                }
            }
        }

        return historialVentas;
    }

    public static void verTodasLasVentas(Connection connection) {
        try {
            String query = "SELECT id, cliente_id, fecha, total, metodo_pago, estado FROM public.ventas";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query); ResultSet resultSet = preparedStatement.executeQuery()) {

                System.out.println("ID\tCliente ID\tFecha\t\t\tTotal\t\tMétodo de Pago\t\tEstado");
                System.out.println("-------------------------------------------------------------------------");

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    int clienteId = resultSet.getInt("cliente_id");
                    Timestamp fecha = resultSet.getTimestamp("fecha");
                    BigDecimal total = resultSet.getBigDecimal("total");
                    String metodoPago = resultSet.getString("metodo_pago");
                    String estado = resultSet.getString("estado");

                    System.out.println(id + "\t" + clienteId + "\t\t" + fecha + "\t" + total + "\t" + metodoPago + "\t\t" + estado);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // 
        }
    }

    public static class VentaInfo {

        private final int idVenta;
        private final String fecha;
        private final BigDecimal total;
        private final String estado;

        public VentaInfo(int idVenta, String fecha, BigDecimal total, String estado) {
            this.idVenta = idVenta;
            this.fecha = fecha;
            this.total = total;
            this.estado = estado;
        }

        public int getIdVenta() {
            return idVenta;
        }

        public String getFecha() {
            return fecha;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public String getEstado() {
            return estado;
        }
    }

}
