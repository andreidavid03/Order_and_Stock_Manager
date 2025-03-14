package com.orderstock.repository;

import com.orderstock.database.DatabaseConnector;
import com.orderstock.model.Order;
import com.orderstock.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class OrderRepository {

    public void addOrder(Order order) {
        String sql = "INSERT INTO orders (client_name, status) VALUES (?, ?)";
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, order.getClientName());
            stmt.setString(2, order.getStatus());
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int orderId = generatedKeys.getInt(1);
                addOrderProducts(orderId, order.getProducts());
                System.out.println("Order added successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addOrderProducts(int orderId, List<Product> products) {
        String sql = "INSERT INTO order_products (order_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Product p : products) {
                int productId = getProductIdByName(p.getName(), conn);
                stmt.setInt(1, orderId);
                stmt.setInt(2, productId);
                stmt.setInt(3, p.getStock());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getProductIdByName(String productName, Connection conn) {
        String sql = "SELECT id FROM products WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateOrder(Order order) {
        String sql = "UPDATE orders SET client_name = ?, status = ? WHERE id = ?";
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, order.getClientName());
            stmt.setString(2, order.getStatus());
            stmt.setInt(3, order.getId());
            stmt.executeUpdate();
            System.out.println("Order updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Order> getAllOrdersWithProducts() {
        String sql = "SELECT o.id AS order_id, o.client_name AS customer, o.status, "
                + "p.id AS product_id, p.name AS product_name, p.quantity AS product_stock, p.price AS product_price, "
                + "op.quantity AS ordered_quantity "
                + "FROM orders o "
                + "LEFT JOIN order_products op ON o.id = op.order_id "
                + "LEFT JOIN products p ON op.product_id = p.id "
                + "ORDER BY o.id";

        List<Order> orders = new ArrayList<>();
        Map<Integer, Order> orderMap = new HashMap<>();

        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                Order order = orderMap.get(orderId);
                if (order == null) {
                    order = new Order(
                            orderId,
                            rs.getString("customer"),
                            new ArrayList<>(),
                            rs.getString("status")
                    );
                    orderMap.put(orderId, order);
                }

                int productId = rs.getInt("product_id");
                if (!rs.wasNull()) {
                    Product product = new Product(
                            productId,
                            rs.getString("product_name"),
                            rs.getInt("ordered_quantity"),
                            rs.getDouble("product_price")
                    );
                    order.getProducts().add(product);
                }
            }
            orders.addAll(orderMap.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }
}