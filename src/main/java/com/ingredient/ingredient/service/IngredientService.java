package com.ingredient.ingredient.service;

import com.ingredient.ingredient.entity.Ingredient;
import com.ingredient.ingredient.entity.StockValue;
import com.ingredient.ingredient.entity.Unit;
import com.ingredient.ingredient.exception.BadRequestException;
import com.ingredient.ingredient.exception.NotFoundException;
import com.ingredient.ingredient.repository.IngredientRepository;
import com.ingredient.ingredient.repository.StockMovementRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@AllArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final StockMovementRepository stockMovementRepository;

    public List<Ingredient> findAll() {
        return ingredientRepository.findAll();
    }

    public Ingredient getIngredientById(Integer id) {
        Ingredient ingredient = ingredientRepository.findById(id);
        if (ingredient == null) {
            throw new NotFoundException("Ingredient.id=" + id + " is not found");
        }
        return ingredient;
    }

    public StockValue getStockValue(Integer id, String at, String unitStr) {
        if (at == null || unitStr == null) {
            throw new BadRequestException("Either mandatory query parameter `at` or `unit` is not provided.");
        }

        Ingredient ingredient = ingredientRepository.findById(id);
        if (ingredient == null) {
            throw new NotFoundException("Ingredient.id=" + id + " is not found");
        }

        Instant instant;
        try {
            instant = Instant.parse(at);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format for 'at'. Expected ISO 8601 (e.g., 2023-01-01T00:00:00Z)");
        }

        Unit unit;
        try {
            unit = Unit.valueOf(unitStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid unit: " + unitStr);
        }

        return stockMovementRepository.getStockValueAt(id, instant, unit.name());
    }
}