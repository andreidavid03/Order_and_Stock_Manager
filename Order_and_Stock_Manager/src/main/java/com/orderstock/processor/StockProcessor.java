package com.orderstock.processor;

import com.orderstock.model.Product;
import com.orderstock.model.Products;
import com.orderstock.repository.ProductRepository;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StockProcessor {
    private static final String INBOX_FOLDER = "C:/Users/David/IdeaProjects/Order_and_Stock_Manager/stock_inbox";
    private static final String DONE_FOLDER = "C:/Users/David/IdeaProjects/Order_and_Stock_Manager/stock_done";

    private static final Logger logger = Logger.getLogger(StockProcessor.class.getName());
    private final ProductRepository productRepository = new ProductRepository();

    public void processStocks() throws Exception {
        Path inboxPath = Paths.get(INBOX_FOLDER);
        Path donePath = Paths.get(DONE_FOLDER);

        Files.createDirectories(inboxPath);
        Files.createDirectories(donePath);

        List<Path> xmlFiles = Files.list(inboxPath)
                .filter(p -> p.getFileName().toString().endsWith(".xml"))
                .collect(Collectors.toList());

        if (!xmlFiles.isEmpty()) {
            logger.info("Found " + xmlFiles.size() + " new XML file(s) in stock_inbox.");
            for (Path file : xmlFiles) {
                processSingleFile(file, donePath);
            }
        } else {
            logger.info("No new XML files found in stock_inbox.");
        }
    }

    private void processSingleFile(Path file, Path donePath) {
        try {
            logger.info("Processing file: " + file.getFileName());
            JAXBContext context = JAXBContext.newInstance(Products.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Products products = (Products) unmarshaller.unmarshal(file.toFile());

            for (Product product : products.getProducts()) {
                productRepository.addOrUpdateProduct(product);
                logger.info("Processed product: " + product.getName());
            }
            Files.copy(file, donePath.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied file to stock_done: " + file.getFileName());
        } catch (Exception e) {
            logger.severe("Error processing file: " + file.getFileName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
