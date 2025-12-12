package com.monk.Commerce.MonkCommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedCartDTO {
    private List<CartItemResponseFormatDTO> items;
    private Double total_price;
    private Double total_discount;
    private Double final_price;
}

