package org.dao;

import org.model.Patient;
import org.model.User;
import org.model.Doctor;
import java.sql.*;
import java.time.LocalDate;

public class PatientDao {
    private final Connection conn;

    public PatientDao(Connection conn) {
        this.conn = conn;
    }

    public Patient findByUserTc(String userTc) throws SQLException {
        String sql = "SELECT p.id, p.created_at, d.id as doc_id FROM patients p " +
                "JOIN doctors d ON p.doctor_id=d.id WHERE p.user_tc=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userTc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new UserDao(conn).findByTc(userTc);
                    Doctor d = new DoctorDao(conn).findById(rs.getInt("doc_id"));
                    LocalDate created = rs.getDate("created_at").toLocalDate();
                    return new Patient(rs.getInt("id"), u, d, created);
                }
            }
        }
        return null;
    }

    public Patient findById(int id) throws SQLException {
        String sql = "SELECT p.id, p.user_tc, p.doctor_id, p.created_at FROM patients p WHERE p.id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String userTc = rs.getString("user_tc");
                    int doctorId = rs.getInt("doctor_id");
                    User u = new UserDao(conn).findByTc(userTc);
                    Doctor d = new DoctorDao(conn).findById(doctorId);
                    LocalDate created = rs.getDate("created_at").toLocalDate();
                    return new Patient(id, u, d, created);
                }
            }
        }
        return null;
    }

    public void save(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients(user_tc, doctor_id, created_at) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getUser().getTcKimlik());
            ps.setInt(2, patient.getDoctor().getId());
            ps.setDate(3, Date.valueOf(patient.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    patient.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

}