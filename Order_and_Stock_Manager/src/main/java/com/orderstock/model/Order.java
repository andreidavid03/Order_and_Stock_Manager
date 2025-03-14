package com.orderstock.model;

import java.util.List;

public class Order {
    private int id;
    private String clientName;
    private List<Product> products;
    private String status;

    public Order(int id, String clientName, List<Product> products, String status) {
        this.id = id;
        this.clientName = clientName;
        this.products = products;
        this.status = status;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
