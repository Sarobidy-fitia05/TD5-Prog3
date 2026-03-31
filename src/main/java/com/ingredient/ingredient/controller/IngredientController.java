package com.ingredient.ingredient.controller;

import com.ingredient.ingredient.entity.Ingredient;
import com.ingredient.ingredient.entity.StockValue;
import com.ingredient.ingredient.service.IngredientService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping("/ingredients")
    public List<Ingredient> getAllIngredients() {
        return ingredientService.findAll();
    }

    @GetMapping("/ingredients/{id}")
    public Ingredient getIngredientById(@PathVariable int id) {
        return ingredientService.getIngredientById(id);
    }

    @GetMapping("/ingredients/{id}/stock")
    public Map<String, Object> getStock(@PathVariable int id,
                                        @RequestParam String at,
                                        @RequestParam String unit) {
        StockValue stock = ingredientService.getStockValue(id, at, unit);
        return Map.of("unite", stock.getUnit().toString(), "valeur", stock.getQuantity());
    }
}