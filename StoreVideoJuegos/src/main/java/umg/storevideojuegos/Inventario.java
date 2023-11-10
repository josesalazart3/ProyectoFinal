package umg.storevideojuegos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Inventario {

    private Connection conexion;
    private int id;
    private String nombre;
    private double precio;
    private String consola;
    private int cantidadDisponible;

    // Constructor
    public Inventario(int id, String nombre, double precio, String consola, int cantidadDisponible) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.consola = consola;
        this.cantidadDisponible = cantidadDisponible;
    }

    public Inventario(Connection conexion) {
        this.conexion = conexion;
    }

    // Métodos getter y setter para los campos de la clase
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getConsola() {
        return consola;
    }

    public void setConsola(String consola) {
        this.consola = consola;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    // Método privado para establecer la conexión a la base de datos
    private Connection establecerConexion(ConfiguracionBaseDatos configuracion) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(configuracion.getUrl(), configuracion.getUsuario(),
                    configuracion.getContraseña());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    // Método para eliminar un videojuego del inventario
    public void eliminarVideojuegoDelInventario(int inventarioId) {
        String sql = "DELETE FROM inventario WHERE id = ?";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, inventarioId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para encontrar videojuegos por consola
    public List<Inventario> encontrarVideojuegosPorConsola(String consola) {
        List<Inventario> inventarios = new ArrayList<>();
        String sql = "SELECT id, nombre, precio, consola, cantidad FROM inventario WHERE consola = ?";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, consola);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int id = result.getInt("id");
                String nombre = result.getString("nombre");
                double precio = result.getDouble("precio");
                int cantidadDisponible = result.getInt("cantidad");

                // Crear un objeto Inventario y agregarlo a la lista
                Inventario inventario = new Inventario(id, nombre, precio, consola, cantidadDisponible);

                inventario.setId(id);
                inventario.setNombre(nombre);
                inventario.setPrecio(precio);
                inventario.setConsola(consola);
                inventario.setCantidadDisponible(cantidadDisponible);

                inventarios.add(inventario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventarios;
    }

    // Método para obtener el total de productos en el inventario
    public int obtenerTotalProductosEnInventario() {
        String sql = "SELECT COUNT(*) AS total FROM inventario";
        int total = 0;
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                total = result.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    // Método para agregar un videojuego al inventario
    public void agregarVideojuego(Videojuego videojuego) {
        // Verificar si el videojuego ya existe en el inventario
        boolean existeVideojuego = verificarExistenciaVideojuego(videojuego.getNombre());

        if (existeVideojuego) {

            System.out.println("El videojuego ya existe en el inventario.");
            System.out.println("¿Desea agregar existencias y establecer un nuevo precio? (S/N)");

            Scanner scanner = new Scanner(System.in);
            String respuesta = scanner.nextLine();

            if (respuesta.equalsIgnoreCase("S")) {
                // Pedir al usuario la nueva cantidad y precio
                System.out.println("Ingrese la nueva cantidad disponible:");
                int nuevaCantidad = scanner.nextInt();
                System.out.println("Ingrese el nuevo precio:");
                double nuevoPrecio = scanner.nextDouble();

                // Actualizar existencias y precio del videojuego existente
                actualizarVideojuegoExistente(videojuego.getNombre(), nuevaCantidad, nuevoPrecio);
                System.out.println("Existencias y precio del videojuego actualizados.");
            } else {
                System.out.println("No se realizaron modificaciones en el inventario.");
            }
        } else {
            // El videojuego no existe, agregarlo al inventario
            String sql = "INSERT INTO inventario (nombre, precio, consola, cantidad) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setString(1, videojuego.getNombre());
                statement.setDouble(2, videojuego.getPrecio());
                statement.setString(3, videojuego.getConsola());
                statement.setInt(4, videojuego.getCantidadDisponible());
                statement.executeUpdate();
                System.out.println("Videojuego agregado al inventario.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

// Método para verificar si un videojuego ya existe en el inventario
    private boolean verificarExistenciaVideojuego(String nombreVideojuego) {
        String sql = "SELECT COUNT(*) AS count FROM inventario WHERE nombre = ?";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, nombreVideojuego);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

// Método para actualizar existencias y precio de un videojuego existente
    private void actualizarVideojuegoExistente(String nombreVideojuego, int nuevaCantidad, double nuevoPrecio) {
        String sql = "UPDATE inventario SET cantidad = ?, precio = ? WHERE nombre = ?";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, nuevaCantidad);
            statement.setDouble(2, nuevoPrecio);
            statement.setString(3, nombreVideojuego);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

// En la clase Inventario
    public List<Inventario> mostrarInventario() {
        List<Inventario> inventarios = new ArrayList<>();
        String sql = "SELECT id, nombre, precio, consola, cantidad FROM inventario";

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int id = result.getInt("id");
                String nombre = result.getString("nombre");
                double precio = result.getDouble("precio");
                String consola = result.getString("consola");
                int cantidadDisponible = result.getInt("cantidad");

                Inventario inventario = new Inventario(id, nombre, precio, consola, cantidadDisponible);
                inventarios.add(inventario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return inventarios;
    }

    public static Inventario obtenerProductoPorID(int id, String url, String usuario, String contraseña) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Inventario producto = null;

        try {
            // Establecer la conexión
            connection = DriverManager.getConnection(url, usuario, contraseña);

            // Consulta SQL para obtener el producto por ID
            String sql = "SELECT * FROM Inventario WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            // Ejecutar la consulta
            resultSet = preparedStatement.executeQuery();

            // Verificar si se encontró el producto
            if (resultSet.next()) {
                // Crear un objeto Inventario con los datos del resultado
                producto = new Inventario(
                        resultSet.getInt("id"),
                        resultSet.getString("nombre"),
                        resultSet.getDouble("precio"),
                        resultSet.getString("consola"),
                        resultSet.getInt("cantidad_disponible")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return producto;
    }

    // Método para cerrar la conexión a la base de datos
    public void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
