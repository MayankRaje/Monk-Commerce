package com.monk.Commerce.MonkCommerce.service.strategy;

import com.monk.Commerce.MonkCommerce.dto.ApplicableCouponDTO;
import com.monk.Commerce.MonkCommerce.model.Cart;
import com.monk.Commerce.MonkCommerce.model.Coupon;
import com.monk.Commerce.MonkCommerce.model.DiscountType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ProductWiseStrategy implements CouponDiscountStrategy {

    @Override
    public double calculateDiscount(Coupon coupon, Cart cart) {
        double totalDiscount = 0.0;
        Set<Long> applicableProductIds = new HashSet<>(coupon.getApplicableProductIds());

        for (var item : cart.getItems()) {
            if (applicableProductIds.contains(item.getProduct().getId())) {
                double itemTotal = item.getOriginalPrice() * item.getQuantity();
                if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                    totalDiscount += itemTotal * (coupon.getDiscountValue() / 100.0);
                } else {
                    totalDiscount += Math.min(coupon.getDiscountValue() * item.getQuantity(), itemTotal);
                }
            }
        }

        return totalDiscount;
    }

    @Override
    public ApplicableCouponDTO checkApplicability(Coupon coupon, Cart cart) {
        if (coupon.getApplicableProductIds() == null || coupon.getApplicableProductIds().isEmpty()) {
            return null;
        }
        double totalDiscount = calculateDiscount(coupon, cart);
        if (totalDiscount == 0) {
            return null;
        }
        ApplicableCouponDTO dto = new ApplicableCouponDTO();
        dto.setId(coupon.getId());
        dto.setName(coupon.getName());
        dto.setType(coupon.getType());
        dto.setTotalDiscount(totalDiscount);
        dto.setReason("Cart contains applicable products");
        return dto;
    }

    @Override
    public void applyDiscount(Coupon coupon, Cart cart) {
        Set<Long> applicableProductIds = new HashSet<>(coupon.getApplicableProductIds());

        for (var item : cart.getItems()) {
            if (applicableProductIds.contains(item.getProduct().getId())) {
                double itemTotal = item.getOriginalPrice() * item.getQuantity();
                double itemDiscount;
                if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                    itemDiscount = itemTotal * (coupon.getDiscountValue() / 100.0);
                } else {
                    itemDiscount = Math.min(coupon.getDiscountValue() * item.getQuantity(), itemTotal);
                }
                item.setDiscountAmount(itemDiscount);
                item.setDiscountedPrice(itemTotal - itemDiscount);
            } else {
                item.setDiscountAmount(0.0);
                item.setDiscountedPrice(item.getOriginalPrice() * item.getQuantity());
            }
        }
    }
}

