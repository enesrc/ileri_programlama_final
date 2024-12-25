package main.controllers;

import main.annotations.RequestMapping;
import main.services.AsyncService;
import main.models.Product;
import main.models.User;
import main.services.CartService;
import main.services.EmailService;
import main.services.ProductService;
import main.repositories.UserRepository;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CartController {
    private final AsyncService asyncService;
    private final CartService cartService;
    private final ProductService productService;
    private final EmailService emailService;

    public CartController(AsyncService asyncService) {
        this.cartService = new CartService();
        this.asyncService = asyncService;
        this.productService = new ProductService();
        this.emailService = new EmailService();
    }

    @RequestMapping(method = "GET", path = "/cart")
    public void getCart(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            try {
                String mainFilePath = "src/main/views/cart.html";
                String cardTemplatePath = "src/main/components/cart_card.html";

                // Ana sayfa HTML'ini yükle
                String htmlContent = loadHtmlFile(mainFilePath);

                // Ürün kartı şablonunu yükle
                String cardTemplate = loadHtmlFile(cardTemplatePath);

                int userId = Integer.parseInt(getUserIdFromCookie(exchange));

                // Veritabanından ürünleri çek
                List<Product> products = cartService.getCartByUserId(userId);


                // Ürün HTML'lerini oluştur ve yerleştir
                StringBuilder cartHtml = new StringBuilder();
                for (Product product : products) {
                    String cardHtml = cardTemplate
                            .replace("{{id}}", String.valueOf(product.getId()))
                            .replace("{{name}}", product.getName())
                            .replace("{{price}}", String.valueOf(product.getPrice()))
                            .replace("{{stock}}", String.valueOf(product.getStock()));

                    cartHtml.append(cardHtml);
                }

                if(products.isEmpty()) {
                    htmlContent = htmlContent.replace("{{products}}", "<h2> Sepetiniz boş</h2>");
                    htmlContent = htmlContent.replace("{{button}}", "");
                }
                else {
                    htmlContent = htmlContent.replace("{{products}}", cartHtml.toString());
                    htmlContent = htmlContent.replace("{{button}}", "<a href='/user/purchase' class='btn btn-success btn-lg'>SATIN AL</a>");
                }

                // HTTP yanıtını gönder
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlContent.getBytes(StandardCharsets.UTF_8).length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(htmlContent.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                System.out.println("Error loading file: " + e.getMessage());
            }
        });
    }

    @RequestMapping(method = "GET", path = "/purchase")
    public void purchase(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            try {
                // Kullanıcıyı tanımlamak için Cookie'den userId al
                String cookie = exchange.getRequestHeaders().getFirst("Cookie");
                String userId = cookie.split("userId=")[1].split(";")[0];

                // Kullanıcıyı ve sepet ürünlerini al
                User user = UserRepository.getUserById(Integer.parseInt(userId));
                List<Product> cartItems = cartService.getCartByUserId(Integer.parseInt(userId));

                if (cartItems.isEmpty()) {
                    exchange.sendResponseHeaders(400, -1); // Sepet boş
                    return;
                }

                // 1. Sepeti temizle
                cartService.clearCartByUserId(Integer.parseInt(userId));

                // 2. Stokları azalt
                for (Product product : cartItems) {
                    productService.decreaseStock(product.getId());
                }

                // 3. Email gönder
                String emailContent = generateEmailContent(cartItems);

                //emailService.sendEmail(user.getEmail(), "Satın Alma Başarılı", emailContent);
                emailService.sendEmailInNewThread(user.getEmail(), "Satın Alma Başarılı", emailContent);

                String redirectUrl = "http://localhost:8080/user/success-purchase";
                exchange.getResponseHeaders().add("Location", redirectUrl);
                exchange.sendResponseHeaders(302, -1);

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    exchange.sendResponseHeaders(500, -1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @RequestMapping(method = "GET", path = "/success-purchase")
    public void getSuccessPage(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            try {
                String mainFilePath = "src/main/views/success-purchase.html";

                String htmlContent = loadHtmlFile(mainFilePath);

                // HTTP yanıtını gönder
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlContent.getBytes(StandardCharsets.UTF_8).length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(htmlContent.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                System.out.println("Error loading file: " + e.getMessage());
            }
        });
    }

    private String generateEmailContent(List<Product> cartItems) {
        StringBuilder content = new StringBuilder("Satın alma işleminiz başarıyla gerçekleşti.\n\n Ürünler:\n");
        for (Product cartItem : cartItems) {
            Product product = productService.getProductById(cartItem.getId());
            content.append("- ").append(product.getName()).append("\n");
        }
        content.append("\nTeşekkür ederiz!");
        return content.toString();
    }

    private String getUserIdFromCookie(HttpExchange exchange) {
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            String[] cookieArray = cookies.split(";");
            for (String cookie : cookieArray) {
                String[] keyValue = cookie.trim().split("=", 2);
                if (keyValue.length == 2 && keyValue[0].equals("userId")) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    private String loadHtmlFile(String filePath) throws IOException {
        // Ana HTML dosyasını oku
        String htmlContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

        // Yer tutucu kontrolü ve değiştirme
        if (htmlContent.contains("{{navbar}}")) {
            String navbarFilePath = "src/main/components/navbar.html";
            String navbarContent = new String(Files.readAllBytes(Paths.get(navbarFilePath)), StandardCharsets.UTF_8);
            htmlContent = htmlContent.replace("{{navbar}}", navbarContent);
        }
        if (htmlContent.contains("{{footer}}")) {
            String footerFilePath = "src/main/components/footer.html";
            String footerContent = new String(Files.readAllBytes(Paths.get(footerFilePath)), StandardCharsets.UTF_8);
            htmlContent = htmlContent.replace("{{footer}}", footerContent);
        }

        return htmlContent;
    }
}
