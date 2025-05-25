package org.dao;

import org.model.ExerciseTracking;

import java.sql.*;
import java.time.LocalDate;
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

    /**
     * Belirtilen hasta ve tarih aralığı için egzersiz uyum oranını hesaplar
     * @param patientId Hasta ID
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return 0-100 arasında uyum yüzdesi
     */
    public double getComplianceRatio(Integer patientId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = """
                     SELECT COUNT(*) AS total_count,
                            SUM(CASE WHEN et.uygulandı_mı = true THEN 1 ELSE 0 END) AS applied_count
                     FROM exercise_tracking et
                     JOIN patient_exercises pe ON et.patient_exercise_id = pe.patient_exercise_id
                     WHERE pe.patient_id = ?
                     AND et.takip_tarihi BETWEEN ? AND ?
                     """;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);
            statement.setDate(2, Date.valueOf(startDate));
            statement.setDate(3, Date.valueOf(endDate));

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int totalCount = rs.getInt("total_count");
                int appliedCount = rs.getInt("applied_count");

                if (totalCount > 0) {
                    return (double) appliedCount / totalCount * 100;
                }
            }
        }

        return 0.0; // Veri yoksa 0 döndür
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
