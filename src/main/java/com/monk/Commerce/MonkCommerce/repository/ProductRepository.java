package com.monk.Commerce.MonkCommerce.repository;

import com.monk.Commerce.MonkCommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}

