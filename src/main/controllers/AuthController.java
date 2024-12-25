package main.controllers;

import com.sun.net.httpserver.HttpExchange;
import main.annotations.RequestMapping;
import main.models.User;
import main.repositories.UserRepository;
import main.services.AsyncService;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

public class AuthController {

    private final AsyncService asyncService;

    public AuthController(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    @RequestMapping(method = "GET", path = "/register")
    public void getRegisterAsync(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            try {
                String filePath = "src/main/views/register.html";
                String htmlContent = loadHtmlFile(filePath);

                // Parametreleri kontrol et
                Map<String, String> params = new HashMap<>();
                String registerError = exchange.getRequestURI().getQuery() != null && exchange.getRequestURI().getQuery().contains("error=true") ? "true" : "false";

                params.put("registerErrorVisibility", registerError.equals("true") ? "block" : "none");

                htmlContent = replacePlaceholders(htmlContent, params);

                // HTTP yanıt başlıklarını ayarla
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlContent.getBytes(StandardCharsets.UTF_8).length);

                // Yanıt gövdesini yaz
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(htmlContent.getBytes(StandardCharsets.UTF_8));
                }
            } catch (FileNotFoundException e) {
                handleNotFound(exchange);
            } catch (IOException e) {
                handleServerError(exchange);
            }
        });
    }

    @RequestMapping(method = "POST", path = "/registerpost")
    public void postRegister(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)) {

                String query = reader.readLine(); // Formdan gelen veriler
                String[] parameters = query.split("&");
                String name = URLDecoder.decode(parameters[0].split("=")[1], StandardCharsets.UTF_8);
                String email = URLDecoder.decode(parameters[1].split("=")[1], StandardCharsets.UTF_8);
                String password = URLDecoder.decode(parameters[2].split("=")[1], StandardCharsets.UTF_8);

                // Veritabanında kullanıcıyı doğrula
                boolean isThereUser = UserRepository.isThereUserByEmail(email); // Kullanıcıyı var mı email ile bul

                if (!isThereUser) {
                    User newUser = new User(name, email, password);
                    UserRepository.saveUser(newUser);

                    String redirectUrl = "http://localhost:8080/auth/login?signup=true";
                    exchange.getResponseHeaders().add("Location", redirectUrl);
                    exchange.sendResponseHeaders(302, -1);
                } else {
                    String redirectUrl = "http://localhost:8080/auth/register?error=true";
                    exchange.getResponseHeaders().add("Location", redirectUrl);
                    exchange.sendResponseHeaders(302, -1);
                }

            } catch (IOException | SQLException e) {
                e.printStackTrace();
                String response = "An error occurred during login";
                try {
                    exchange.sendResponseHeaders(500, response.getBytes(StandardCharsets.UTF_8).length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @RequestMapping(method = "GET", path = "/login")
    public void getLoginAsync(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            System.out.println("asenkron istek, " + Thread.currentThread().getName());
            login(exchange);
        });
    }

    @RequestMapping(method = "GET", path = "/login-sync")
    public void getLoginSync(HttpExchange exchange) {
        System.out.println("senkron istek");
        login(exchange);
    }

    @RequestMapping(method = "POST", path = "/loginpost")
    public void postLogin(HttpExchange exchange) {
        asyncService.executeAsync(() -> {
            try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)) {

                String query = reader.readLine(); // Formdan gelen veriler
                String[] parameters = query.split("&");
                String email = URLDecoder.decode(parameters[0].split("=")[1], StandardCharsets.UTF_8);
                String password = URLDecoder.decode(parameters[1].split("=")[1], StandardCharsets.UTF_8);

                User user = UserRepository.getUserByEmail(email); // Kullanıcıyı email ile bul

                if (user != null && user.getPassword().equals(password)) {
                    String cookie = "userId=" + user.getId() + "; Path=/; HttpOnly";
                    exchange.getResponseHeaders().add("Set-Cookie", cookie);

                    String redirectUrl = "http://localhost:8080/user/explore";
                    exchange.getResponseHeaders().add("Location", redirectUrl);
                    exchange.sendResponseHeaders(302, -1);
                } else {
                    String redirectUrl = "http://localhost:8080/auth/login?error=true";
                    exchange.getResponseHeaders().add("Location", redirectUrl);
                    exchange.sendResponseHeaders(302, -1);
                }

            } catch (IOException | SQLException e) {
                e.printStackTrace();
                String response = "An error occurred during login";
                try {
                    exchange.sendResponseHeaders(500, response.getBytes(StandardCharsets.UTF_8).length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }



    private void login(HttpExchange exchange) {
        try {
            Thread.sleep(0);

            String filePath = "src/main/views/login.html";
            String htmlContent = loadHtmlFile(filePath);

            // Parametreleri kontrol et
            Map<String, String> params = new HashMap<>();
            String loginError = exchange.getRequestURI().getQuery() != null && exchange.getRequestURI().getQuery().contains("error=true") ? "true" : "false";
            String registerSuccess = exchange.getRequestURI().getQuery() != null && exchange.getRequestURI().getQuery().contains("signup=true") ? "true" : "false";

            params.put("loginErrorVisibility", loginError.equals("true") ? "block" : "none");
            params.put("registerSuccessVisibility", registerSuccess.equals("true") ? "block" : "none");

            htmlContent = replacePlaceholders(htmlContent, params);

            // HTTP yanıt başlıklarını ayarla
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlContent.getBytes(StandardCharsets.UTF_8).length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(htmlContent.getBytes(StandardCharsets.UTF_8));
            }
        } catch (FileNotFoundException e) {
            handleNotFound(exchange);
        } catch (IOException e) {
            handleServerError(exchange);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String loadHtmlFile(String filePath) throws IOException {
        // Dosyayı oku
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        return contentBuilder.toString();
    }

    private String replacePlaceholders(String htmlContent, Map<String, String> params) {
        // Bu basit işlev, yer tutucuları HTML içeriğinde verilen parametrelerle değiştirir
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue();
            htmlContent = htmlContent.replace(placeholder, value);
        }
        return htmlContent;
    }

    private void handleNotFound(HttpExchange exchange) {
        try {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
        }
    }

    private void handleServerError(HttpExchange exchange) {
        try {
            String response = "500 Internal Server Error";
            exchange.sendResponseHeaders(500, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
        }
    }

}
