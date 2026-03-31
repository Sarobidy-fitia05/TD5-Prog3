package com.ingredient.ingredient.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockValue {
    private Double quantity;
    private Unit unit;
}