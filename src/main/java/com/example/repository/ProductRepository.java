package com.example.repository;

import com.example.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
@SuppressWarnings("rawtypes")
public class ProductRepository extends MainRepository<Product> {

    @Value("${spring.application.productDataPath}")
    private String productDataPath;  // Injected from application.properties

    public ProductRepository() {}

    @Override
    protected String getDataPath() {
        return productDataPath;  // Now uses the injected value instead of a hard-coded path
    }

    @Override
    protected Class<Product[]> getArrayType() {
        return Product[].class;
    }

    public Product addProduct(Product product) {
        save(product);
        return product;
    }

    public ArrayList<Product> getProducts() {
        return findAll();
    }

    public Product getProductById(UUID productId) {
        try {
            return findAll().stream()
                    .filter(product -> product.getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read products.json");
        }
    }

    public Product updateProduct(UUID productId, String newName, Double newPrice) {
        try {
            File file = new File(getDataPath());
            List<Product> products = objectMapper.readValue(file, new TypeReference<List<Product>>() {});

            return products.stream()
                    .filter(product -> product.getId().equals(productId))
                    .findFirst()
                    .map(product -> {
                        if (newName != null) {
                            product.setName(newName);
                        }
                        if (newPrice != null) { // Only update price if newPrice is provided
                            product.setPrice(newPrice);
                        }
                        saveAll(new ArrayList<>(products)); // Save the updated list
                        return product;
                    })
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to update product", e);
        }
    }
    public void applyDiscount(double discount, ArrayList<UUID> productIds) {
        try {
            File file = new File(getDataPath());
            List<Product> products = objectMapper.readValue(file, new TypeReference<List<Product>>() {});
            for (Product product : products) {
                if (productIds.contains(product.getId())) {
                    double newPrice = product.getPrice() * (1 - discount / 100);
                    product.setPrice(newPrice);
                }
            }
            objectMapper.writeValue(file, products);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update products.json");
        }
    }

    public void deleteProductById(UUID productId) {
        try {
            File file = new File(getDataPath());
            List<Product> products = objectMapper.readValue(file, new TypeReference<List<Product>>() {});
            products.removeIf(product -> product.getId().equals(productId));
            objectMapper.writeValue(file, products);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete product.");
        }
    }
}
