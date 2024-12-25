package main.models;

public class Cart {
    private int id;
    private int userId;
    private int productId;

    public Cart(int id, int userId, int productId) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }
}
