package org.dao;

import org.model.InsulinReference;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * İnsülin referans değerleri için DAO sınıfı
 */
public class InsulinReferenceDao implements IInsulinReferenceDao {

    private final DatabaseConnectionManager connectionManager;

    public InsulinReferenceDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public InsulinReference findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM insulin_reference WHERE reference_id = ?";
        InsulinReference reference = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    reference = mapResultSetToReference(rs);
                }
            }
        }

        return reference;
    }

    @Override
    public List<InsulinReference> findAll() throws SQLException {
        String sql = "SELECT * FROM insulin_reference ORDER BY min_blood_sugar";
        List<InsulinReference> references = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InsulinReference reference = mapResultSetToReference(rs);
                references.add(reference);
            }
        }

        return references;
    }

    @Override
    public boolean save(InsulinReference reference) throws SQLException {
        if (reference.getReference_id() == null) {
            // Insert
            String sql = "INSERT INTO insulin_reference (min_blood_sugar, max_blood_sugar, insulin_dose, description) " +
                    "VALUES (?, ?, ?, ?) RETURNING reference_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, reference.getMin_blood_sugar());
                stmt.setInt(2, reference.getMax_blood_sugar());
                stmt.setDouble(3, reference.getInsulin_dose());
                stmt.setString(4, reference.getDescription());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        reference.setReference_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE insulin_reference SET min_blood_sugar = ?, max_blood_sugar = ?, insulin_dose = ?, " +
                    "description = ? WHERE reference_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, reference.getMin_blood_sugar());
                stmt.setInt(2, reference.getMax_blood_sugar());
                stmt.setDouble(3, reference.getInsulin_dose());
                stmt.setString(4, reference.getDescription());
                stmt.setInt(5, reference.getReference_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM insulin_reference WHERE reference_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Verilen kan şekeri değeri için uygun insülin referansını bulur
     * @param bloodSugarValue Kan şekeri değeri
     * @return İnsülin referans bilgisi veya null
     * @throws SQLException
     */
    public InsulinReference findByBloodSugarValue(Integer bloodSugarValue) throws SQLException {
        String sql = "SELECT * FROM insulin_reference WHERE min_blood_sugar <= ? AND max_blood_sugar >= ?";
        InsulinReference reference = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bloodSugarValue);
            stmt.setInt(2, bloodSugarValue);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    reference = mapResultSetToReference(rs);
                }
            }
        }

        return reference;
    }

    // ResultSet'ten InsulinReference nesnesine dönüştürme yardımcı metodu
    private InsulinReference mapResultSetToReference(ResultSet rs) throws SQLException {
        InsulinReference reference = new InsulinReference();
        reference.setReference_id(rs.getInt("reference_id"));
        reference.setMin_blood_sugar(rs.getInt("min_blood_sugar"));
        reference.setMax_blood_sugar(rs.getInt("max_blood_sugar"));
        reference.setInsulin_dose(rs.getDouble("insulin_dose"));
        reference.setDescription(rs.getString("description"));
        return reference;
    }
}