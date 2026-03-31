package com.ingredient.ingredient.service;

import com.ingredient.ingredient.config.DataSource;
import com.ingredient.ingredient.entity.Ingredient;
import com.ingredient.ingredient.entity.StockMovement;
import com.ingredient.ingredient.entity.StockValue;
import com.ingredient.ingredient.entity.Unit;
import com.ingredient.ingredient.exception.BadRequestException;
import com.ingredient.ingredient.exception.NotFoundException;
import com.ingredient.ingredient.repository.IngredientRepository;
import com.ingredient.ingredient.repository.StockMovementRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@AllArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final StockMovementRepository stockMovementRepository;
    private final DataSource dataSource;

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
    public List<StockMovement> getStockMovements(Integer id, String fromStr, String toStr) {
        // 1. Vérification d'existence (réutilise ta logique existante)
        // Si l'id n'existe pas, getIngredientById lève déjà la NotFoundException
        this.getIngredientById(id);

        // 2. Parsing des dates
        Instant from;
        Instant to;
        try {
            from = Instant.parse(fromStr);
            to = Instant.parse(toStr);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Expected ISO 8601 (e.g., 2026-03-31T00:00:00Z)");
        }

        // 3. Appel au repository
        return stockMovementRepository.findByFilter(id, from, to);
    }

    // --- AJOUTE AUSSI CELLE-CI POUR LE POINT G ---
    public List<StockMovement> saveStockMovements(Integer id, List<StockMovement> movements) {
        // Vérifie si l'ingrédient existe avant d'insérer
        this.getIngredientById(id);
        return stockMovementRepository.saveAll(id, movements);
    }


}