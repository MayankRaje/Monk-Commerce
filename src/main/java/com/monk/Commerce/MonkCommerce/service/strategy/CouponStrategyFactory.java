package com.monk.Commerce.MonkCommerce.service.strategy;

import com.monk.Commerce.MonkCommerce.model.CouponType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CouponStrategyFactory {

    private final Map<CouponType, CouponDiscountStrategy> strategies;

    public CouponStrategyFactory(CartWiseStrategy cartWiseStrategy,
                                 ProductWiseStrategy productWiseStrategy,
                                 BxGyStrategy bxGyStrategy) {
        this.strategies = Map.of(
                CouponType.CART_WISE, cartWiseStrategy,
                CouponType.PRODUCT_WISE, productWiseStrategy,
                CouponType.BXGY, bxGyStrategy
        );
    }

    public CouponDiscountStrategy getStrategy(CouponType type) {
        CouponDiscountStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for coupon type: " + type);
        }
        return strategy;
    }
}

