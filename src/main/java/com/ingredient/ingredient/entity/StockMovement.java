package com.ingredient.ingredient.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class StockMovement {
    private Integer id;
    private MovementTypeEnum type;
    private StockValue value;
    private Instant creationDatetime;

}