package org.dao;

import org.model.PatientExercise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientExercisesDao implements IPatientExercisesDao {

    private final DatabaseConnectionManager connectionManager;

    public PatientExercisesDao(){
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public PatientExercise findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM patient_exercises WHERE patient_exercise_id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapToPatientExercise(rs);
            }
            return null;
        }
    }

    @Override
    public List<PatientExercise> findAll() throws SQLException {
        List<PatientExercise> list = new ArrayList<>();
        String sql = "SELECT * FROM patient_exercises";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapToPatientExercise(rs));
            }
        }

        return list;
    }

    @Override
    public boolean save(PatientExercise entity) throws SQLException {
        String sql = "INSERT INTO patient_exercises (patient_id, exercise_id, doctor_id, baslangic_tarihi) VALUES (?, ?, ?, ?)";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, entity.getPatientId());
            ps.setInt(2, entity.getExerciseId());
            ps.setInt(3, entity.getDoctorId());
            ps.setDate(4, Date.valueOf(entity.getBaslangicTarihi()));

            int affected = ps.executeUpdate();

            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    entity.setPatientExerciseId(rs.getInt(1));
                }
                return true;
            }

            return false;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM patient_exercises WHERE patient_exercise_id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Integer findLatestPatientExerciseIdByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT patient_exercise_id FROM patient_exercises WHERE patient_id = ? ORDER BY baslangic_tarihi DESC LIMIT 1";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("patient_exercise_id");
            } else {
                throw new SQLException("Bu hastaya ait bir diyet kaydı bulunamadı.");
            }

        }
    }

    private PatientExercise mapToPatientExercise(ResultSet rs) throws SQLException {
        PatientExercise pe = new PatientExercise();
        pe.setPatientExerciseId(rs.getInt("patient_exercise_id"));
        pe.setPatientId(rs.getInt("patient_id"));
        pe.setExerciseId(rs.getInt("exercise_id"));
        pe.setDoctorId(rs.getInt("doctor_id"));
        pe.setBaslangicTarihi(rs.getDate("baslangic_tarihi").toLocalDate());
        return pe;
    }
}
