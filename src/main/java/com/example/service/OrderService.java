package com.example.service;

import com.example.model.Order;
import com.example.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class OrderService { // Extend MainService<Order> if you have one, otherwise omit

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // 1) Add Order: Adds a new order to the system.
    public void addOrder(Order order) {
        orderRepository.addOrder(order);
    }

    // 2) Get All Orders: Retrieves all orders.
    public ArrayList<Order> getOrders() {
        return orderRepository.getOrders();
    }

    // 3) Get a Specific Order: Retrieves an order by its ID.
    public Order getOrderById(UUID orderId) {
        return orderRepository.getOrderById(orderId);
    }

    // 4) Delete a Specific Order: Deletes an order; throws exception if not found.
    public void deleteOrderById(UUID orderId) {
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        orderRepository.deleteOrderById(orderId);
    }
}
