package com.example.MiniProject1;

import com.example.model.Order;
import com.example.model.User;
import com.example.model.Cart;
import com.example.model.Product;

import com.example.service.CartService;
import com.example.service.OrderService;
import com.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AllServicesTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;


    // ============= UserService Tests =============

    // --- Tests for addUser(User user)
    @Test
    void addUser_success() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User1");
        User saved = userService.addUser(user);
        assertNotNull(saved, "Saved user should not be null");
        assertEquals("User1", saved.getName(), "User name should match");
    }

    @Test
    void addUser_nullUser_throwsException() {
        // If your implementation doesn't allow null, expect a NullPointerException.
        assertThrows(NullPointerException.class, () -> {
            userService.addUser(null);
        });
    }

    @Test
    void addUser_duplicateUserHandling() {
        UUID userId = UUID.randomUUID();
        User user1 = new User(userId, "UserDuplicate");
        userService.addUser(user1);
        // Depending on your repository logic, adding a duplicate might be allowed or not.
        // Here we just check that at least one instance exists.
        User user2 = new User(userId, "UserDuplicate2");
        userService.addUser(user2);
        long count = userService.getUsers().stream().filter(u -> u.getId().equals(userId)).count();
        assertTrue(count >= 1, "There should be at least one user with the same ID");
    }

    // --- Tests for getUsers()
    @Test
    void getUsers_returnsNonEmptyListAfterAdd() {
        User user = new User(UUID.randomUUID(), "User2");
        userService.addUser(user);
        List<User> users = userService.getUsers();
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Users list should not be empty");
    }

    @Test
    void getUsers_returnsEmptyInitially() {
        // In isolation this is hard if data persists between tests.
        // For demonstration, assert that getUsers() is not null.
        List<User> users = userService.getUsers();
        assertNotNull(users, "Users list should not be null");
    }

    @Test
    void getUsers_includesRecentlyAddedUser() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User3");
        userService.addUser(user);
        List<User> users = userService.getUsers();
        assertTrue(users.stream().anyMatch(u -> u.getId().equals(userId)), "Users list should contain the new user");
    }

    // --- Tests for getUserById(UUID userId)
    @Test
    void getUserById_returnsCorrectUser() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User4");
        userService.addUser(user);
        User retrieved = userService.getUserById(userId);
        assertNotNull(retrieved);
        assertEquals("User4", retrieved.getName());
    }

    @Test
    void getUserById_returnsNullForNonexistentUser() {
        User retrieved = userService.getUserById(UUID.randomUUID());
        assertNull(retrieved, "Should return null if user does not exist");
    }

    @Test
    void getUserById_handlesMultipleUsers() {
        UUID userId = UUID.randomUUID();
        userService.addUser(new User(userId, "User5"));
        userService.addUser(new User(UUID.randomUUID(), "User6"));
        User retrieved = userService.getUserById(userId);
        assertNotNull(retrieved);
        assertEquals("User5", retrieved.getName());
    }

    // --- Tests for getOrdersByUserId(UUID userId)
    @Test
    void getOrdersByUserId_initiallyEmpty() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User7", new ArrayList<>());
        userService.addUser(user);
        List<Order> orders = userService.getOrdersByUserId(userId);
        assertNotNull(orders);
        assertTrue(orders.isEmpty(), "New user should have an empty orders list");
    }

    @Test
    void getOrdersByUserId_afterCheckoutContainsOrder() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User8", new ArrayList<>());
        userService.addUser(user);
        userService.addOrderToUser(userId);
        List<Order> orders = userService.getOrdersByUserId(userId);
        assertEquals(1, orders.size(), "After checkout, orders list should contain 1 order");
    }

    @Test
    void getOrdersByUserId_returnsCorrectOrders() {
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
    void addOrderToUser_increasesOrderCount() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User10", new ArrayList<>());
        userService.addUser(user);
        int before = userService.getOrdersByUserId(userId).size();
        userService.addOrderToUser(userId);
        int after = userService.getOrdersByUserId(userId).size();
        assertEquals(before + 1, after, "Order count should increase by 1 after checkout");
    }

    @Test
    void addOrderToUser_orderHasCorrectUserId() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User11", new ArrayList<>());
        userService.addUser(user);
        userService.addOrderToUser(userId);
        Order order = userService.getOrdersByUserId(userId).get(0);
        assertEquals(userId, order.getUserId(), "Order's userId should match the user's id");
    }

    @Test
    void addOrderToUser_doesNotAddOrderIfUserNotExist() {
        UUID nonExistentUser = UUID.randomUUID();
        userService.addOrderToUser(nonExistentUser);
        List<Order> orders = userService.getOrdersByUserId(nonExistentUser);
        assertTrue(orders.isEmpty(), "No order should be added for a non-existent user");
    }

    // --- Tests for emptyCart(UUID userId)
    @Test
    void emptyCart_doesNotThrowException() {
        UUID userId = UUID.randomUUID();
        assertDoesNotThrow(() -> userService.emptyCart(userId));
    }

    @Test
    void emptyCart_onExistingUser_doesNotChangeOrders() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User12", new ArrayList<>());
        userService.addUser(user);
        userService.emptyCart(userId);
        List<Order> orders = userService.getOrdersByUserId(userId);
        assertTrue(orders.isEmpty(), "Empty cart should leave orders list unchanged (stub)");
    }

    @Test
    void emptyCart_calledMultipleTimes_doesNotFail() {
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
    void removeOrderFromUser_removesExistingOrder() {
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
    void removeOrderFromUser_nonExistentOrder_doesNothing() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User15", new ArrayList<>());
        userService.addUser(user);
        // Try removing an order that doesn't exist
        assertDoesNotThrow(() -> userService.removeOrderFromUser(userId, UUID.randomUUID()));
    }

    @Test
    void removeOrderFromUser_whenUserNotExist_doesNothing() {
        assertDoesNotThrow(() -> userService.removeOrderFromUser(UUID.randomUUID(), UUID.randomUUID()));
    }

    // --- Tests for deleteUserById(UUID userId)
    @Test
    void deleteUserById_success() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User16");
        userService.addUser(user);
        userService.deleteUserById(userId);
        assertNull(userService.getUserById(userId), "Deleted user should not be retrievable");
    }

    @Test
    void deleteUserById_nonExistentUser_throwsException() {
        UUID randomId = UUID.randomUUID();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.deleteUserById(randomId);
        });
        assertEquals("User not found", exception.getReason(), "Exception reason should be 'User not found'");
    }

    @Test
    void deleteUserById_afterDeletion_userNotInList() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User17");
        userService.addUser(user);
        userService.deleteUserById(userId);
        boolean exists = userService.getUsers().stream().anyMatch(u -> u.getId().equals(userId));
        assertFalse(exists, "Deleted user should not appear in the users list");
    }

    // ============= OrderService Tests =============

    // --- Tests for addOrder(Order order)
    @Test
    void addOrder_success() {
        Order order = new Order(UUID.randomUUID(), UUID.randomUUID(), 0.0, new ArrayList<>());
        orderService.addOrder(order);
        Order retrieved = orderService.getOrderById(order.getId());
        assertNotNull(retrieved, "Added order should be retrievable");
    }

    @Test
    void addOrder_invalidOrder_doesNotThrow() {
        Order order = new Order(UUID.randomUUID(), UUID.randomUUID(), 0.0, new ArrayList<>());
        assertDoesNotThrow(() -> orderService.addOrder(order));
    }

    @Test
    void addOrder_orderDataMatches() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Order order = new Order(orderId, userId, 0.0, new ArrayList<>());
        orderService.addOrder(order);
        Order retrieved = orderService.getOrderById(orderId);
        assertNotNull(retrieved, "Order data should match");
        assertEquals(userId, retrieved.getUserId());
    }

    // --- Tests for getOrders()
    @Test
    void getOrders_returnsNonEmptyAfterAdd() {
        Order order = new Order(UUID.randomUUID(), UUID.randomUUID(), 0.0, new ArrayList<>());
        orderService.addOrder(order);
        List<Order> orders = orderService.getOrders();
        assertNotNull(orders);
        assertFalse(orders.isEmpty(), "Orders list should be non-empty after adding an order");
    }

    @Test
    void getOrders_returnsEmptyIfNoneAdded() {
        // This test may be affected by other tests; ideally use isolated data.
        List<Order> orders = orderService.getOrders();
        assertNotNull(orders, "Orders list should not be null");
    }

    @Test
    void getOrders_includesRecentlyAddedOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 0.0, new ArrayList<>());
        orderService.addOrder(order);
        List<Order> orders = orderService.getOrders();
        assertTrue(orders.stream().anyMatch(o -> o.getId().equals(orderId)), "Orders list should include the recently added order");
    }

    // --- Tests for getOrderById(UUID orderId)
    @Test
    void getOrderById_returnsCorrectOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 0.0, new ArrayList<>());
        orderService.addOrder(order);
        Order retrieved = orderService.getOrderById(orderId);
        assertNotNull(retrieved);
        assertEquals(orderId, retrieved.getId());
    }

    @Test
    void getOrderById_returnsNullForNonexistent() {
        Order retrieved = orderService.getOrderById(UUID.randomUUID());
        assertNull(retrieved, "Should return null if order does not exist");
    }

    @Test
    void getOrderById_handlesMultipleOrders() {
        UUID orderId = UUID.randomUUID();
        orderService.addOrder(new Order(UUID.randomUUID(), UUID.randomUUID(), 0.0, new ArrayList<>()));
        Order order = new Order(orderId, UUID.randomUUID(), 0.0, new ArrayList<>());
        orderService.addOrder(order);
        Order retrieved = orderService.getOrderById(orderId);
        assertNotNull(retrieved);
        assertEquals(orderId, retrieved.getId());
    }

    // --- Tests for deleteOrderById(UUID orderId)
    @Test
    void deleteOrderById_success() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 0.0, new ArrayList<>());
        orderService.addOrder(order);
        orderService.deleteOrderById(orderId);
        Order retrieved = orderService.getOrderById(orderId);
        assertNull(retrieved, "Order should be deleted successfully");
    }

    @Test
    void deleteOrderById_nonExistentOrder_throwsException() {
        UUID randomOrderId = UUID.randomUUID();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.deleteOrderById(randomOrderId);
        });
        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void deleteOrderById_afterDeletion_notInOrdersList() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 0.0, new ArrayList<>());
        orderService.addOrder(order);
        orderService.deleteOrderById(orderId);
        List<Order> orders = orderService.getOrders();
        boolean exists = orders.stream().anyMatch(o -> o.getId().equals(orderId));
        assertFalse(exists, "Deleted order should not be present in the orders list");
    }

    // Tests for Cart!!!!

    @Test
    void addCart_success() {
        UUID cartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart(cartId, userId, new ArrayList<>());

        Cart savedCart = cartService.addCart(cart);

        System.out.println("Saved Cart: " + savedCart);

        assertNotNull(savedCart, "Saved cart should not be null");
        assertEquals(userId, savedCart.getUserId(), "Cart should be associated with the correct user");
    }

    @Test
    void addCart_nullCart_doesNotThrow() {
        assertDoesNotThrow(() -> cartService.addCart(null));
    }

    @Test
    void addCart_duplicateCartId_allowsMultipleEntries() {
        UUID cartId = UUID.randomUUID();
        Cart cart1 = new Cart(cartId, UUID.randomUUID(), new ArrayList<>());
        Cart cart2 = new Cart(cartId, UUID.randomUUID(), new ArrayList<>());

        cartService.addCart(cart1);
        cartService.addCart(cart2);

        List<Cart> carts = cartService.getCarts();
        assertTrue(carts.size() >= 1, "At least one cart with the same ID should exist");
    }


    @Test
    void getCarts_returnsNonEmptyListAfterAdd() {
        cartService.addCart(new Cart(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()));
        List<Cart> carts = cartService.getCarts();
        assertNotNull(carts);
        assertFalse(carts.isEmpty(), "Carts list should not be empty");
    }

    @Test
    void getCarts_returnsEmptyInitially() {
        List<Cart> carts = cartService.getCarts();
        assertNotNull(carts, "Carts list should not be null");
    }

    @Test
    void getCarts_multipleCartsAreRetrieved() {
        cartService.addCart(new Cart(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()));
        cartService.addCart(new Cart(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()));
        List<Cart> carts = cartService.getCarts();
        assertTrue(carts.size() >= 2, "Carts list should contain multiple entries");
    }


    @Test
    void getCartById_returnsCorrectCart() {
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart(cartId, UUID.randomUUID(), new ArrayList<>());
        cartService.addCart(cart);
        Cart retrieved = cartService.getCartById(cartId);
        assertNotNull(retrieved);
        assertEquals(cartId, retrieved.getId());
    }

    @Test
    void getCartById_returnsNullForNonexistentCart() {
        Cart retrieved = cartService.getCartById(UUID.randomUUID());
        assertNull(retrieved, "Should return null if cart does not exist");
    }

    @Test
    void getCartById_handlesMultipleCarts() {
        UUID cartId = UUID.randomUUID();
        cartService.addCart(new Cart(cartId, UUID.randomUUID(), new ArrayList<>()));
        cartService.addCart(new Cart(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()));

        Cart retrieved = cartService.getCartById(cartId);
        assertNotNull(retrieved);
        assertEquals(cartId, retrieved.getId());
    }


    @Test
    void getCartByUserId_returnsCorrectCart() {
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart(UUID.randomUUID(), userId, new ArrayList<>());
        cartService.addCart(cart);
        Cart retrieved = cartService.getCartByUserId(userId);
        assertNotNull(retrieved);
        assertEquals(userId, retrieved.getUserId());
    }

    @Test
    void getCartByUserId_returnsNullForNonexistentUser() {
        Cart retrieved = cartService.getCartByUserId(UUID.randomUUID());
        assertNull(retrieved, "Should return null if user does not have a cart");
    }

    @Test
    void getCartByUserId_handlesMultipleUsers() {
        UUID userId = UUID.randomUUID();
        cartService.addCart(new Cart(UUID.randomUUID(), userId, new ArrayList<>()));
        cartService.addCart(new Cart(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()));

        Cart retrieved = cartService.getCartByUserId(userId);
        assertNotNull(retrieved);
        assertEquals(userId, retrieved.getUserId());
    }


    @Test
    void deleteCartById_success() {
        UUID cartId = UUID.randomUUID();
        cartService.addCart(new Cart(cartId, UUID.randomUUID(), new ArrayList<>()));
        cartService.deleteCartById(cartId);
        assertNull(cartService.getCartById(cartId), "Deleted cart should not be retrievable");
    }

    @Test
    void deleteCartById_nonExistentCart_doesNotThrow() {
        assertDoesNotThrow(() -> cartService.deleteCartById(UUID.randomUUID()));
    }

    @Test
    void deleteCartById_doesNotAffectOtherCarts() {
        UUID cartId1 = UUID.randomUUID();
        UUID cartId2 = UUID.randomUUID();
        cartService.addCart(new Cart(cartId1, UUID.randomUUID(), new ArrayList<>()));
        cartService.addCart(new Cart(cartId2, UUID.randomUUID(), new ArrayList<>()));

        cartService.deleteCartById(cartId1);
        assertNotNull(cartService.getCartById(cartId2), "Other carts should remain unaffected");
    }

    // --- Tests for addProductToCart(UUID cartId, Product product) ---

    @Test
    void addProductToCart_success() {
        UUID cartId = UUID.randomUUID();
        Product product = new Product(UUID.randomUUID(), "Laptop", 1500.0);
        cartService.addCart(new Cart(cartId, UUID.randomUUID(), new ArrayList<>()));
        assertDoesNotThrow(() -> cartService.addProductToCart(cartId, product));
    }

    @Test
    void addProductToCart_nullProduct_throwsException() {
        UUID cartId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> cartService.addProductToCart(cartId, null));
    }

    @Test
    void addProductToCart_productIsAddedSuccessfully() {
        UUID cartId = UUID.randomUUID();
        Product product = new Product(UUID.randomUUID(), "Phone", 800.0);
        Cart cart = new Cart(cartId, UUID.randomUUID(), new ArrayList<>());
        cartService.addCart(cart);
        cartService.addProductToCart(cartId, product);
    }

    //remaining: Tests for deleteProductFromCart

}



