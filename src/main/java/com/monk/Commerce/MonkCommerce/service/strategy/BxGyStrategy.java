package com.monk.Commerce.MonkCommerce.service.strategy;

import com.monk.Commerce.MonkCommerce.dto.ApplicableCouponDTO;
import com.monk.Commerce.MonkCommerce.model.Cart;
import com.monk.Commerce.MonkCommerce.model.CartItem;
import com.monk.Commerce.MonkCommerce.model.Coupon;
import com.monk.Commerce.MonkCommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BxGyStrategy implements CouponDiscountStrategy {

    private final ProductRepository productRepository;

    @Override
    public double calculateDiscount(Coupon coupon, Cart cart) {
        Map<Long, Integer> buyProductQuantities = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            if (coupon.getBuyProductIds().contains(item.getProduct().getId())) {
                buyProductQuantities.put(item.getProduct().getId(),
                        buyProductQuantities.getOrDefault(item.getProduct().getId(), 0) + item.getQuantity());
            }
        }

        int totalBuyQuantity = buyProductQuantities.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (totalBuyQuantity < coupon.getBuyQuantity()) {
            return 0.0;
        }

        int dealRepetitions = totalBuyQuantity / coupon.getBuyQuantity();
        if (coupon.getMaxRepetitions() != null && coupon.getMaxRepetitions() > 0) {
            dealRepetitions = Math.min(dealRepetitions, coupon.getMaxRepetitions());
        }

        int totalFreeQuantity = dealRepetitions * coupon.getGetQuantity();

        double totalDiscount = 0.0;
        Map<Long, Integer> freeProductQuantities = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            if (coupon.getFreeProductIds().contains(item.getProduct().getId())) {
                freeProductQuantities.put(item.getProduct().getId(), item.getQuantity());
            }
        }

        int remainingFreeQuantity = totalFreeQuantity;
        for (Map.Entry<Long, Integer> entry : freeProductQuantities.entrySet()) {
            if (remainingFreeQuantity <= 0) break;

            Long productId = entry.getKey();
            Integer availableQuantity = entry.getValue();
            var product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                int freeCount = Math.min(remainingFreeQuantity, availableQuantity);
                totalDiscount += product.getPrice() * freeCount;
                remainingFreeQuantity -= freeCount;
            }
        }

        return totalDiscount;
    }

    @Override
    public ApplicableCouponDTO checkApplicability(Coupon coupon, Cart cart) {
        if (coupon.getBuyProductIds() == null || coupon.getBuyProductIds().isEmpty() ||
            coupon.getFreeProductIds() == null || coupon.getFreeProductIds().isEmpty()) {
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
        dto.setReason("BxGy conditions met");
        return dto;
    }

    @Override
    public void applyDiscount(Coupon coupon, Cart cart) {
        for (var item : cart.getItems()) {
            item.setDiscountAmount(0.0);
            item.setDiscountedPrice(item.getOriginalPrice() * item.getQuantity());
        }

        Map<Long, Integer> buyProductQuantities = new HashMap<>();
        for (var item : cart.getItems()) {
            if (coupon.getBuyProductIds().contains(item.getProduct().getId())) {
                buyProductQuantities.put(item.getProduct().getId(),
                        buyProductQuantities.getOrDefault(item.getProduct().getId(), 0) + item.getQuantity());
            }
        }

        int totalBuyQuantity = buyProductQuantities.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (totalBuyQuantity < coupon.getBuyQuantity()) {
            return;
        }

        int dealRepetitions = totalBuyQuantity / coupon.getBuyQuantity();
        if (coupon.getMaxRepetitions() != null && coupon.getMaxRepetitions() > 0) {
            dealRepetitions = Math.min(dealRepetitions, coupon.getMaxRepetitions());
        }

        int totalFreeQuantity = dealRepetitions * coupon.getGetQuantity();

        int remainingFreeQuantity = totalFreeQuantity;
        for (var item : cart.getItems()) {
            if (remainingFreeQuantity <= 0) break;

            if (coupon.getFreeProductIds().contains(item.getProduct().getId())) {
                int freeCount = Math.min(remainingFreeQuantity, item.getQuantity());
                double freeDiscount = item.getProduct().getPrice() * freeCount;
                item.setDiscountAmount(freeDiscount);
                item.setDiscountedPrice((item.getOriginalPrice() * item.getQuantity()) - freeDiscount);
                remainingFreeQuantity -= freeCount;
            }
        }
    }
}

