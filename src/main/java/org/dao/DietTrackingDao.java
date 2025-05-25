package org.dao;

import org.model.DietTracking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DietTrackingDao implements IDietTrackingDao {

    private final DatabaseConnectionManager connectionManager;

    public DietTrackingDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public DietTracking findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM diet_tracking WHERE tracking_id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return mapResultSetToDietTracking(rs);
            }
            return null;
        }
    }

    @Override
    public List<DietTracking> findAll() throws SQLException {
        List<DietTracking> trackingList = new ArrayList<>();
        String sql = "SELECT * FROM diet_tracking";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                trackingList.add(mapResultSetToDietTracking(rs));
            }
        }

        return trackingList;
    }

    @Override
    public boolean save(DietTracking entity) throws SQLException {
        String sql = "INSERT INTO diet_tracking (patient_diet_id, takip_tarihi, uygulandı_mı) VALUES (?, ?, ?)";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, entity.getPatient_diet_id());
            statement.setDate(2, Date.valueOf(entity.getTakip_tarihi()));
            statement.setBoolean(3, entity.getUygulandi_mi());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    entity.setTracking_id(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM diet_tracking WHERE tracking_id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public List<DietTracking> findAllByPatientId(int patientId) throws SQLException {
        List<DietTracking> list = new ArrayList<>();
        String sql = """
                     SELECT dt.*
                     FROM diet_tracking dt
                     JOIN patient_diets pd ON dt.patient_diet_id = pd.patient_diet_id
                     WHERE pd.patient_id = ?
                     ORDER BY dt.takip_tarihi DESC
                     """;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToDietTracking(rs));
            }
        }

        return list;
    }

    private DietTracking mapResultSetToDietTracking(ResultSet rs) throws SQLException {
        DietTracking dt = new DietTracking();
        dt.setTracking_id(rs.getInt("tracking_id"));
        dt.setPatient_diet_id(rs.getInt("patient_diet_id"));
        dt.setTakip_tarihi(rs.getDate("takip_tarihi").toLocalDate());
        dt.setUygulandi_mi(rs.getBoolean("uygulandi_mi"));
        return dt;
    }
}
