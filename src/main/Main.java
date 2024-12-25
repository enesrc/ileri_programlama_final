package main;

import main.controllers.AuthController;
import main.controllers.ProductController;
import main.controllers.CartController;
import main.services.AsyncService;
import main.utils.ControllerRegistry;
import main.db.MySQLConnection;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        // Veritabanına bağlan
        MySQLConnection.connect();

        // Asenkron servis oluştur
        AsyncService asyncService = new AsyncService(16);

        // HttpServer oluştur
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Controller'ı kaydet
        AuthController authController = new AuthController(asyncService);
        ProductController productController = new ProductController(asyncService);
        CartController cartController = new CartController(asyncService);

        // Controller registry ile controller'ları kaydet
        ControllerRegistry registry = new ControllerRegistry();
        registry.registerController("/auth", authController);
        registry.registerController("/user", productController);
        registry.registerController("/user", cartController);

        // Tüm rotaları sunucuya yükle
        registry.configure(server);

        // Sunucuyu başlat
        server.start();
        System.out.println("Sunucu http://localhost:8080 adresinde çalışıyor");

        // Uygulama kapanırken thread havuzundaki thread'leri kapat
        Runtime.getRuntime().addShutdownHook(new Thread(asyncService::shutdown));
    }
}
