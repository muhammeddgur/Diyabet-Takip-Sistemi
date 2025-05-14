package org.dao;

import org.model.Doctor;
import org.model.User;
import org.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-05-14 15:53:19
 * Current User's Login: Emirhan-Karabulut
 *
 * Doktor tablosu için veritabanı işlemlerini yöneten DAO sınıfı
 * Lisans numarası, hastane ve uzmanlık alanı kaldırıldı
 */
public class DoctorDao {
    private UserDao userDao = new UserDao();

    public Doctor findById(Integer doctorId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        }

        return null;
    }

    public Doctor findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        }

        return null;
    }

    public List<Doctor> findAll() throws SQLException {
        String sql = "SELECT * FROM doctors";
        List<Doctor> doctors = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                doctors.add(mapResultSetToDoctor(rs));
            }
        }

        return doctors;
    }

    public Integer save(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors (user_id) VALUES (?) RETURNING doctor_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctor.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return null;
    }

    public void delete(Integer doctorId) throws SQLException {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            stmt.executeUpdate();
        }
    }

    public List<Doctor> findWithUserDetails() throws SQLException {
        String sql = "SELECT d.*, u.* FROM doctors d " +
                "JOIN users u ON d.user_id = u.user_id";
        List<Doctor> doctors = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("doctor_id"));
                doctor.setUserId(rs.getInt("user_id"));

                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setTcIdentity(rs.getString("tc_identity"));
                user.setPassword(rs.getString("password"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setBirthDate(rs.getDate("birth_date").toLocalDate());
                user.setGender(rs.getString("gender"));
                user.setProfilePhoto(rs.getBytes("profile_photo"));
                user.setUserType(rs.getString("user_type"));

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    user.setCreatedAt(createdAt.toLocalDateTime());
                }

                doctor.setUser(user);
                doctors.add(doctor);
            }
        }

        return doctors;
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getInt("doctor_id"));
        doctor.setUserId(rs.getInt("user_id"));

        // Kullanıcı bilgisini yükleme
        try {
            User user = userDao.findById(doctor.getUserId());
            doctor.setUser(user);
        } catch (SQLException e) {
            // Hata durumunda sadece doktor bilgilerini döndürmek için hatayı yut
        }

        return doctor;
    }
}