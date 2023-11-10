package umg.storevideojuegos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConfiguracionBaseDatos {
    private String url = "jdbc:postgresql://localhost:5432/postgres";
    private String usuario = "postgres";
    private String contraseña = "1234";

    public ConfiguracionBaseDatos(String url, String usuario, String contraseña) {
        this.url = url;
        this.usuario = usuario;
        this.contraseña = contraseña;
    }

    ConfiguracionBaseDatos() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public String getUrl() {
        return url;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, contraseña);
    }
}

