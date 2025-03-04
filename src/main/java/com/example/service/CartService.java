package com.example.service;

import com.example.model.Cart;
import com.example.model.Product;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@SuppressWarnings("rawtypes")
public class CartService extends MainService<Cart> {
    private final CartRepository cartRepository;
    //fix userRepo callning ?
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public Cart addCart(Cart cart) {
        return cartRepository.addCart(cart);
    }

    public ArrayList<Cart> getCarts() {
        return cartRepository.getCarts();
    }

    public Cart getCartById(UUID cartId) {
        return cartRepository.getCartById(cartId);
    }

    public Cart getCartByUserId(UUID userId) {
        return cartRepository.getCartByUserId(userId);
    }

    public void deleteCartById(UUID cartId) {
        cartRepository.deleteCartById(cartId);
    }

    public String addProductToCart(UUID cartId, UUID productId) {
        Product product = productRepository.getProductById(productId);
        if (product == null) return "Product not found";

        cartRepository.addProductToCart(cartId, product);
        return "Product added to cart";
    }

    public String deleteProductFromCart(UUID cartId, UUID productId) {
        Product product = productRepository.getProductById(productId);
        if (product == null) return "Product not found";

        cartRepository.deleteProductFromCart(cartId, product);
        return "Product removed from cart";
    }

    //just in case
    public UserRepository getUserRepository() {
        return userRepository;
    }
}
