package com.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Make sure to import Order if it's in a different package
import com.example.model.Order;

public class User {
    private UUID id;
    private String name;
    private List<Order> orders = new ArrayList<>();

    // No-argument constructor
    public User() {}

    // Constructor with id and name
    public User(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor with id, name, and orders
    public User(UUID id, String name, List<Order> orders) {
        this.id = id;
        this.name = name;
        this.orders = orders;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
