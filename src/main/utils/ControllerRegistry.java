package main.utils;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import main.annotations.RequestMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
interface ContextHandler {
    HttpContext apply(HttpServer server);
}

public class ControllerRegistry {

    private final Map<String, ContextHandler> routes = new HashMap<>();

    public void registerController(String basePath, Object controller) {
        Method[] methods = controller.getClass().getDeclaredMethods();

        for (Method method : methods) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                String requestMethod = mapping.method();
                String route = basePath + mapping.path();

                routes.put(route + ":" + requestMethod, server -> {
                    HttpContext context = server.createContext(route, exchange -> {
                        if (exchange.getRequestMethod().equalsIgnoreCase(requestMethod)) {
                            try {
                                method.setAccessible(true);
                                method.invoke(controller, exchange);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                                exchange.sendResponseHeaders(500, -1); // Internal Server Error
                            }
                        } else {
                            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                        }
                    });
                    return context;
                });
            }
        }
    }

    public void configure(HttpServer server) {
        //routes.forEach((route, handler) -> System.out.println(route + " " + handler));
        routes.forEach((route, handler) -> handler.apply(server));
    }
}
