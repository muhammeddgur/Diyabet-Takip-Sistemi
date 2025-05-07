package dao;

import model.Patient;
import model.User;
import model.Doctor;
import java.sql.*;
import java.time.LocalDate;

public class PatientDao {
    private final Connection conn;
    public PatientDao(Connection conn) { this.conn = conn; }

    public Patient findByUserId(int userId) throws SQLException {
        String sql = "SELECT p.id, p.created_at, d.id as doc_id, d.specialty FROM patients p " +
                "JOIN doctors d ON p.doctor_id=d.id WHERE p.user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new UserDao(conn).findById(userId);
                    Doctor d = new DoctorDao(conn).findById(rs.getInt("doc_id"));
                    LocalDate created = rs.getDate("created_at").toLocalDate();
                    return new Patient(rs.getInt("id"), u, d, created);
                }
            }
        }
        return null;
    }
}
