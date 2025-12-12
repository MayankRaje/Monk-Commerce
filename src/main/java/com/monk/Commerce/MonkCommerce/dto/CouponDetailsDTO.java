package com.monk.Commerce.MonkCommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponDetailsDTO {
    // For cart-wise
    private Double threshold;
    private Double discount;
    
    // For product-wise
    private Long product_id;
    
    // For bxgy
    private List<BuyProductDTO> buy_products;
    private List<GetProductDTO> get_products;
    private Integer repition_limit;
    
    // Expiration dates
    private LocalDateTime start_date;
    private LocalDateTime end_date;
}

