package com.example.MiniProject1;

import com.example.model.Order;
import com.example.model.User;
import com.example.service.UserService;
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
public class UserServiceTestJson {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // Inject paths to JSON files (ensure these are configured in your application.properties)
    @Value("${spring.application.userDataPath}")
    private String userDataPath;

    @Value("${spring.application.orderDataPath}")
    private String orderDataPath;

    @BeforeEach

    public void clearJsonFiles() throws Exception {
        // Delete existing files if they exist
        new File(userDataPath).delete();
        new File(orderDataPath).delete();

        // Recreate empty JSON files
        objectMapper.writeValue(new File(userDataPath), new ArrayList<>());
        objectMapper.writeValue(new File(orderDataPath), new ArrayList<Order>());
    }


    // --- Tests for addUser(User user)

    @Test
    public void testAddUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User1");
        User saved = userService.addUser(user);
        assertNotNull(saved, "Saved user should not be null");
        assertEquals("User1", saved.getName(), "User name should match");
    }

    @Test
    public void testAddUser_NullUser_ThrowsException() {
        assertThrows(NullPointerException.class, () -> userService.addUser(null),
                "Adding null user should throw NullPointerException");
    }

    @Test
    public void testAddUser_DuplicateUserHandling() {
        UUID userId = UUID.randomUUID();
        User user1 = new User(userId, "UserDuplicate");
        userService.addUser(user1);
        User user2 = new User(userId, "UserDuplicate2");
        userService.addUser(user2);
        long count = userService.getUsers().stream().filter(u -> u.getId().equals(userId)).count();
        assertTrue(count >= 1, "There should be at least one user with the same ID");
    }

    // --- Tests for getUsers()

    @Test
    public void testGetUsers_ReturnsEmptyInitially() {
        List<User> users = userService.getUsers();
        assertNotNull(users, "Users list should not be null");
        assertEquals(0, users.size(), "Initially, users list should be empty");
    }

    @Test
    public void testGetUsers_ReturnsNonEmptyAfterAdd() {
        User user = new User(UUID.randomUUID(), "User2");
        userService.addUser(user);
        List<User> users = userService.getUsers();
        assertNotNull(users, "Users list should not be null");
        assertFalse(users.isEmpty(), "Users list should not be empty after adding a user");
    }

    @Test
    public void testGetUsers_IncludesRecentlyAddedUser() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User3");
        userService.addUser(user);
        List<User> users = userService.getUsers();
        assertTrue(users.stream().anyMatch(u -> u.getId().equals(userId)), "Users list should include the new user");
    }

    // --- Tests for getUserById(UUID userId)

    @Test
    public void testGetUserById_ReturnsCorrectUser() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User4");
        userService.addUser(user);
        User retrieved = userService.getUserById(userId);
        assertNotNull(retrieved, "Retrieved user should not be null");
        assertEquals("User4", retrieved.getName(), "User name should match");
    }

    @Test
    public void testGetUserById_ReturnsNullForNonexistentUser() {
        User retrieved = userService.getUserById(UUID.randomUUID());
        assertNull(retrieved, "Should return null if user does not exist");
    }

    @Test
    public void testGetUserById_HandlesMultipleUsers() {
        UUID userId = UUID.randomUUID();
        userService.addUser(new User(userId, "User5"));
        userService.addUser(new User(UUID.randomUUID(), "User6"));
        User retrieved = userService.getUserById(userId);
        assertNotNull(retrieved, "User should be found");
        assertEquals("User5", retrieved.getName(), "User name should match the first added");
    }

    // --- Tests for getOrdersByUserId(UUID userId)

    @Test
    public void testGetOrdersByUserId_InitiallyEmpty() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User7", new ArrayList<>());
        userService.addUser(user);
        List<Order> orders = userService.getOrdersByUserId(userId);
        assertNotNull(orders, "Orders list should not be null");
        assertTrue(orders.isEmpty(), "New user should have an empty orders list");
    }

    @Test
    public void testGetOrdersByUserId_AfterCheckoutContainsOrder() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User8", new ArrayList<>());
        userService.addUser(user);
        userService.addOrderToUser(userId);
        List<Order> orders = userService.getOrdersByUserId(userId);
        assertEquals(1, orders.size(), "After checkout, orders list should contain 1 order");
    }

    @Test
    public void testGetOrdersByUserId_ReturnsCorrectOrders() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User9", new ArrayList<>());
        userService.addUser(user);
        userService.addOrderToUser(userId);
        userService.addOrderToUser(userId);
        List<Order> orders = userService.getOrdersByUserId(userId);
        assertEquals(2, orders.size(), "Should return 2 orders after adding two orders");
    }

    // --- Tests for addOrderToUser(UUID userId)

    @Test
    public void testAddOrderToUser_IncreasesOrderCount() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User10", new ArrayList<>());
        userService.addUser(user);
        int before = userService.getOrdersByUserId(userId).size();
        userService.addOrderToUser(userId);
        int after = userService.getOrdersByUserId(userId).size();
        assertEquals(before + 1, after, "Order count should increase by 1 after checkout");
    }

    @Test
    public void testAddOrderToUser_OrderHasCorrectUserId() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User11", new ArrayList<>());
        userService.addUser(user);
        userService.addOrderToUser(userId);
        Order order = userService.getOrdersByUserId(userId).get(0);
        assertEquals(userId, order.getUserId(), "Order's userId should match the user's id");
    }

    @Test
    public void testAddOrderToUser_DoesNotAddOrderIfUserNotExist() {
        UUID nonExistentUser = UUID.randomUUID();
        userService.addOrderToUser(nonExistentUser);
        List<Order> orders = userService.getOrdersByUserId(nonExistentUser);
        assertTrue(orders.isEmpty(), "No order should be added for a non-existent user");
    }

    // --- Tests for emptyCart(UUID userId)

    @Test
    public void testEmptyCart_DoesNotThrowException() {
        UUID userId = UUID.randomUUID();
        assertDoesNotThrow(() -> userService.emptyCart(userId));
    }

    @Test
    public void testEmptyCart_OnExistingUser_DoesNotChangeOrders() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User12", new ArrayList<>());
        userService.addUser(user);
        userService.emptyCart(userId);
        List<Order> orders = userService.getOrdersByUserId(userId);
        assertTrue(orders.isEmpty(), "Empty cart should leave orders list unchanged");
    }

    @Test
    public void testEmptyCart_CalledMultipleTimes_DoesNotFail() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User13", new ArrayList<>());
        userService.addUser(user);
        assertDoesNotThrow(() -> {
            userService.emptyCart(userId);
            userService.emptyCart(userId);
        });
    }

    // --- Tests for removeOrderFromUser(UUID userId, UUID orderId)

    @Test
    public void testRemoveOrderFromUser_RemovesExistingOrder() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User14", new ArrayList<>());
        userService.addUser(user);
        userService.addOrderToUser(userId);
        List<Order> ordersBefore = userService.getOrdersByUserId(userId);
        assertFalse(ordersBefore.isEmpty(), "Orders list should not be empty before removal");
        UUID orderId = ordersBefore.get(0).getId();
        userService.removeOrderFromUser(userId, orderId);
        List<Order> ordersAfter = userService.getOrdersByUserId(userId);
        assertTrue(ordersAfter.isEmpty(), "Orders list should be empty after removal");
    }

    @Test
    public void testRemoveOrderFromUser_NonExistentOrder_DoesNothing() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User15", new ArrayList<>());
        userService.addUser(user);
        assertDoesNotThrow(() -> userService.removeOrderFromUser(userId, UUID.randomUUID()),
                "Removing a non-existent order should not throw an exception");
    }

    @Test
    public void testRemoveOrderFromUser_WhenUserNotExist_DoesNothing() {
        assertDoesNotThrow(() -> userService.removeOrderFromUser(UUID.randomUUID(), UUID.randomUUID()),
                "Removing an order for a non-existent user should not throw an exception");
    }

    // --- Tests for deleteUserById(UUID userId)

    @Test
    public void testDeleteUserById_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User16");
        userService.addUser(user);
        userService.deleteUserById(userId);
        assertNull(userService.getUserById(userId), "Deleted user should not be retrievable");
    }

    @Test
    public void testDeleteUserById_NonExistentUser_ThrowsException() {
        UUID randomId = UUID.randomUUID();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.deleteUserById(randomId);
        });
        assertEquals("User not found", exception.getReason(), "Exception reason should be 'User not found'");
    }

    @Test
    public void testDeleteUserById_AfterDeletion_UserNotInList() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User17");
        userService.addUser(user);
        userService.deleteUserById(userId);
        boolean exists = userService.getUsers().stream().anyMatch(u -> u.getId().equals(userId));
        assertFalse(exists, "Deleted user should not appear in the users list");
    }
}
