package org.dao;

import org.model.BloodSugarCategory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Kan şekeri kategorileri için DAO sınıfı
 */
public class BloodSugarCategoryDao implements IBloodSugarCategoryDao {

    private final DatabaseConnectionManager connectionManager;

    public BloodSugarCategoryDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public BloodSugarCategory findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_categories WHERE category_id = ?";
        BloodSugarCategory category = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    category = mapResultSetToCategory(rs);
                }
            }
        }

        return category;
    }

    @Override
    public List<BloodSugarCategory> findAll() throws SQLException {
        String sql = "SELECT * FROM blood_sugar_categories ORDER BY min_value";
        List<BloodSugarCategory> categories = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BloodSugarCategory category = mapResultSetToCategory(rs);
                categories.add(category);
            }
        }

        return categories;
    }

    @Override
    public boolean save(BloodSugarCategory category) throws SQLException {
        if (category.getCategory_id() == null) {
            // Insert
            String sql = "INSERT INTO blood_sugar_categories (category_name, min_value, max_value, description, alert_level, color_code) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING category_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, category.getCategory_name());
                stmt.setInt(2, category.getMin_value());
                stmt.setInt(3, category.getMax_value());
                stmt.setString(4, category.getDescription());
                stmt.setString(5, category.getAlert_level());
                stmt.setString(6, category.getColor_code());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        category.setCategory_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE blood_sugar_categories SET category_name = ?, min_value = ?, max_value = ?, " +
                    "description = ?, alert_level = ?, color_code = ? WHERE category_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, category.getCategory_name());
                stmt.setInt(2, category.getMin_value());
                stmt.setInt(3, category.getMax_value());
                stmt.setString(4, category.getDescription());
                stmt.setString(5, category.getAlert_level());
                stmt.setString(6, category.getColor_code());
                stmt.setInt(7, category.getCategory_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM blood_sugar_categories WHERE category_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Verilen kan şekeri değeri için kategori bulur
     * @param bloodSugarValue Kan şekeri değeri
     * @return Kan şekeri kategorisi veya null
     * @throws SQLException
     */
    public BloodSugarCategory findByBloodSugarValue(Integer bloodSugarValue) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_categories WHERE min_value <= ? AND max_value >= ?";
        BloodSugarCategory category = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bloodSugarValue);
            stmt.setInt(2, bloodSugarValue);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    category = mapResultSetToCategory(rs);
                }
            }
        }

        return category;
    }

    // ResultSet'ten BloodSugarCategory nesnesine dönüştürme yardımcı metodu
    private BloodSugarCategory mapResultSetToCategory(ResultSet rs) throws SQLException {
        BloodSugarCategory category = new BloodSugarCategory();
        category.setCategory_id(rs.getInt("category_id"));
        category.setCategory_name(rs.getString("category_name"));
        category.setMin_value(rs.getInt("min_value"));
        category.setMax_value(rs.getInt("max_value"));
        category.setDescription(rs.getString("description"));
        category.setAlert_level(rs.getString("alert_level"));
        category.setColor_code(rs.getString("color_code"));
        return category;
    }
}