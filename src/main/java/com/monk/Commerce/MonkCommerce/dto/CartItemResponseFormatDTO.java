package com.monk.Commerce.MonkCommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseFormatDTO {
    private Long product_id;
    private Integer quantity;
    private Double price;
    private Double total_discount;
}

