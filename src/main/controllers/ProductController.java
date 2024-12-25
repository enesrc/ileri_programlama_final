package main.controllers;

import main.annotations.RequestMapping;
import main.services.AsyncService;
import main.models.Product;
import main.services.ProductService;
import main.services.CartService;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ProductController {
    private final ProductService productService;
    private final CartService cartService;
    private final AsyncService asyncService;

    public ProductController(AsyncService asyncService) {
        this.asyncService = asyncService;
        this.productService = new ProductService();
        this.cartService = new CartService();
    }

    @RequestMapping(method = "GET", path = "/explore")
    public void getExplorePage(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            try {
                String mainFilePath = "src/main/views/explore.html";
                String cardTemplatePath = "src/main/components/explore_card.html";

                // Ana sayfa HTML'ini yükle
                String htmlContent = loadHtmlFile(mainFilePath);

                // Ürün kartı şablonunu yükle
                String cardTemplate = loadHtmlFile(cardTemplatePath);

                // Veritabanından ürünleri çek
                List<Product> products = productService.getProducts();

                // Ürün HTML'lerini oluştur ve yerleştir
                StringBuilder productsHtml = new StringBuilder();
                for (Product product : products) {
                    String cardHtml = cardTemplate
                            .replace("{{id}}", String.valueOf(product.getId()))
                            .replace("{{name}}", product.getName())
                            .replace("{{price}}", String.valueOf(product.getPrice()))
                            .replace("{{stock}}", String.valueOf(product.getStock()));

                    productsHtml.append(cardHtml);
                }

                // Ana HTML'deki {{products}} yer tutucusunu değiştir
                htmlContent = htmlContent.replace("{{products}}", productsHtml.toString());

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

    @RequestMapping(method = "POST", path = "/explorepost")
    public void postExplore(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)) {

                // Formdan gelen verileri oku
                String query = reader.readLine();
                if (query == null || query.isEmpty()) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                // Product ID'sini al
                String productIdStr = null;
                String[] parameters = query.split("&");
                for (String param : parameters) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2 && "productId".equals(keyValue[0])) {
                        productIdStr = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        break;
                    }
                }

                if (productIdStr == null) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                // Kullanıcı ID'sini Cookie'den al
                String userIdStr = getUserIdFromCookie(exchange);

                if (userIdStr == null) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                // Verileri uygun tiplere çevir
                int productId = Integer.parseInt(productIdStr);
                int userId = Integer.parseInt(userIdStr);

                // Ürünü sepete ekle
                boolean isAdded = cartService.addProductToCart(userId, productId);

                if (isAdded) {
                    // Başarılı yönlendirme
                    String redirectUrl = "/user/explore";
                    exchange.getResponseHeaders().add("Location", redirectUrl);
                    exchange.sendResponseHeaders(302, -1);
                } else {
                    exchange.sendResponseHeaders(500, -1);
                }

            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
                try {
                    exchange.sendResponseHeaders(500, -1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @RequestMapping(method = "GET", path = "/about")
    public void getAboutPage(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            try {
                String mainFilePath = "src/main/views/about.html";

                // Ana sayfa HTML'ini yükle
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
