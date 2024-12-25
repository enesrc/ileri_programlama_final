package main.repositories;

import main.db.MySQLConnection;
import main.models.User;

import java.sql.*;

public class UserRepository {

    public static void saveUser(User user) throws SQLException {
        String query = "INSERT INTO user (name, email, password) VALUES (?, ?, ?)";
        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("User successfully saved!");
            } else {
                System.out.println("Failed to save the user.");
            }
        }
    }

    public static User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";
        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                return new User(id, name, email, password);
            } else {
                return null;
            }
        }
    }

    public static User getUserById(int id) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ?";
        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                return new User(id, name, email, password);
            } else {
                return null;
            }
        }
    }

    public static boolean isThereUserByEmail(String email) throws SQLException {
        String query = "SELECT email FROM user WHERE email = ?";
        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);

            System.out.println(statement);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {

                return true;
            } else {
                return false;
            }
        }
    }
}
