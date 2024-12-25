package main.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {
    public static Connection connect() {
        // Bağlantı URL'si
        String url = "jdbc:mysql://localhost:3306/your_db_name";
        String user = "your_username";
        String password = "your_password";

        // Bağlantı objesi
        Connection connection = null;

        try {
            // JDBC sürücüsünü yükleyin
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Veritabanına bağlanın
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Bağlantı hatası: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Sürücüsü bulunamadı: " + e.getMessage());
        }

        return connection;
    }

    public static void main(String[] args) {
        connect();
    }
}

