package com.monk.Commerce.MonkCommerce.service.strategy;

import com.monk.Commerce.MonkCommerce.dto.ApplicableCouponDTO;
import com.monk.Commerce.MonkCommerce.model.Cart;
import com.monk.Commerce.MonkCommerce.model.Coupon;
import com.monk.Commerce.MonkCommerce.model.DiscountType;
import org.springframework.stereotype.Component;

@Component
public class CartWiseStrategy implements CouponDiscountStrategy {

    @Override
    public double calculateDiscount(Coupon coupon, Cart cart) {
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            return cart.getTotalAmount() * (coupon.getDiscountValue() / 100.0);
        } else {
            return Math.min(coupon.getDiscountValue(), cart.getTotalAmount());
        }
    }

    @Override
    public ApplicableCouponDTO checkApplicability(Coupon coupon, Cart cart) {
        if (coupon.getMinimumCartTotal() != null && cart.getTotalAmount() <= coupon.getMinimumCartTotal()) {
            return null;
        }
        double totalDiscount = calculateDiscount(coupon, cart);
        if (totalDiscount > 0) {
            ApplicableCouponDTO dto = new ApplicableCouponDTO();
            dto.setId(coupon.getId());
            dto.setName(coupon.getName());
            dto.setType(coupon.getType());
            dto.setTotalDiscount(totalDiscount);
            dto.setReason("Cart total meets minimum requirement");
            return dto;
        }
        return null;
    }

    @Override
    public void applyDiscount(Coupon coupon, Cart cart) {
        double totalDiscount = calculateDiscount(coupon, cart);
        distributeDiscountProportionally(cart, totalDiscount);
    }

    private void distributeDiscountProportionally(Cart cart, double totalDiscount) {
        double totalAmount = cart.getTotalAmount();
        if (totalAmount == 0) return;

        for (var item : cart.getItems()) {
            double itemTotal = item.getOriginalPrice() * item.getQuantity();
            double itemDiscount = (itemTotal / totalAmount) * totalDiscount;
            item.setDiscountAmount(itemDiscount);
            item.setDiscountedPrice(itemTotal - itemDiscount);
        }
    }
}

