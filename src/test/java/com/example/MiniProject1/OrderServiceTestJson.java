package com.example.MiniProject1;

import com.example.model.Order;
import com.example.repository.OrderRepository;
import com.example.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceTestJson {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.application.orderDataPath}")
    private String orderDataPath;

    @BeforeEach
    void clearOrdersJson() throws Exception {
        // Clear the orders.json file before each test
        objectMapper.writeValue(new File(orderDataPath), new ArrayList<Order>());
    }

    // =====================================================
    // Tests for addOrder(Order order)
    // =====================================================

    @Test
    public void testAddOrder_Success() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 100.0, new ArrayList<>());
        orderService.addOrder(order);

        Order retrieved = orderService.getOrderById(orderId);
        assertNotNull(retrieved, "Added order should be retrievable from JSON");
        assertEquals(100.0, retrieved.getTotalPrice(), "Total price should match the added order");
    }

    @Test
    public void testAddOrder_OrderDataMatches() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Order order = new Order(orderId, userId, 150.0, new ArrayList<>());
        orderService.addOrder(order);

        Order retrieved = orderService.getOrderById(orderId);
        assertNotNull(retrieved, "Order should be found in JSON");
        assertEquals(userId, retrieved.getUserId(), "User ID should match");
        assertEquals(150.0, retrieved.getTotalPrice(), "Total price should match");
    }

    @Test
    public void testAddOrder_MultipleOrders() {
        int initialSize = orderService.getOrders().size();
        Order order1 = new Order(UUID.randomUUID(), UUID.randomUUID(), 50.0, new ArrayList<>());
        Order order2 = new Order(UUID.randomUUID(), UUID.randomUUID(), 75.0, new ArrayList<>());
        orderService.addOrder(order1);
        orderService.addOrder(order2);

        ArrayList<Order> orders = orderService.getOrders();
        assertEquals(initialSize + 2, orders.size(), "Two orders should be added in JSON");
    }

    // =====================================================
    // Tests for getOrders()
    // =====================================================

    @Test
    public void testGetOrders_ReturnsEmptyInitially() {
        ArrayList<Order> orders = orderService.getOrders();
        assertNotNull(orders, "Orders list should not be null");
        assertEquals(0, orders.size(), "Initially, orders.json should be empty");
    }

    @Test
    public void testGetOrders_ReturnsNonEmptyAfterAdd() {
        Order order = new Order(UUID.randomUUID(), UUID.randomUUID(), 80.0, new ArrayList<>());
        orderService.addOrder(order);

        ArrayList<Order> orders = orderService.getOrders();
        assertFalse(orders.isEmpty(), "Orders.json should not be empty after adding an order");
    }

    @Test
    public void testGetOrders_IncludesRecentlyAddedOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 90.0, new ArrayList<>());
        orderService.addOrder(order);

        ArrayList<Order> orders = orderService.getOrders();
        assertTrue(orders.stream().anyMatch(o -> o.getId().equals(orderId)),
                "orders.json should include the recently added order");
    }

    // =====================================================
    // Tests for getOrderById(UUID orderId)
    // =====================================================

    @Test
    public void testGetOrderById_ReturnsCorrectOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 120.0, new ArrayList<>());
        orderService.addOrder(order);

        Order retrieved = orderService.getOrderById(orderId);
        assertNotNull(retrieved, "Retrieved order should not be null");
        assertEquals(orderId, retrieved.getId(), "Order IDs should match");
    }

    @Test
    public void testGetOrderById_NonExistent() {
        Order retrieved = orderService.getOrderById(UUID.randomUUID());
        assertNull(retrieved, "Should return null for a non-existent order in JSON");
    }

    @Test
    public void testGetOrderById_HandlesMultipleOrders() {
        UUID orderId = UUID.randomUUID();
        orderService.addOrder(new Order(UUID.randomUUID(), UUID.randomUUID(), 60.0, new ArrayList<>()));
        Order order = new Order(orderId, UUID.randomUUID(), 70.0, new ArrayList<>());
        orderService.addOrder(order);

        Order retrieved = orderService.getOrderById(orderId);
        assertNotNull(retrieved, "Order should be found among multiple orders in JSON");
        assertEquals(orderId, retrieved.getId(), "Order IDs should match");
    }

    // =====================================================
    // Tests for deleteOrderById(UUID orderId)
    // =====================================================

    @Test
    public void testDeleteOrderById_Success() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 110.0, new ArrayList<>());
        orderService.addOrder(order);

        orderService.deleteOrderById(orderId);
        Order retrieved = orderService.getOrderById(orderId);
        assertNull(retrieved, "Order should be deleted from JSON");
    }

    @Test
    public void testDeleteOrderById_NonExistentOrder_ThrowsException() {
        UUID randomOrderId = UUID.randomUUID();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.deleteOrderById(randomOrderId));
        assertEquals("Order not found", exception.getMessage(), "Exception message should be 'Order not found'");
    }

    @Test
    public void testDeleteOrderById_AfterDeletion_NotInOrdersList() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 130.0, new ArrayList<>());
        orderService.addOrder(order);
        orderService.deleteOrderById(orderId);

        List<Order> orders = orderService.getOrders();
        boolean exists = orders.stream().anyMatch(o -> o.getId().equals(orderId));
        assertFalse(exists, "Deleted order should not appear in orders.json");
    }
}
