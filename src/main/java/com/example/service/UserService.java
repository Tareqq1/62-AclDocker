package com.example.service;

import com.example.model.Order;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrderService orderService; // New dependency for Order operations

    @Autowired
    public UserService(UserRepository userRepository, OrderService orderService) {
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    // 1) Add New User
    public User addUser(User user) {
        return userRepository.addUser(user);
    }

    // 2) Get All Users
    public ArrayList<User> getUsers() {
        return userRepository.getUsers();
    }

    // 3) Get a Specific User
    public User getUserById(UUID userId) {
        return userRepository.getUserById(userId);
    }

    // 4) Get the User's Orders
    public List<Order> getOrdersByUserId(UUID userId) {
        User user = userRepository.getUserById(userId);
        return (user != null) ? user.getOrders() : new ArrayList<>();
    }

    // 5) Add a New Order (Checkout)
    public void addOrderToUser(UUID userId) {
        User user = userRepository.getUserById(userId);
        if (user != null) {
            // Create a new order (dummy for now)
            Order newOrder = new Order(UUID.randomUUID(), userId, 0.0, new ArrayList<>());
            // Add the order to the user's orders list
            user.getOrders().add(newOrder);
            // Update the user record in users.json
            userRepository.deleteUserById(userId);
            userRepository.addUser(user);
            // Persist the order in the orders repository (orders.json)
            orderService.addOrder(newOrder);
        }
    }

    // 6) Empty Cart (Stub)
    public void emptyCart(UUID userId) {
        // Stub: No operation performed at this time.
    }

    // 7) Remove Order
    public void removeOrderFromUser(UUID userId, UUID orderId) {
        User user = userRepository.getUserById(userId);
        if (user != null) {
            user.getOrders().removeIf(o -> o.getId().equals(orderId));
            userRepository.deleteUserById(userId);
            userRepository.addUser(user);
        }
    }

    // 8) Delete the User
    public void deleteUserById(UUID userId) {
        userRepository.deleteUserById(userId);
    }
}
