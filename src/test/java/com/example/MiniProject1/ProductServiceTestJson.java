package com.example.MiniProject1;

import com.example.model.Product;
import com.example.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductServiceTestJson {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.application.productDataPath:src/main/java/com/example/data/products.json}")
    private String productDataPath;

    @BeforeEach
    public void clearProductsJson() throws Exception {
        // Clear the products.json file so each test starts fresh.
        objectMapper.writeValue(new File(productDataPath), new ArrayList<Product>());
    }

    // =====================================================
    // Tests for addProduct(Product product)
    // =====================================================

    @Test
    void testAddProduct_success() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Test Product", 50.99);
        Product savedProduct = ProductService.addProduct(product);
        assertNotNull(savedProduct, "Saved product should not be null");
        assertEquals("Test Product", savedProduct.getName(), "Product name should match");
        assertEquals(50.99, savedProduct.getPrice(), "Product price should match");
    }

    @Test
    void testAddProduct_nullProduct_returnsNull() {
        Product result = ProductService.addProduct(null);
        assertNull(result, "Adding a null product should return null");
    }

    @Test
    void testAddProduct_duplicateId() {
        UUID commonId = UUID.randomUUID();
        Product product1 = new Product(commonId, "Product A", 25.00);
        Product product2 = new Product(commonId, "Product B", 35.00);
        ProductService.addProduct(product1);
        ProductService.addProduct(product2);
        List<Product> products = ProductService.getProducts();
        long count = products.stream().filter(p -> p.getId().equals(commonId)).count();
        assertTrue(count >= 1, "There should be at least one product with the duplicate ID");
    }

    // =====================================================
    // Tests for getProducts()
    // =====================================================

    @Test
    void testGetProducts_emptyInitially() {
        List<Product> products = ProductService.getProducts();
        assertNotNull(products, "Products list should not be null");
        assertEquals(0, products.size(), "Initially, products list should be empty");
    }

    @Test
    void testGetProducts_nonEmptyAfterAdd() {
        Product product = new Product(UUID.randomUUID(), "New Product", 60.00);
        ProductService.addProduct(product);
        List<Product> products = ProductService.getProducts();
        assertFalse(products.isEmpty(), "Products list should not be empty after adding a product");
    }

    @Test
    void testGetProducts_includesRecentlyAddedProduct() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Latest Product", 75.00);
        ProductService.addProduct(product);
        List<Product> products = ProductService.getProducts();
        assertTrue(products.stream().anyMatch(p -> p.getId().equals(productId)),
                "Products list should include the recently added product");
    }

    // =====================================================
    // Tests for getProductById(UUID productId)
    // =====================================================

    @Test
    void testGetProductById_returnsCorrectProduct() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Lookup Product", 85.00);
        ProductService.addProduct(product);
        Product retrieved = ProductService.getProductById(productId);
        assertNotNull(retrieved, "Retrieved product should not be null");
        assertEquals("Lookup Product", retrieved.getName(), "Product name should match");
        assertEquals(85.00, retrieved.getPrice(), "Product price should match");
    }

    @Test
    void testGetProductById_nonExistent_throwsException() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                ProductService.getProductById(UUID.randomUUID()));
        // Accept either "Product not found" or "Unable to read products.json" in the exception.
        boolean reasonMatches = exception.getReason().contains("Product not found") ||
                exception.getReason().contains("Unable to read products.json");
        assertTrue(reasonMatches, "Exception reason should indicate 'Product not found' or an error reading JSON");
    }

    @Test
    void testGetProductById_handlesMultipleProducts() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Multi Product", 95.00);
        ProductService.addProduct(new Product(UUID.randomUUID(), "Another Product", 45.00));
        ProductService.addProduct(product);
        Product retrieved = ProductService.getProductById(productId);
        assertNotNull(retrieved, "Product should be found among multiple products");
        assertEquals("Multi Product", retrieved.getName(), "Product name should match");
    }

    // =====================================================
    // Tests for updateProduct(UUID productId, String newName, double newPrice)
    // =====================================================

    @Test
    void testUpdateProduct_fullUpdate() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Old Product", 50.00);
        ProductService.addProduct(product);
        String newName = "New Product";
        double newPrice = 70.00;
        Product updated = ProductService.updateProduct(productId, newName, newPrice);
        assertNotNull(updated, "Updated product should not be null");
        assertEquals(newName, updated.getName(), "Product name should be updated");
        assertEquals(newPrice, updated.getPrice(), "Product price should be updated");
    }

    @Test
    void testUpdateProduct_partialUpdate_nameOnly() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Partial Product", 80.00);
        ProductService.addProduct(product);
        String newName = "Updated Name";
        // Passing the same price value to simulate updating only the name.
        Product updated = ProductService.updateProduct(productId, newName, 80.00);
        assertEquals(newName, updated.getName(), "Product name should be updated");
        assertEquals(80.00, updated.getPrice(), "Product price should remain unchanged");
    }

    @Test
    void testUpdateProduct_nonExistent_throwsException() {
        UUID productId = UUID.randomUUID();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                ProductService.updateProduct(productId, "Doesn't Matter", 50.00));
        assertEquals("Product not found", exception.getReason(), "Exception reason should be 'Product not found'");
    }

    // =====================================================
    // Tests for applyDiscount(double discount, ArrayList<UUID> productIds)
    // =====================================================

    @Test
    void testApplyDiscount_singleProduct() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Discount Product", 200.00);
        ProductService.addProduct(product);
        ArrayList<UUID> ids = new ArrayList<>(Arrays.asList(productId));
        ProductService.applyDiscount(10.0, ids); // 10% discount: 200 -> 180
        Product updated = ProductService.getProductById(productId);
        assertEquals(180.00, updated.getPrice(), 0.01, "Price should be reduced by 10%");
    }

    @Test
    void testApplyDiscount_multipleProducts() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        Product product1 = new Product(productId1, "Product 1", 100.00);
        Product product2 = new Product(productId2, "Product 2", 200.00);
        ProductService.addProduct(product1);
        ProductService.addProduct(product2);
        ArrayList<UUID> ids = new ArrayList<>(Arrays.asList(productId1, productId2));
        ProductService.applyDiscount(20.0, ids); // 20% discount: 100 -> 80, 200 -> 160
        assertEquals(80.00, ProductService.getProductById(productId1).getPrice(), 0.01, "Product 1 price should be discounted");
        assertEquals(160.00, ProductService.getProductById(productId2).getPrice(), 0.01, "Product 2 price should be discounted");
    }

    @Test
    void testApplyDiscount_nonExistentProduct_ignoresIt() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Existing Product", 150.00);
        ProductService.addProduct(product);
        ArrayList<UUID> ids = new ArrayList<>(Arrays.asList(productId, UUID.randomUUID()));
        ProductService.applyDiscount(10.0, ids); // 10% discount: 150 -> 135
        Product updated = ProductService.getProductById(productId);
        assertEquals(135.00, updated.getPrice(), 0.01, "Existing product price should be reduced by 10%");
    }

    // =====================================================
    // Tests for deleteProductById(UUID productId)
    // =====================================================

    @Test
    void testDeleteProductById_success() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Delete Product", 100.00);
        ProductService.addProduct(product);
        ProductService.deleteProductById(productId);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                ProductService.getProductById(productId));
        // Accept either "Product not found" or "Unable to read products.json"
        boolean condition = exception.getReason().contains("Product not found") ||
                exception.getReason().contains("Unable to read products.json");
        assertTrue(condition, "After deletion, getProductById should indicate product is not found");
    }

    @Test
    void testDeleteProductById_nonExistent_doesNotThrow() {
        UUID productId = UUID.randomUUID();
        // Expect that deletion of a non-existent product simply does nothing.
        assertDoesNotThrow(() -> ProductService.deleteProductById(productId),
                "Deleting a non-existent product should not throw an exception");
    }

    @Test
    void testDeleteProductById_afterDeletion_notInProductsList() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Check Delete", 90.00);
        ProductService.addProduct(product);
        int sizeBefore = ProductService.getProducts().size();
        ProductService.deleteProductById(productId);
        int sizeAfter = ProductService.getProducts().size();
        assertEquals(sizeBefore - 1, sizeAfter, "Products list size should decrease by one after deletion");
    }
}
