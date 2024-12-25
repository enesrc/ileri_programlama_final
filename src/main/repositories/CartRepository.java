package main.repositories;

import main.models.Cart;
import main.models.Product;
import main.db.MySQLConnection;
import main.services.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class CartRepository {
    ProductService productService = new ProductService();

    // Kullanıcı ID'sine göre sepeti ve ürünleri döndürür
    public List<Product> getCartByUserId(int userId) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT product_id FROM cart WHERE user_id = ?";

        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int productId = resultSet.getInt("product_id");

                    // Her bir product_id'yi kullanarak ProductService ile ürün bilgilerini alıyoruz
                    Product product = productService.getProductById(productId);
                    if (product != null) {
                        products.add(product);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Sepet ürünleri çekilirken hata oluştu: " + e.getMessage());
        }

        return products;
    }

    // Kullanıcı sepetine ürün ekle
    public boolean addProductToCart(int userId, int productId) {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String query = "INSERT INTO cart (user_id, product_id) VALUES (?, ?)";

        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setInt(2, productId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Ürün sepete eklenirken hata oluştu: " + e.getMessage());
        }

        return false;
    }

    // Kullanıcı sepetinden ürün sil
    public boolean removeProductFromCart(int userId, int productId) {
        String query = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";

        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setInt(2, productId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Ürün sepetten silinirken hata oluştu: " + e.getMessage());
        }

        return false;
    }

    // Kullanıcının sepetini temizle
    public boolean clearCart(int userId) {
        String query = "DELETE FROM cart WHERE user_id = ?";

        try (Connection connection = MySQLConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Sepet temizlenirken hata oluştu: " + e.getMessage());
        }

        return false;
    }
}
