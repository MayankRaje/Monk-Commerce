package com.monk.Commerce.MonkCommerce.dto;

import com.monk.Commerce.MonkCommerce.model.CouponType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableCouponDTO {
    private Long id;
    private String name;
    private CouponType type;
    private Double totalDiscount;
    private String reason;
}

