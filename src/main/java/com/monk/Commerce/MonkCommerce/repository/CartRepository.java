package com.monk.Commerce.MonkCommerce.repository;

import com.monk.Commerce.MonkCommerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
}

