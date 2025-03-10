package com.example.MiniProject1;

import com.example.model.Cart;
import com.example.model.Product;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;
import com.example.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CartServiceTestJson {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.application.cartDataPath}")
    private String cartDataPath;

    @Value("${spring.application.productDataPath}")
    private String productDataPath;

    @BeforeEach
    public void clearJsonFiles() throws Exception {
        // Clear carts.json
        objectMapper.writeValue(new File(cartDataPath), new ArrayList<Cart>());
        // Clear products.json so that product lookups start with a fresh file.
        objectMapper.writeValue(new File(productDataPath), new ArrayList<Product>());
    }

    // =====================================================
    // Tests for addCart()
    // =====================================================

    @Test
    public void testAddCart_Success() {
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart(userId);
        Cart addedCart = cartService.addCart(cart);
        assertNotNull(addedCart, "Cart should be added successfully.");
        assertEquals(userId, addedCart.getUserId(), "User ID should match what was set.");
    }

    @Test
    public void testAddCart_Multiple() {
        int initialSize = cartService.getCarts().size();
        Cart cart1 = new Cart(UUID.randomUUID());
        Cart cart2 = new Cart(UUID.randomUUID());
        cartService.addCart(cart1);
        cartService.addCart(cart2);
        ArrayList<Cart> carts = cartService.getCarts();
        assertEquals(initialSize + 2, carts.size(), "Two carts should be added.");
    }

    @Test
    public void testAddCart_DuplicateId() {
        UUID commonId = UUID.randomUUID();
        Cart cart1 = new Cart(commonId);
        Cart cart2 = new Cart(commonId);
        cartService.addCart(cart1);
        cartService.addCart(cart2);
        ArrayList<Cart> carts = cartService.getCarts();
        // If duplicates are allowed, then both should be present.
        assertEquals(2, carts.size(), "Both carts with duplicate IDs are added (if allowed).");
    }

    // =====================================================
    // Tests for getCarts()
    // =====================================================

    @Test
    public void testGetCarts_EmptyInitially() {
        ArrayList<Cart> carts = cartService.getCarts();
        assertNotNull(carts, "Carts list should not be null.");
        assertEquals(0, carts.size(), "Initially, carts list should be empty.");
    }

    @Test
    public void testGetCarts_AfterOneAdd() {
        Cart cart = new Cart(UUID.randomUUID());
        cartService.addCart(cart);
        ArrayList<Cart> carts = cartService.getCarts();
        assertEquals(1, carts.size(), "Carts list should have one entry after adding one cart.");
    }

    @Test
    public void testGetCarts_AfterMultipleAdds() {
        cartService.addCart(new Cart(UUID.randomUUID()));
        cartService.addCart(new Cart(UUID.randomUUID()));
        cartService.addCart(new Cart(UUID.randomUUID()));
        ArrayList<Cart> carts = cartService.getCarts();
        assertTrue(carts.size() >= 3, "Carts list should have at least three entries.");
    }

    // =====================================================
    // Tests for getCartById()
    // =====================================================

    @Test
    public void testGetCartById_Valid() {
        Cart cart = new Cart(UUID.randomUUID());
        cartService.addCart(cart);
        Cart fetched = cartService.getCartById(cart.getId());
        assertNotNull(fetched, "Fetched cart should not be null.");
        assertEquals(cart.getId(), fetched.getId(), "Cart IDs should match.");
    }

    @Test
    public void testGetCartById_NonExistent() {
        Cart fetched = cartService.getCartById(UUID.randomUUID());
        assertNull(fetched, "Fetching a non-existent cart should return null.");
    }

    @Test
    public void testGetCartById_AfterDeletion() {
        Cart cart = new Cart(UUID.randomUUID());
        cartService.addCart(cart);
        UUID cartId = cart.getId();
        cartService.deleteCartById(cartId);
        Cart fetched = cartService.getCartById(cartId);
        assertNull(fetched, "After deletion, cart should not be found.");
    }

    // =====================================================
    // Tests for getCartByUserId()
    // =====================================================

    @Test
    public void testGetCartByUserId_Valid() {
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart(userId);
        cartService.addCart(cart);
        Cart fetched = cartService.getCartByUserId(userId);
        assertNotNull(fetched, "Cart for a valid user should be found.");
        assertEquals(userId, fetched.getUserId(), "User IDs should match.");
    }

    @Test
    public void testGetCartByUserId_NonExistent() {
        Cart fetched = cartService.getCartByUserId(UUID.randomUUID());
        assertNull(fetched, "No cart should be found for a non-existent user.");
    }

    @Test
    public void testGetCartByUserId_MultipleCarts() {
        UUID userId = UUID.randomUUID();
        Cart cart1 = new Cart(userId);
        Cart cart2 = new Cart(userId);
        cartService.addCart(cart1);
        cartService.addCart(cart2);
        Cart fetched = cartService.getCartByUserId(userId);
        assertNotNull(fetched, "A cart should be returned even if multiple exist.");
        assertEquals(userId, fetched.getUserId(), "User IDs should match.");
    }

    // =====================================================
    // Tests for addProductToCart()
    // =====================================================

    @Test
    public void testAddProductToCart_SingleProduct() {
        Cart cart = new Cart(UUID.randomUUID());
        cartService.addCart(cart);
        Product product = new Product(UUID.randomUUID(), "Test Product", 50.0);
        // Add product to the real products.json file
        productRepository.addProduct(product);
        cartService.addProductToCart(cart.getId(), product);
        Cart updated = cartService.getCartById(cart.getId());
        assertTrue(updated.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId())),
                "Product should be added to the cart.");
    }

    @Test
    public void testAddProductToCart_MultipleProducts() {
        Cart cart = new Cart(UUID.randomUUID());
        cartService.addCart(cart);
        Product product1 = new Product(UUID.randomUUID(), "Product 1", 30.0);
        Product product2 = new Product(UUID.randomUUID(), "Product 2", 40.0);
        productRepository.addProduct(product1);
        productRepository.addProduct(product2);
        cartService.addProductToCart(cart.getId(), product1);
        cartService.addProductToCart(cart.getId(), product2);
        Cart updated = cartService.getCartById(cart.getId());
        assertEquals(2, updated.getProducts().size(), "Cart should contain two products.");
    }

    @Test
    public void testAddProductToCart_NonExistentCart() {
        Product product = new Product(UUID.randomUUID(), "Test Product", 50.0);
        // Ensure the product exists in products.json
        productRepository.addProduct(product);
        assertDoesNotThrow(() -> cartService.addProductToCart(UUID.randomUUID(), product),
                "Adding a product to a non-existent cart should not throw an exception.");
    }

    // =====================================================
    // Tests for deleteProductFromCart()
    // =====================================================

    @Test
    public void testDeleteProductFromCart_ExistingProduct() {
        Product product = new Product(UUID.randomUUID(), "Test Product", 50.0);
        // Add product to products.json
        productRepository.addProduct(product);
        Cart cart = new Cart(UUID.randomUUID());
        // Add the product to the cart
        cart.getProducts().add(product);
        cartService.addCart(cart);
        cartService.deleteProductFromCart(cart.getId(), product.getId());
        Cart updated = cartService.getCartById(cart.getId());
        assertFalse(updated.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId())),
                "Product should be removed from the cart.");
    }

    @Test
    public void testDeleteProductFromCart_ProductNotInCart() {
        // First, add a product to products.json so that getProductById succeeds.
        Product product = new Product(UUID.randomUUID(), "Test Product", 50.0);
        productRepository.addProduct(product);
        Cart cart = new Cart(UUID.randomUUID());
        cartService.addCart(cart);
        // Now, attempt deletion using the product's ID on a cart that doesn't contain the product.
        assertDoesNotThrow(() -> cartService.deleteProductFromCart(cart.getId(), product.getId()),
                "Deleting a product that is not in the cart should not throw an exception.");
        // Ensure the cart's product list remains empty.
        Cart updated = cartService.getCartById(cart.getId());
        assertTrue(updated.getProducts().isEmpty(), "Cart should remain empty.");
    }

    @Test
    public void testDeleteProductFromCart_MultipleProducts() {
        Product product1 = new Product(UUID.randomUUID(), "Product 1", 30.0);
        Product product2 = new Product(UUID.randomUUID(), "Product 2", 40.0);
        productRepository.addProduct(product1);
        productRepository.addProduct(product2);
        Cart cart = new Cart(UUID.randomUUID());
        cart.getProducts().add(product1);
        cart.getProducts().add(product2);
        cartService.addCart(cart);
        // Remove product1 by ID
        cartService.deleteProductFromCart(cart.getId(), product1.getId());
        Cart updated = cartService.getCartById(cart.getId());
        assertEquals(1, updated.getProducts().size(), "Only one product should remain.");
        assertEquals(product2.getId(), updated.getProducts().get(0).getId(), "Remaining product should be product2.");
    }

    // =====================================================
    // Tests for deleteCartById()
    // =====================================================

    @Test
    public void testDeleteCartById_ExistingCart() {
        Cart cart = new Cart(UUID.randomUUID());
        cartService.addCart(cart);
        UUID cartId = cart.getId();
        cartService.deleteCartById(cartId);
        assertNull(cartService.getCartById(cartId), "Deleted cart should no longer be retrievable.");
    }

    @Test
    public void testDeleteCartById_NonExistentCart() {
        UUID randomId = UUID.randomUUID();
        assertDoesNotThrow(() -> cartService.deleteCartById(randomId),
                "Deleting a non-existent cart should not throw an exception.");
    }

    @Test
    public void testDeleteCartById_CheckCartsList() {
        Cart cart = new Cart(UUID.randomUUID());
        cartService.addCart(cart);
        int sizeBefore = cartService.getCarts().size();
        cartService.deleteCartById(cart.getId());
        int sizeAfter = cartService.getCarts().size();
        assertEquals(sizeBefore - 1, sizeAfter, "Carts list size should decrease by one after deletion.");
    }
}
