package com.ingredient.ingredient.repository;


import com.ingredient.ingredient.config.DataSource;
import com.ingredient.ingredient.entity.StockValue;
import com.ingredient.ingredient.entity.Unit;
import com.ingredient.ingredient.exception.BadRequestException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;

@Repository
public class StockMovementRepository {

    private final DataSource dataSource;

    public StockMovementRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public StockValue getStockValueAt(Integer ingredientId, Instant t, String unitStr) {
        Unit unit;
        try {
            unit = Unit.valueOf(unitStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // L'unité est invalide => erreur 400
            throw new BadRequestException("Invalid unit: " + unitStr);
        }

        String sql = """
                SELECT unit, SUM(CASE WHEN type = 'OUT' THEN -quantity ELSE quantity END) AS actual_quantity
                FROM stock_movement
                    WHERE id_ingredient = ? 
                    AND creation_datetime <= ? 
                    AND unit = CAST(? AS unit_type)
                GROUP BY unit
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ps.setTimestamp(2, Timestamp.from(t));
            ps.setString(3, unit.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StockValue sv = new StockValue();
                    sv.setQuantity(rs.getDouble("actual_quantity"));
                    sv.setUnit(Unit.valueOf(rs.getString("unit")));
                    return sv;
                } else {
                    StockValue sv = new StockValue();
                    sv.setQuantity(0.0);
                    sv.setUnit(unit);
                    return sv;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du stock", e);
        }
    }
}