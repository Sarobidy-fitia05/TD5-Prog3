package com.ingredient.ingredient.repository;


import com.ingredient.ingredient.config.DataSource;
import com.ingredient.ingredient.entity.MovementTypeEnum;
import com.ingredient.ingredient.entity.StockMovement;
import com.ingredient.ingredient.entity.StockValue;
import com.ingredient.ingredient.entity.Unit;
import com.ingredient.ingredient.exception.BadRequestException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    public List<StockMovement> findByFilter(Integer ingredientId, Instant from, Instant to) {
        List<StockMovement> movements = new ArrayList<>();
        String sql = """
            SELECT id, type, quantity, unit, creation_datetime 
            FROM stock_movement 
            WHERE id_ingredient = ? 
              AND creation_datetime >= ? 
              AND creation_datetime <= ?
            ORDER BY creation_datetime DESC
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ingredientId);
            ps.setTimestamp(2, Timestamp.from(from));
            ps.setTimestamp(3, Timestamp.from(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockMovement sm = new StockMovement();
                    sm.setId(rs.getInt("id"));
                    sm.setType(MovementTypeEnum.valueOf(rs.getString("type")));
                    sm.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

                    // Création du StockValue
                    StockValue sv = new StockValue();
                    sv.setQuantity(rs.getDouble("quantity"));
                    sv.setUnit(Unit.valueOf(rs.getString("unit").toUpperCase()));

                    // On attache la valeur au mouvement
                    sm.setValue(sv);

                    movements.add(sm);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des mouvements", e);
        }
        return movements;
    }
    public List<StockMovement> saveAll(Integer ingredientId, List<StockMovement> movements) {
        // Le CAST est nécessaire si tu utilises des types ENUM personnalisés dans PostgreSQL
        String sql = """
            INSERT INTO stock_movement (id_ingredient, type, quantity, unit, creation_datetime) 
            VALUES (?, CAST(? AS movement_type), ?, CAST(? AS unit_type), ?)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (StockMovement m : movements) {
                ps.setInt(1, ingredientId);
                ps.setString(2, m.getType().name()); // "IN" ou "OUT"

                // On récupère quantity et unit depuis l'objet StockValue (m.getValue())
                ps.setDouble(3, m.getValue().getQuantity());
                ps.setString(4, m.getValue().getUnit().name());

                // Gestion de la date : si nulle dans le JSON, on met l'heure actuelle
                Instant now = (m.getCreationDatetime() != null) ? m.getCreationDatetime() : Instant.now();
                ps.setTimestamp(5, Timestamp.from(now));

                ps.addBatch(); // On prépare l'envoi groupé pour plus de performance
            }

            ps.executeBatch(); // Exécution de l'insertion
            return movements;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion des mouvements de stock", e);
        }
    }
}