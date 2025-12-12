package com.monk.Commerce.MonkCommerce.dto;

import com.monk.Commerce.MonkCommerce.model.CouponType;
import com.monk.Commerce.MonkCommerce.model.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequestDTO {
    private String name;
    private CouponType type;
    private DiscountType discountType;
    private Double discountValue;
    private Double minimumCartTotal;
    private List<Long> applicableProductIds;
    private Integer buyQuantity;
    private Integer getQuantity;
    private List<Long> buyProductIds;
    private List<Long> freeProductIds;
    private Integer maxRepetitions;
    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

