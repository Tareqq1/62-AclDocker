package com.example.repository;

import com.example.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.UUID;

@Repository
@SuppressWarnings("rawtypes")
public class OrderRepository extends MainRepository<Order> {

    // Inject the path to the orders.json file from application properties.
    @Value("${spring.application.orderDataPath}")
    private String orderDataPath;

    /**
     * Returns the data path for orders.json.
     */
    @Override
    protected String getDataPath() {
        return orderDataPath;
    }

    /**
     * Specifies how to deserialize an array of Order objects.
     */
    @Override
    protected Class<Order[]> getArrayType() {
        return Order[].class;
    }

    /**
     * Add Order:
     * Adds a new order to the orders JSON file.
     */
    public void addOrder(Order order) {
        save(order); // 'save()' is inherited from MainRepository
    }

    /**
     * Get All Orders:
     * Retrieves all orders from the JSON file.
     */
    public ArrayList<Order> getOrders() {
        return findAll(); // 'findAll()' is inherited from MainRepository
    }

    /**
     * Get a Specific Order:
     * Retrieves an order by its unique ID.
     */
    public Order getOrderById(UUID orderId) {
        for (Order order : getOrders()) {
            if (order.getId().equals(orderId)) {
                return order;
            }
        }
        return null;
    }

    /**
     * Delete a Specific Order:
     * Deletes an order by its ID and updates the JSON file.
     */
    public void deleteOrderById(UUID orderId) {
        ArrayList<Order> orders = getOrders();
        boolean removed = orders.removeIf(o -> o.getId().equals(orderId));
        if (removed) {
            overrideData(orders); // 'overrideData()' writes the updated list back to the JSON file
        }
    }
}
