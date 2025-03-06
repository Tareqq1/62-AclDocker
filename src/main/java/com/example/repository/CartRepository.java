package com.example.repository;

import com.example.model.Cart;
import com.example.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("rawtypes")
public class CartRepository extends MainRepository<Cart> {

    public CartRepository() {}

    @Value("${spring.application.cartDataPath}")
    private String cartDataPath;

    @Override
    protected String getDataPath() {
        return cartDataPath;
    }

    @Override
    protected Class<Cart[]> getArrayType() {
        return Cart[].class;
    }

    public Cart addCart(Cart cart) {
        save(cart);
        return cart;
    }

    public ArrayList<Cart> getCarts() {
        return findAll();
    }

    public Cart getCartById(UUID cartId) {
        return findAll().stream().filter(c -> c.getId().equals(cartId)).findFirst().orElse(null);
    }

    public Cart getCartByUserId(UUID userId) {
        return findAll().stream().filter(c -> c.getUserId().equals(userId)).findFirst().orElse(null);
    }

    public void deleteCartById(UUID cartId) {
        ArrayList<Cart> carts = findAll();
        carts.removeIf(c -> c.getId().equals(cartId));
        overrideData(carts);
    }

    public void addProductToCart(UUID cartId, Product product) {
        ArrayList<Cart> carts = findAll();
        for (Cart cart : carts) {
            if (cart.getId().equals(cartId)) {
                cart.getProducts().add(product);
                overrideData(carts);
                return;
            }
        }
    }

    public void deleteProductFromCart(UUID cartId, Product product) {
        ArrayList<Cart> carts = findAll();
        for (Cart cart : carts) {
            if (cart.getId().equals(cartId)) {
                cart.getProducts().removeIf(p -> p.getId().equals(product.getId()));
                overrideData(carts);
                 //return;
            }
        }
    }
}

