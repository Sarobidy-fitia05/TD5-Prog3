package com.ingredient.ingredient.controller;

import com.ingredient.ingredient.entity.Dish;
import com.ingredient.ingredient.entity.Ingredient;
import com.ingredient.ingredient.service.DishService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping("/dishes")
    public List<Dish> getAllDishes() {
        return dishService.findAllWithIngredients();
    }

    @GetMapping("/dishes/{id}")
    public Dish getDishById(@PathVariable int id) {
        return dishService.findById(id);
    }

    @PutMapping("/dishes/{id}/ingredients")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDishIngredients(@PathVariable int id,
                                      @RequestBody(required = false) List<Ingredient> ingredients) {
        dishService.updateDishIngredients(id, ingredients);
    }
}