package org.dao;

import org.model.Diet;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DietDao arayüzünün implementasyonu.
 */
public class DietDao implements IDietDao {

    private final DatabaseConnectionManager connectionManager;

    public DietDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public Diet findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM diets WHERE diet_id = ?";
        Diet diet = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    diet = mapResultSetToDiet(rs);
                }
            }
        }

        return diet;
    }

    @Override
    public List<Diet> findAll() throws SQLException {
        String sql = "SELECT * FROM diets";
        List<Diet> diets = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Diet diet = mapResultSetToDiet(rs);
                diets.add(diet);
            }
        }

        return diets;
    }

    @Override
    public boolean save(Diet diet) throws SQLException {
        if (diet.getDiet_id() == null) {
            // Insert
            String sql = "INSERT INTO diets (diet_adi, aciklama) VALUES (?, ?) RETURNING diet_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, diet.getDiet_adi());
                stmt.setString(2, diet.getAciklama());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        diet.setDiet_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE diets SET diet_adi = ?, aciklama = ? WHERE diet_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, diet.getDiet_adi());
                stmt.setString(2, diet.getAciklama());
                stmt.setInt(3, diet.getDiet_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM diets WHERE diet_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Diet findByName(String name) throws SQLException {
        String sql = "SELECT * FROM diets WHERE diet_adi = ?";
        Diet diet = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    diet = mapResultSetToDiet(rs);
                }
            }
        }

        return diet;
    }

    @Override
    public List<Diet> getPatientDiets(Integer patientId) throws SQLException {
        String sql = "SELECT d.* FROM diets d " +
                "JOIN patient_diets pd ON d.diet_id = pd.diet_id " +
                "WHERE pd.patient_id = ?";
        List<Diet> diets = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Diet diet = mapResultSetToDiet(rs);
                    diets.add(diet);
                }
            }
        }

        return diets;
    }

    @Override
    public boolean assignDietToPatient(Integer patientId, Integer dietId, Integer doctorId) throws SQLException {
        String sql = "INSERT INTO patient_diets (patient_id, diet_id, doctor_id, baslangic_tarihi) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, dietId);
            stmt.setInt(3, doctorId);
            stmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // ResultSet'ten Diet nesnesine dönüştürme yardımcı metodu
    private Diet mapResultSetToDiet(ResultSet rs) throws SQLException {
        Diet diet = new Diet();
        diet.setDiet_id(rs.getInt("diet_id"));
        diet.setDiet_adi(rs.getString("diet_adi"));
        diet.setAciklama(rs.getString("aciklama"));
        return diet;
    }
}