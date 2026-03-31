package com.ingredient.ingredient.service;

import com.ingredient.ingredient.entity.Dish;
import com.ingredient.ingredient.entity.Ingredient;
import com.ingredient.ingredient.exception.BadRequestException;
import com.ingredient.ingredient.exception.NotFoundException;
import com.ingredient.ingredient.repository.DishRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DishService {

    private final DishRepository dishRepository;

    public List<Dish> findAllWithIngredients() {
        return dishRepository.findAllWithIngredients();
    }

    public Dish findById(Integer id) {
        Dish dish = dishRepository.findById(id);
        if (dish == null) {
            throw new NotFoundException("Dish.id=" + id + " is not found");
        }
        return dish;
    }

    public void updateDishIngredients(Integer dishId, List<Ingredient> ingredients) {
        if (ingredients == null) {
            throw new BadRequestException("Request body is required");
        }

        Dish dish = dishRepository.findById(dishId);
        if (dish == null) {
            throw new NotFoundException("Dish.id=" + dishId + " is not found");
        }

        dishRepository.updateIngredients(dishId, ingredients);
    }
}