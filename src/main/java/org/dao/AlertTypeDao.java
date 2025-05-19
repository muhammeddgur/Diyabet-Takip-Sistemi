package org.dao;

import org.model.AlertType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AlertTypeDao arayüzünün implementasyonu.
 */
public class AlertTypeDao implements IAlertTypeDao {

    private final DatabaseConnectionManager connectionManager;

    public AlertTypeDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public AlertType findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM alert_types WHERE alert_type_id = ?";
        AlertType alertType = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    alertType = mapResultSetToAlertType(rs);
                }
            }
        }

        return alertType;
    }

    @Override
    public List<AlertType> findAll() throws SQLException {
        String sql = "SELECT * FROM alert_types";
        List<AlertType> alertTypes = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AlertType alertType = mapResultSetToAlertType(rs);
                alertTypes.add(alertType);
            }
        }

        return alertTypes;
    }

    @Override
    public boolean save(AlertType alertType) throws SQLException {
        if (alertType.getAlert_type_id() != null && alertType.getAlert_type_id() > 0) {
            return update(alertType);
        } else {
            String sql = "INSERT INTO alert_types (tip_adi, aciklama) VALUES (?, ?)";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, alertType.getTip_adi());
                stmt.setString(2, alertType.getAciklama());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            alertType.setAlert_type_id(generatedKeys.getInt(1));
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }


    public boolean update(AlertType alertType) throws SQLException {
        String sql = "UPDATE alert_types SET tip_adi = ?, aciklama = ? WHERE alert_type_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alertType.getTip_adi());
            stmt.setString(2, alertType.getAciklama());
            stmt.setInt(3, alertType.getAlert_type_id());

            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM alert_types WHERE alert_type_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        }
    }

    @Override
    public AlertType findByName(String tipAdi) throws SQLException {
        String sql = "SELECT * FROM alert_types WHERE tip_adi = ?";
        AlertType alertType = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipAdi);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    alertType = mapResultSetToAlertType(rs);
                }
            }
        }

        return alertType;
    }

    /**
     * ResultSet'ten AlertType nesnesine dönüştürme yapan yardımcı metod.
     *
     * @param rs ResultSet
     * @return AlertType nesnesi
     * @throws SQLException SQL hatası durumunda
     */
    private AlertType mapResultSetToAlertType(ResultSet rs) throws SQLException {
        AlertType alertType = new AlertType();
        alertType.setAlert_type_id(rs.getInt("alert_type_id"));
        alertType.setTip_adi(rs.getString("tip_adi"));
        alertType.setAciklama(rs.getString("aciklama"));
        return alertType;
    }
    }