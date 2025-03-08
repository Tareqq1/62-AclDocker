package com.example.service;

import com.example.model.Product;
import com.example.repository.ProductRepository;
import com.example.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
@SuppressWarnings("rawtypes")
public class ProductService extends MainService<Product> {

 static ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository){

        this.productRepository = productRepository;
    }


    // 1) Add New Product
    public static Product addProduct(Product product){
        return productRepository.addProduct(product);
    }
    // 2) Get All Products
    public static ArrayList<Product> getProducts(){
        return productRepository.getProducts();
    }
    // 3) Get Product By id
    public static Product getProductById(UUID productId){
        return productRepository.getProductById(productId);
    }
    // 4) Update Product
    public static Product updateProduct(UUID productId, String newName, double newPrice){
        return productRepository.updateProduct(productId, newName, newPrice);
    }
    // 5) Apply Discount
    public static void applyDiscount(double discount, ArrayList<UUID> productIds){
        productRepository.applyDiscount(discount,productIds);
    }
    // 6 ) delete Product By ID
   public static void deleteProductById(UUID productId){
        productRepository.deleteProductById(productId);
    }

}

