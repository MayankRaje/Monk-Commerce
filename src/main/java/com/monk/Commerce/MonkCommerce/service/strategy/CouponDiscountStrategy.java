package com.monk.Commerce.MonkCommerce.service.strategy;

import com.monk.Commerce.MonkCommerce.dto.ApplicableCouponDTO;
import com.monk.Commerce.MonkCommerce.model.Cart;
import com.monk.Commerce.MonkCommerce.model.Coupon;

public interface CouponDiscountStrategy {
    double calculateDiscount(Coupon coupon, Cart cart);
    ApplicableCouponDTO checkApplicability(Coupon coupon, Cart cart);
    void applyDiscount(Coupon coupon, Cart cart);
}

