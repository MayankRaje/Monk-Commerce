package com.monk.Commerce.MonkCommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableCouponFormatDTO {
    private Long coupon_id;
    private String type;
    private Double discount;
}

