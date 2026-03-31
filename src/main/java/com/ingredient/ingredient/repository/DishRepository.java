package com.ingredient.ingredient.repository;


import com.ingredient.ingredient.config.DataSource;
import com.ingredient.ingredient.entity.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository {

    private final DataSource dataSource;

    // Injection du DataSource par constructeur
    public DishRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Dish> findAllWithIngredients() {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT id, name,  selling_price FROM dish";
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setPrice(rs.getDouble("selling_price"));
                dish.setDishIngredients(findIngredientsByDishId(conn, dish.getId()));
                dishes.add(dish);
            }
            return dishes;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll dishes", e);
        }
    }

    public Dish findById(Integer id) {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dish dish = new Dish();
                    dish.setId(rs.getInt("id"));
                    dish.setName(rs.getString("name"));
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                    dish.setPrice(rs.getDouble("selling_price"));
                    dish.setDishIngredients(findIngredientsByDishId(conn, dish.getId()));
                    return dish;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById dish", e);
        }
    }

    private List<DishIngredient> findIngredientsByDishId(Connection conn, Integer dishId) throws SQLException {
        List<DishIngredient> list = new ArrayList<>();
        String sql = """
            SELECT i.id, i.name, i.price, i.category, di.required_quantity, di.unit
            FROM ingredient i
            JOIN dish_ingredient di ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ing = new Ingredient();
                    ing.setId(rs.getInt("id"));
                    ing.setName(rs.getString("name"));
                    ing.setPrice(rs.getDouble("price"));
                    ing.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                    DishIngredient di = new DishIngredient();
                    di.setIngredient(ing);
                    di.setQuantity(rs.getDouble("required_quantity"));
                    di.setUnit(Unit.valueOf(rs.getString("unit")));
                    list.add(di);
                }
            }
        }
        return list;
    }

    public void updateIngredients(Integer dishId, List<Ingredient> ingredients) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dish_ingredient WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }
            String insertSql = "INSERT INTO dish_ingredient (id, id_ingredient, id_dish, required_quantity, unit) VALUES (?, ?, ?, ?, ?::unit)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ing : ingredients) {
                    if (ingredientExists(conn, ing.getId())) {
                        ps.setInt(1, getNextDishIngredientId(conn));
                        ps.setInt(2, ing.getId());
                        ps.setInt(3, dishId);
                        ps.setDouble(4, 1.0);
                        ps.setString(5, Unit.PCS.name());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updateIngredients", e);
        }
    }

    private boolean ingredientExists(Connection conn, Integer ingredientId) throws SQLException {
        String sql = "SELECT 1 FROM ingredient WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int getNextDishIngredientId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM dish_ingredient";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}