package org.dao;

import org.model.ExerciseTracking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseTrackingDao implements IExerciseTrackingDao {

    private final DatabaseConnectionManager connectionManager;

    public ExerciseTrackingDao(){
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public ExerciseTracking findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM exercise_tracking WHERE tracking_id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapToExerciseTracking(rs);
            }
            return null;
        }
    }

    @Override
    public List<ExerciseTracking> findAll() throws SQLException {
        List<ExerciseTracking> list = new ArrayList<>();
        String sql = "SELECT * FROM exercise_tracking";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapToExerciseTracking(rs));
            }
        }

        return list;
    }

    @Override
    public boolean save(ExerciseTracking entity) throws SQLException {
        String sql = "INSERT INTO exercise_tracking (patient_exercise_id, takip_tarihi, uygulandı_mı) VALUES (?, ?, ?)";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, entity.getPatient_exercise_id());
            ps.setDate(2, Date.valueOf(entity.getTakip_tarihi()));
            ps.setBoolean(3, entity.getUygulandi_mi());

            int affected = ps.executeUpdate();

            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
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
        String sql = "DELETE FROM exercise_tracking WHERE tracking_id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private ExerciseTracking mapToExerciseTracking(ResultSet rs) throws SQLException {
        ExerciseTracking et = new ExerciseTracking();
        et.setTracking_id(rs.getInt("tracking_id"));
        et.setPatient_exercise_id(rs.getInt("patient_exercise_id"));
        et.setTakip_tarihi(rs.getDate("takip_tarihi").toLocalDate());
        et.setUygulandi_mi(rs.getBoolean("uygulandi_mi"));
        return et;
    }
}
