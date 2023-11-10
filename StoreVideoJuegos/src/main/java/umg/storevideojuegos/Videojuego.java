package umg.storevideojuegos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Videojuego {

    private int id;
    private String nombre;
    private double precio;
    private String consola;
    private int cantidadDisponible;

    public Videojuego() {
        // Constructor vacío
    }

    @Override
    public String toString() {
        return "Videojuego [id=" + id + ", nombre=" + nombre + ", precio=" + precio + ", consola=" + consola
                + ", cantidadDisponible=" + cantidadDisponible + "]";
    }

    public Videojuego(int id, String nombre, double precio, String consola, int cantidadDisponible) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.consola = consola;
        this.cantidadDisponible = cantidadDisponible;
    }

    // Getters y setters para los campos
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

    // Método para crear un nuevo videojuego en la base de datos
    public static void crearVideojuegoDesdeUsuario(String url, String usuario, String contraseña) {
        Scanner scanner = new Scanner(System.in);
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            System.out.print("Nombre del nuevo videojuego: ");
            String nombre = scanner.nextLine();
            System.out.print("Precio del nuevo videojuego: ");
            double precio = Double.parseDouble(scanner.nextLine());
            System.out.print("Consola del nuevo videojuego: ");
            String consola = scanner.nextLine();
            System.out.print("Cantidad disponible del nuevo videojuego: ");
            int cantidadDisponible = Integer.parseInt(scanner.nextLine());

            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "INSERT INTO videojuegos (nombre, precio, consola, cantidad_disponible) VALUES (?, ?, ?, ?)";
            consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            consulta.setString(1, nombre);
            consulta.setDouble(2, precio);
            consulta.setString(3, consola);
            consulta.setInt(4, cantidadDisponible);
            consulta.executeUpdate();

            ResultSet generatedKeys = consulta.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                System.out.println("Videojuego creado exitosamente con ID: " + id);

            } else {
                System.out.println("Error al obtener el ID del videojuego creado.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (consulta != null) {
                    consulta.close();
                }
                if (conexion != null) {
                    conexion.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para mostrar la información de un videojuego por su ID
    public void mostrarInformacionVideojuego(String url, String usuario, String contraseña) {
        Scanner scanner = new Scanner(System.in);
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            int idVideojuego;
            while (true) {
                System.out.print("ID del videojuego: ");
                if (scanner.hasNextInt()) {
                    idVideojuego = scanner.nextInt();
                    break;
                } else {
                    System.out.println("Por favor, ingrese un número válido.");
                    scanner.nextLine(); // Consumir entrada no válida
                }
            }
            scanner.nextLine(); // Consumir línea en blanco

            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "SELECT nombre, precio, consola, cantidad_disponible FROM videojuegos WHERE id = ?";
            consulta = conexion.prepareStatement(sql);
            consulta.setInt(1, idVideojuego);

            ResultSet result = consulta.executeQuery();

            if (result.next()) {
                String nombre = result.getString("nombre");
                double precio = result.getDouble("precio");
                String consola = result.getString("consola");
                int cantidadDisponible = result.getInt("cantidad_disponible");

                System.out.println("Información del videojuego (ID: " + idVideojuego + "):");
                System.out.println("Nombre: " + nombre);
                System.out.println("Precio: " + precio);
                System.out.println("Consola: " + consola);
                System.out.println("Cantidad Disponible: " + cantidadDisponible);
            } else {
                System.out.println("Videojuego con ID " + idVideojuego + " no encontrado.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (consulta != null) {
                    consulta.close();
                }
                if (conexion != null) {
                    conexion.close();
                }
                scanner.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para modificar los datos de un videojuego existente en la base de datos
    public void modificarVideojuego(String url, String usuario, String contraseña) {
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "UPDATE videojuegos SET nombre = ?, precio = ?, consola = ?, cantidad_disponible = ? WHERE id = ?";
            consulta = conexion.prepareStatement(sql);
            consulta.setString(1, this.nombre);
            consulta.setDouble(2, this.precio);
            consulta.setString(3, this.consola);
            consulta.setInt(4, this.cantidadDisponible);
            consulta.setInt(5, this.id);
            consulta.executeUpdate();
            System.out.println("Videojuego modificado exitosamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (consulta != null) {
                    consulta.close();
                }
                if (conexion != null) {
                    conexion.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para eliminar un videojuego de la base de datos por su ID
    public static void eliminarVideojuegoPorID(int videojuegoID, String url, String usuario, String contraseña) {
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "DELETE FROM videojuegos WHERE id = ?";
            consulta = conexion.prepareStatement(sql);
            consulta.setInt(1, videojuegoID);
            consulta.executeUpdate();
            System.out.println("Videojuego eliminado exitosamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (consulta != null) {
                    consulta.close();
                }
                if (conexion != null) {
                    conexion.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para mostrar todos los videojuegos en la base de datos
    public static void mostrarTodosLosVideojuegos(String url, String usuario, String contraseña) {
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "SELECT id, nombre, precio, consola, cantidad_disponible FROM videojuegos";
            consulta = conexion.prepareStatement(sql);
            ResultSet result = consulta.executeQuery();

            System.out.println("Listado de Videojuegos:");
            while (result.next()) {
                int id = result.getInt("id");
                String nombre = result.getString("nombre");
                double precio = result.getDouble("precio");
                String consola = result.getString("consola");
                int cantidadDisponible = result.getInt("cantidad_disponible");

                System.out.println("ID: " + id + " - Nombre: " + nombre + " - Precio: " + precio + " - Consola: " + consola + " - Cantidad Disponible: " + cantidadDisponible);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (consulta != null) {
                    consulta.close();
                }
                if (conexion != null) {
                    conexion.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para obtener un Videojuego por su ID
    public static Videojuego obtenerVideojuegoPorId(Connection conexion, int idVideojuego) {
        Videojuego videojuego = null;
        String sql = "SELECT * FROM Videojuegos WHERE id = ?";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, idVideojuego);
            ResultSet result = statement.executeQuery();
            
            if (result.next()) {
                int id = result.getInt("id");
                String nombre = result.getString("nombre");
                double precio = result.getDouble("precio");
                String consola = result.getString("consola");
                int cantidadDisponible = result.getInt("cantidad_disponible");
                
                // Puedes ajustar los parámetros según tu esquema de base de datos
                videojuego = new Videojuego(id, nombre, precio, consola, cantidadDisponible);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return videojuego; // Agregamos esta línea
    }
    public Videojuego obtenerVideojuegoPorID(int idVideojuego, String url, String usuario, String contraseña) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Videojuego videojuego = null;

        try {
            // Establecer conexión
            connection = DriverManager.getConnection(url, usuario, contraseña);

            // Consulta SQL para obtener el videojuego por ID
            String sql = "SELECT * FROM videojuegos WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, idVideojuego);

            // Ejecutar la consulta
            resultSet = preparedStatement.executeQuery();

            // Verificar si se encontró el videojuego
            if (resultSet.next()) {
                // Crear un objeto Videojuego con los datos de la base de datos
                videojuego = new Videojuego();
                videojuego.setId(resultSet.getInt("id"));
                videojuego.setNombre(resultSet.getString("nombre"));
                videojuego.setPrecio(resultSet.getDouble("precio"));
                videojuego.setConsola(resultSet.getString("consola"));
                videojuego.setCantidadDisponible(resultSet.getInt("cantidad_disponible"));
                // Otros campos...

                // Puedes agregar más campos según tu esquema de base de datos
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return videojuego;
    }
}


