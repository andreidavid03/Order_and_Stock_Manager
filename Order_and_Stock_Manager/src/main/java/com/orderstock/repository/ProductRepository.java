package com.orderstock.repository;

import com.orderstock.database.DatabaseConnector;
import com.orderstock.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public void deleteAllProducts() {
        String sql = "DELETE FROM products";
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("All products deleted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addOrUpdateProduct(Product product) {
        if (productExists(product.getName())) {
            updateProductQuantity(product);
            System.out.println("Existing product updated: " + product.getName());
        } else {
            insertProduct(product);
            System.out.println("New product added: " + product.getName());
        }
    }

    private void insertProduct(Product product) {
        String sql = "INSERT INTO products (name, quantity, price) VALUES (?, ?, ?)";
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getStock());
            stmt.setDouble(3, product.getPrice());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateProductQuantity(Product product) {

        String sql = "UPDATE products SET quantity = quantity + ? WHERE name = ?";
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getStock());
            stmt.setString(2, product.getName());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean productExists(String productName) {
        String sql = "SELECT COUNT(*) FROM products WHERE name = ?";
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public Product getProductByName(String productName) {
        String sql = "SELECT * FROM products WHERE name = ?";
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}