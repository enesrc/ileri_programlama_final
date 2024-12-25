package main.repositories;

import main.models.Product;
import main.db.MySQLConnection;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class ProductRepository {

    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM product";

        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                int stock = resultSet.getInt("stock");

                Product product = new Product(id, name, price, stock);
                products.add(product);
            }

        } catch (SQLException e) {
            System.out.println("Ürünler çekilirken hata oluştu: " + e.getMessage());
        }

        return products;
    }

    public Product getProductById(int productId) {
        Product product = null;
        String query = "SELECT * FROM product WHERE id = ?";

        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    double price = resultSet.getDouble("price");
                    int stock = resultSet.getInt("stock");

                    product = new Product(id, name, price, stock);
                }
            }

        } catch (SQLException e) {
            System.out.println("Ürün bilgileri çekilirken hata oluştu: " + e.getMessage());
        }

        return product;
    }

    public boolean decreaseStock(int productId) {
        String query = "UPDATE product SET stock = stock - 1 WHERE id = ? AND stock > 0";

        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Parametreleri ayarla
            statement.setInt(1, productId);

            // Güncelleme işlemini gerçekleştir
            int rowsUpdated = statement.executeUpdate();

            // Eğer bir satır güncellenmişse stok başarıyla azaltılmıştır
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.out.println("Stok azaltılırken hata oluştu: " + e.getMessage());
            return false;
        }
    }

}
