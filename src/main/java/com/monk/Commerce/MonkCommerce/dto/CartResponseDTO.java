package com.monk.Commerce.MonkCommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private Long id;
    private List<CartItemResponseDTO> items;
    private Double totalAmount;
    private Double totalDiscount;
    private Double finalAmount;
    private Long appliedCouponId;
    private String appliedCouponName;
}

