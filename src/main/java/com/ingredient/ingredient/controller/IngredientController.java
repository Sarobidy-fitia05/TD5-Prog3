package com.ingredient.ingredient.controller;

import com.ingredient.ingredient.entity.Ingredient;
import com.ingredient.ingredient.entity.StockMovement;
import com.ingredient.ingredient.entity.StockValue;
import com.ingredient.ingredient.service.IngredientService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ingredients")
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
    @GetMapping("/{id}/stockMovements")
    public List<StockMovement> getStockMovements(
            @PathVariable Integer id,
            @RequestParam String from,
            @RequestParam String to) {
        // On appelle la méthode du service que tu viens de créer
        return ingredientService.getStockMovements(id, from, to);
    }
    @PostMapping("/{id}/stockMovements")
    public List<StockMovement> createStockMovements(
            @PathVariable Integer id,
            @RequestBody List<StockMovement> movements) {
        // On passe la liste de mouvements reçue en JSON au service
        return ingredientService.saveStockMovements(id, movements);
    }
}