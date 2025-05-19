package org.dao;

import org.model.Exercise;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ExerciseDao arayüzünün implementasyonu.
 */
public class ExerciseDao implements IExerciseDao {

    private final DatabaseConnectionManager connectionManager;

    public ExerciseDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public Exercise findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM exercises WHERE exercise_id = ?";
        Exercise exercise = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    exercise = mapResultSetToExercise(rs);
                }
            }
        }

        return exercise;
    }

    @Override
    public List<Exercise> findAll() throws SQLException {
        String sql = "SELECT * FROM exercises";
        List<Exercise> exercises = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Exercise exercise = mapResultSetToExercise(rs);
                exercises.add(exercise);
            }
        }

        return exercises;
    }

    @Override
    public boolean save(Exercise exercise) throws SQLException {
        if (exercise.getExercise_id() == null) {
            // Insert
            String sql = "INSERT INTO exercises (exercise_adi, aciklama) VALUES (?, ?) RETURNING exercise_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, exercise.getExercise_adi());
                stmt.setString(2, exercise.getAciklama());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        exercise.setExercise_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE exercises SET exercise_adi = ?, aciklama = ? WHERE exercise_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, exercise.getExercise_adi());
                stmt.setString(2, exercise.getAciklama());
                stmt.setInt(3, exercise.getExercise_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM exercises WHERE exercise_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Exercise findByName(String name) throws SQLException {
        String sql = "SELECT * FROM exercises WHERE exercise_adi = ?";
        Exercise exercise = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    exercise = mapResultSetToExercise(rs);
                }
            }
        }

        return exercise;
    }

    @Override
    public List<Exercise> getPatientExercises(Integer patientId) throws SQLException {
        String sql = "SELECT e.* FROM exercises e " +
                "JOIN patient_exercises pe ON e.exercise_id = pe.exercise_id " +
                "WHERE pe.patient_id = ?";
        List<Exercise> exercises = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Exercise exercise = mapResultSetToExercise(rs);
                    exercises.add(exercise);
                }
            }
        }

        return exercises;
    }

    @Override
    public boolean assignExerciseToPatient(Integer patientId, Integer exerciseId, Integer doctorId) throws SQLException {
        String sql = "INSERT INTO patient_exercises (patient_id, exercise_id, doctor_id, baslangic_tarihi) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, exerciseId);
            stmt.setInt(3, doctorId);
            stmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // ResultSet'ten Exercise nesnesine dönüştürme yardımcı metodu
    private Exercise mapResultSetToExercise(ResultSet rs) throws SQLException {
        Exercise exercise = new Exercise();
        exercise.setExercise_id(rs.getInt("exercise_id"));
        exercise.setExercise_adi(rs.getString("exercise_adi"));
        exercise.setAciklama(rs.getString("aciklama"));
        return exercise;
    }
}