package com.monk.Commerce.MonkCommerce.repository;

import com.monk.Commerce.MonkCommerce.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
}

