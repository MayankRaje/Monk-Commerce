package com.monk.Commerce.MonkCommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemFormatDTO {
    private Long product_id;
    private Integer quantity;
    private Double price;
}

