package com.monk.Commerce.MonkCommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequestFormatDTO {
    private String type; // "cart-wise", "product-wise", "bxgy"
    private CouponDetailsDTO details;
}

