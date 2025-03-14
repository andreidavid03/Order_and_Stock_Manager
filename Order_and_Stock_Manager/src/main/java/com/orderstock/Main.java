package com.orderstock;

import com.orderstock.model.Order;
import com.orderstock.model.Product;
import com.orderstock.repository.OrderRepository;
import com.orderstock.repository.ProductRepository;
import com.orderstock.processor.OrderProcessor;
import com.orderstock.processor.StockProcessor;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {


    public static JTextArea orderStatusArea;

    public static void main(String[] args) {
        try {

            new StockProcessor().processStocks();
            new OrderProcessor().startProcessing();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Order and Stock Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);


        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Products", createProductsPanel());
        tabbedPane.addTab("Orders", createOrdersPanel());

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }


    private static JPanel createProductsPanel() {
        JPanel productsPanel = new JPanel(new BorderLayout());


        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField nameField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField priceField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);

        JButton addButton = new JButton("Add/Update Product");
        formPanel.add(addButton);


        JPanel listPanel = new JPanel(new BorderLayout());
        JButton listButton = new JButton("List Products");
        JTextArea productArea = new JTextArea(10, 40);
        productArea.setEditable(false);
        listPanel.add(listButton, BorderLayout.NORTH);
        listPanel.add(new JScrollPane(productArea), BorderLayout.CENTER);

        JButton deleteButton = new JButton("Delete All");
        listPanel.add(deleteButton, BorderLayout.SOUTH);


        JPanel mainProductsPanel = new JPanel(new BorderLayout());
        mainProductsPanel.add(formPanel, BorderLayout.NORTH);
        mainProductsPanel.add(listPanel, BorderLayout.CENTER);


        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String qtyStr = quantityField.getText();
            String priceStr = priceField.getText();
            if (name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
                showAlert("Error", "All fields must be filled!");
                return;
            }
            try {
                int qty = Integer.parseInt(qtyStr);
                double price = Double.parseDouble(priceStr);
                Product product = new Product(0, name, qty, price);
                new ProductRepository().addOrUpdateProduct(product);
                showAlert("Success", "Product added/updated!");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Quantity and Price must be numbers!");
            }
        });

        listButton.addActionListener(e -> {
            List<Product> products = new ProductRepository().getAllProducts();
            StringBuilder sb = new StringBuilder();
            for (Product p : products) {
                sb.append(p.getId()).append(" - ")
                        .append(p.getName()).append(" - ")
                        .append("Qty: ").append(p.getStock()).append(" - ")
                        .append("Price: ").append(p.getPrice()).append("\n");
            }
            productArea.setText(sb.toString());
        });

        deleteButton.addActionListener(e -> {
            new ProductRepository().deleteAllProducts();
            productArea.setText("");
            showAlert("Success", "All products deleted!");
        });

        productsPanel.add(mainProductsPanel, BorderLayout.CENTER);
        return productsPanel;
    }


    private static JPanel createOrdersPanel() {
        JPanel ordersPanel = new JPanel(new BorderLayout());


        JTextArea ordersArea = new JTextArea(15, 40);
        ordersArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(ordersArea);
        ordersPanel.add(scrollPane, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh Orders");
        JButton editBtn = new JButton("Edit Order");
        JButton addOrderBtn = new JButton("Add Order");
        buttonPanel.add(refreshBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(addOrderBtn);
        ordersPanel.add(buttonPanel, BorderLayout.SOUTH);


        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Order Status Log"));
        orderStatusArea = new JTextArea(6, 40);
        orderStatusArea.setEditable(false);
        statusPanel.add(new JScrollPane(orderStatusArea), BorderLayout.CENTER);

        ordersPanel.add(statusPanel, BorderLayout.NORTH);


        refreshBtn.addActionListener(e -> {
            List<Order> orders = new OrderRepository().getAllOrdersWithProducts();
            StringBuilder sb = new StringBuilder();
            for (Order o : orders) {
                sb.append("ID: ").append(o.getId())
                        .append(", Customer: ").append(o.getClientName())
                        .append(", Status: ").append(o.getStatus());

                if (o.getProducts() != null && !o.getProducts().isEmpty()) {
                    sb.append("\n   Products:\n");
                    for (Product p : o.getProducts()) {
                        sb.append("     - ").append(p.getName())
                                .append(" x ").append(p.getStock())
                                .append("\n");
                    }
                }
                sb.append("\n");
            }
            ordersArea.setText(sb.toString());
        });


        editBtn.addActionListener(e -> {
            String idStr = JOptionPane.showInputDialog("Enter Order ID:");
            if (idStr == null || idStr.isEmpty()) return;
            try {
                int id = Integer.parseInt(idStr);
                String newCustomer = JOptionPane.showInputDialog("Enter new customer name:");
                if (newCustomer == null) return;
                String newStatus = JOptionPane.showInputDialog("Enter new status (RESERVED or NO_STOCK):");
                if (newStatus == null) return;

                Order updatedOrder = new Order(id, newCustomer, new ArrayList<>(), newStatus);
                new OrderRepository().updateOrder(updatedOrder);
                JOptionPane.showMessageDialog(null, "Order updated!");
                refreshBtn.doClick();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid Order ID!");
            }
        });


        addOrderBtn.addActionListener(e -> {
            addNewOrder();
            refreshBtn.doClick();
        });

        return ordersPanel;
    }

    private static void addNewOrder() {
        String customer = JOptionPane.showInputDialog("Enter customer name:");
        if (customer == null || customer.isEmpty()) return;


        List<Product> allProducts = new ProductRepository().getAllProducts();
        if (allProducts.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No products available in stock!");
            return;
        }

        String[] productNames = allProducts.stream().map(Product::getName).toArray(String[]::new);
        String chosenProduct = (String) JOptionPane.showInputDialog(null,
                "Select product:", "Choose Product",
                JOptionPane.QUESTION_MESSAGE, null,
                productNames, productNames[0]);
        if (chosenProduct == null) return;

        String qtyStr = JOptionPane.showInputDialog("Enter quantity to order:");
        if (qtyStr == null || qtyStr.isEmpty()) return;
        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid quantity!");
            return;
        }


        Product p = new Product(0, chosenProduct, qty, 0.0);
        List<Product> productList = new ArrayList<>();
        productList.add(p);


        Order order = new Order(0, customer, productList, "PENDING");
        new OrderRepository().addOrder(order);
        JOptionPane.showMessageDialog(null, "New order added!");
    }

    private static void showAlert(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }
}