package com.orderstock.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderstock.Main;
import com.orderstock.model.Order;
import com.orderstock.model.Product;
import com.orderstock.repository.OrderRepository;
import com.orderstock.repository.ProductRepository;
import com.rabbitmq.client.*;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class OrderProcessor {
    private static final String ORDERS_QUEUE = "ORDERS";
    private static final String ORDERS_RESPONSE_QUEUE = "ORDERS_RESPONSE";

    private final ProductRepository productRepo = new ProductRepository();
    private final OrderRepository orderRepo = new OrderRepository();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    public void startProcessing() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(ORDERS_QUEUE, false, false, false, null);
            channel.queueDeclare(ORDERS_RESPONSE_QUEUE, false, false, false, null);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                executor.submit(() -> processOrder(message, channel));
            };

            channel.basicConsume(ORDERS_QUEUE, true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processOrder(String message, Channel channel) {
        try {
            Order order = mapper.readValue(message, Order.class);
            boolean allAvailable = true;


            for (Product product : order.getProducts()) {
                Product dbProd = productRepo.getProductByName(product.getName());
                if (dbProd == null || dbProd.getStock() < product.getStock()) {
                    allAvailable = false;
                    break;
                }
            }

            if (allAvailable) {
                for (Product product : order.getProducts()) {

                    productRepo.updateProductQuantity(new Product(
                            product.getId(),
                            product.getName(),
                            -product.getStock(),
                            product.getPrice()
                    ));
                }
                order.setStatus("RESERVED");
            } else {
                order.setStatus("NO_STOCK");
            }

            orderRepo.addOrder(order);
            String response = mapper.writeValueAsString(order);
            channel.basicPublish("", ORDERS_RESPONSE_QUEUE, null, response.getBytes(StandardCharsets.UTF_8));


            SwingUtilities.invokeLater(() -> Main.orderStatusArea.append(
                    "Order processed for " + order.getClientName() + ": " + order.getStatus() + "\n"
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}