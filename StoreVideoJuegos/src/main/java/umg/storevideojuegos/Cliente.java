package umg.storevideojuegos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Cliente {

    private int id;
    private String nombre;
    private String nit;
    private double saldo;

    // Getters y Setters para los campos
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

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    // Método para crear un nuevo cliente en la base de datos
    public static void crearClienteDesdeUsuario(String url, String usuario, String contraseña) {
        Scanner scanner = new Scanner(System.in);
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            System.out.print("Nombre del nuevo cliente: ");
            String nombre = scanner.nextLine();
            System.out.print("NIT del nuevo cliente: ");
            String nit = scanner.nextLine();

            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "INSERT INTO clientes (nombre, nit) VALUES (?, ?)";
            consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            consulta.setString(1, nombre);
            consulta.setString(2, nit);
            consulta.executeUpdate();

            ResultSet generatedKeys = consulta.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                System.out.println("Cliente creado exitosamente con ID: " + id);
                // Quita la siguiente línea que intenta leer una entrada adicional
            } else {
                System.out.println("Error al obtener el ID del cliente creado.");
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

    // Método para mostrar la información de un cliente por su ID
    public void mostrarInformacionCliente(String url, String usuario, String contraseña) {
        Scanner scanner = new Scanner(System.in);
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            int idCliente;
            while (true) {
                System.out.print("ID del cliente: ");
                if (scanner.hasNextInt()) {
                    idCliente = scanner.nextInt();
                    break;
                } else {
                    System.out.println("Por favor, ingrese un número válido.");
                    scanner.nextLine(); // Consumir entrada no válida
                }
            }
            scanner.nextLine(); // Consumir línea en blanco

            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "SELECT nombre, nit FROM clientes WHERE id = ?";
            consulta = conexion.prepareStatement(sql);
            consulta.setInt(1, idCliente);

            ResultSet result = consulta.executeQuery();

            if (result.next()) {
                String nombre = result.getString("nombre");
                String nit = result.getString("nit");
                System.out.println("Información del cliente (ID: " + idCliente + "):");
                System.out.println("Nombre: " + nombre);
                System.out.println("NIT: " + nit);
            } else {
                System.out.println("Cliente con ID " + idCliente + " no encontrado.");
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

    // Método para modificar los datos de un cliente existente en la base de datos
    public void modificarCliente(String url, String usuario, String contraseña) {
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "UPDATE clientes SET nombre = ?, nit = ? WHERE id = ?";
            consulta = conexion.prepareStatement(sql);
            consulta.setString(1, this.nombre);
            consulta.setString(2, this.nit);
            consulta.setInt(3, this.id);  // No necesitas el campo "saldo" en esta actualización
            consulta.executeUpdate();
            System.out.println("Cliente modificado exitosamente.");
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

    // Método para eliminar un cliente de la base de datos por su ID
    public static void eliminarClientePorID(int clienteID, String url, String usuario, String contraseña) {
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "DELETE FROM clientes WHERE id = ?";
            consulta = conexion.prepareStatement(sql);
            consulta.setInt(1, clienteID);
            consulta.executeUpdate();
            System.out.println("Cliente eliminado exitosamente.");
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

    public void mostrarTodosLosClientes(String url, String usuario, String contraseña) {
        Connection conexion = null;
        PreparedStatement consulta = null;

        try {
            conexion = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "SELECT id, nombre, nit FROM clientes";
            consulta = conexion.prepareStatement(sql);

            ResultSet result = consulta.executeQuery();

            while (result.next()) {
                int idCliente = result.getInt("id");
                String nombre = result.getString("nombre");
                String nit = result.getString("nit");
                System.out.println("Información del cliente (ID: " + idCliente + "):");
                System.out.println("Nombre: " + nombre);
                System.out.println("NIT: " + nit);
                System.out.println("-------------");
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

    public Cliente obtenerClientePorID(int idCliente, String url, String usuario, String contraseña) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    Cliente cliente = null;

    try {
        // Establecer conexión
        connection = DriverManager.getConnection(url, usuario, contraseña);

        // Consulta SQL para obtener el cliente por ID
        String sql = "SELECT * FROM clientes WHERE id = ?";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, idCliente);

        // Ejecutar la consulta
        resultSet = preparedStatement.executeQuery();

        // Verificar si se encontró el cliente
        if (resultSet.next()) {
            // Crear un objeto Cliente con los datos de la base de datos
            cliente = new Cliente();
            cliente.setId(resultSet.getInt("id"));
            cliente.setNombre(resultSet.getString("nombre"));
            cliente.setNit(resultSet.getString("nit"));
            // Otros campos...

            // Puedes agregar más campos según tu esquema de base de datos
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        // No cierres la conexión aquí para que el objeto Cliente siga siendo utilizable
        // La conexión se cerrará en el lugar que llama a este método
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    return cliente;
}

}
