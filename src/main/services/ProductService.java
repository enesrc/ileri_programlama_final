package main.services;

import main.models.Product;
import main.repositories.ProductRepository;

import java.util.List;

public class ProductService {
    private final ProductRepository repository;

    public ProductService() {
        this.repository = new ProductRepository();
    }

    public List<Product> getProducts() {
        return repository.getProducts();
    }

    public Product getProductById(int productId) {
        return repository.getProductById(productId);
    }

    public boolean decreaseStock(int productId) {
        return repository.decreaseStock(productId);
    }
}
