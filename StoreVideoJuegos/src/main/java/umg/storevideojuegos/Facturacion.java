package umg.storevideojuegos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Facturacion {

    public static void generarFactura(Connection connection, int ventaId, String metodoPago, String direccionEnvio) {
        try {
            // Obtener el último número de factura y actualizar la secuencia
            int ultimoNumeroFactura = obtenerYActualizarSecuencia(connection);

            // Incrementar el número de factura
            int nuevoNumeroFactura = ultimoNumeroFactura + 1;

            // Actualizar la secuencia
            actualizarSecuencia(connection, nuevoNumeroFactura);

            // Obtener la fecha actual
            Date fechaFacturacion = new Date();

            // Formatear el número de factura
            String numeroFactura = formatearNumeroFactura(nuevoNumeroFactura);

            String query = "INSERT INTO public.facturacion (venta_id, numero_factura, fecha_facturacion, metodo_pago, direccion_envio) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, ventaId);
                preparedStatement.setString(2, numeroFactura);
                preparedStatement.setDate(3, new java.sql.Date(fechaFacturacion.getTime()));
                preparedStatement.setString(4, metodoPago);
                preparedStatement.setString(5, direccionEnvio);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("La inserción en la tabla facturacion falló, no se generó ninguna fila.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // O manejo de la excepción según tus necesidades
        }
    }

    private static int obtenerYActualizarSecuencia(Connection connection) throws SQLException {
        String selectQuery = "SELECT ultimo_numero_factura FROM public.secuencia_factura FOR UPDATE";
        String updateQuery = "UPDATE public.secuencia_factura SET ultimo_numero_factura = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = selectStatement.executeQuery()) {
            if (resultSet.next()) {
                int ultimoNumeroFactura = resultSet.getInt("ultimo_numero_factura");

                // Actualizar la secuencia
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setInt(1, ultimoNumeroFactura + 1);
                    updateStatement.executeUpdate();
                }

                return ultimoNumeroFactura;
            } else {
                throw new SQLException("No se pudo obtener la secuencia de facturación.");
            }
        }
    }

    private static void actualizarSecuencia(Connection connection, int nuevoNumeroFactura) throws SQLException {
        String updateQuery = "UPDATE public.secuencia_factura SET ultimo_numero_factura = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setInt(1, nuevoNumeroFactura);
            updateStatement.executeUpdate();
        }
    }

    private static String formatearNumeroFactura(int numeroFactura) {
        // Puedes ajustar el formato según tus necesidades
        return String.format("%06d", numeroFactura);
    }
}
