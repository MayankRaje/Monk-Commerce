package com.monk.Commerce.MonkCommerce.config;

import com.monk.Commerce.MonkCommerce.model.Product;
import com.monk.Commerce.MonkCommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            productRepository.save(new Product(null, "Laptop", 999.99, "High-performance laptop"));
            productRepository.save(new Product(null, "Mouse", 29.99, "Wireless mouse"));
            productRepository.save(new Product(null, "Keyboard", 79.99, "Mechanical keyboard"));
            productRepository.save(new Product(null, "Monitor", 299.99, "27-inch 4K monitor"));
            productRepository.save(new Product(null, "Headphones", 149.99, "Noise-cancelling headphones"));
            productRepository.save(new Product(null, "Webcam", 89.99, "HD webcam"));
            productRepository.save(new Product(null, "USB Drive", 19.99, "64GB USB drive"));
            productRepository.save(new Product(null, "Tablet", 399.99, "10-inch tablet"));
        }
    }
}
