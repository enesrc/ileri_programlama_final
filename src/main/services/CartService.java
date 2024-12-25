package main.services;

import main.models.Cart;
import main.models.Product;
import main.repositories.CartRepository;
import java.util.List;

public class CartService {

    // CartRepository'yi kullanarak sepete ürün ekleme, silme ve görüntüleme işlemleri yapacağız.
    private CartRepository cartRepository = new CartRepository();

    // Kullanıcının sepetindeki ürünleri döndüren fonksiyon
    public List<Product> getCartByUserId(int userId) {
        return cartRepository.getCartByUserId(userId);
    }

    // Kullanıcı sepetine ürün ekler
    public boolean addProductToCart(int userId, int productId) {
        new Thread(() -> {
            cartRepository.addProductToCart(userId, productId);
        }).start();
        return true;
    }

    // Kullanıcı sepetinden ürün siler
    public void removeProductFromCart(int userId, int productId) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cartRepository.removeProductFromCart(userId, productId);
        }).start();
    }

    // Kullanıcının sepetini temizler
    public void clearCartByUserId(int userId) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cartRepository.clearCart(userId);
        }).start();
    }
}
