package com.orderstock.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Product")
public class Product {
    private int id;
    private String name;
    private int stock;
    private double price;

    public Product() {
    }

    public Product(int id, String name, int stock, double price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    @XmlElement
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @XmlElement
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @XmlElement
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}