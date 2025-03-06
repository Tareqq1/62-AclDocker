package com.example.controller;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.Product;
import com.example.model.User;
import com.example.service.CartService;
import com.example.service.ProductService;
import com.example.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final CartService cartService;
    private final ProductService productService;

    @Autowired
    public UserController(UserService userService, CartService cartService, ProductService productService) {
        this.userService = userService;
        this.cartService = cartService;
        this.productService = productService;
    }

    // 1) Add User Endpoint: POST /user/
    @PostMapping("/")
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    // 2) Get All Users Endpoint: GET /user/
    @GetMapping("/")
    public ArrayList<User> getUsers() {
        return userService.getUsers();
    }

    // 3) Get Specific User Endpoint: GET /user/{userId}
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId);
    }

    // 4) Delete User Endpoint: DELETE /user/delete/{userId}
    @DeleteMapping("/delete/{userId}")
    public String deleteUserById(@PathVariable UUID userId) {
        try {
            userService.deleteUserById(userId);
            return "User deleted successfully";
        } catch (ResponseStatusException ex) {
            return ex.getReason();
        }
    }

    // 5) Get a User's Orders: GET /user/{userId}/orders
    @GetMapping("/{userId}/orders")
    public List<Order> getOrdersByUserId(@PathVariable UUID userId) {
        return userService.getOrdersByUserId(userId);
    }

    // 6) Checkout (Add Order): POST /user/{userId}/checkout
    @PostMapping("/{userId}/checkout")
    public String addOrderToUser(@PathVariable UUID userId) {
        userService.addOrderToUser(userId);
        return "Order added successfully";
    }

    // 7) Remove Order: POST /user/{userId}/removeOrder
    @PostMapping("/{userId}/removeOrder")
    public String removeOrderFromUser(@PathVariable UUID userId, @RequestParam UUID orderId) {
        userService.removeOrderFromUser(userId, orderId);
        return "Order removed successfully";
    }

    // 8) Empty Cart: DELETE /user/{userId}/emptyCart
    @DeleteMapping("/{userId}/emptyCart")
    public String emptyCart(@PathVariable UUID userId) {
        userService.emptyCart(userId);
        return "Cart emptied successfully";
    }

    // 9) Add Product to Cart: PUT /user/addProductToCart
    @PutMapping("/addProductToCart")
    public String addProductToCart(@RequestParam UUID userId, @RequestParam UUID productId) {
        // Retrieve cart for the given user
        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            // Create a new cart if none exists
            cart = new Cart(userId);
            cart = cartService.addCart(cart);
        }
        // Retrieve the product using ProductService
        Product product = productService.getProductById(productId);
        if (product == null) {
            return "Product not found";
        }
        // Add the product to the cart
        cartService.addProductToCart(cart.getId(), product);
        return "Product added to cart";
    }

    // 10) Delete Product from Cart: PUT /user/deleteProductFromCart
    @PutMapping("/deleteProductFromCart")
    public String deleteProductFromCart(@RequestParam UUID userId, @RequestParam UUID productId) {
        // Retrieve the cart for the given user
        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            return "Cart is empty";
        }
        // Delegate to CartService to delete the product
        return cartService.deleteProductFromCart(cart.getId(), productId);
    }
}
