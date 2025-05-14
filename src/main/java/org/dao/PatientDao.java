package org.dao;

import org.model.Doctor;
import org.model.Patient;
import org.model.User;
import org.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-05-14 15:56:25
 * Current User's Login: Emirhan-Karabulut
 *
 * Patient tablosu için veritabanı işlemlerini yöneten DAO sınıfı
 * height ve weight alanları kaldırıldı
 */
public class PatientDao {
    private UserDao userDao = new UserDao();
    private DoctorDao doctorDao = new DoctorDao();

    public Patient findById(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        }

        return null;
    }

    public Patient findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        }

        return null;
    }

    public List<Patient> findAll() throws SQLException {
        String sql = "SELECT * FROM patients";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        }

        return patients;
    }

    public List<Patient> findByDoctorId(Integer doctorId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE doctor_id = ?";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        }

        return patients;
    }

    public List<Patient> findByDiabetesType(String diabetesType) throws SQLException {
        String sql = "SELECT * FROM patients WHERE diabetes_type = ?";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, diabetesType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        }

        return patients;
    }

    public Integer save(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (user_id, doctor_id, diagnosis_date, diabetes_type, notes) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING patient_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patient.getUserId());
            stmt.setInt(2, patient.getDoctorId());

            if (patient.getDiagnosisDate() != null) {
                stmt.setDate(3, Date.valueOf(patient.getDiagnosisDate()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            stmt.setString(4, patient.getDiabetesType());
            stmt.setString(5, patient.getNotes());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return null;
    }

    public void update(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET doctor_id = ?, diagnosis_date = ?, diabetes_type = ?, " +
                "notes = ? WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patient.getDoctorId());

            if (patient.getDiagnosisDate() != null) {
                stmt.setDate(2, Date.valueOf(patient.getDiagnosisDate()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            stmt.setString(3, patient.getDiabetesType());
            stmt.setString(4, patient.getNotes());
            stmt.setInt(5, patient.getPatientId());

            stmt.executeUpdate();
        }
    }

    public void delete(Integer patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.executeUpdate();
        }
    }

    public List<Patient> findWithUserDetails() throws SQLException {
        String sql = "SELECT p.*, u.* FROM patients p " +
                "JOIN users u ON p.user_id = u.user_id";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getInt("patient_id"));
                patient.setUserId(rs.getInt("user_id"));
                patient.setDoctorId(rs.getInt("doctor_id"));

                Date diagnosisDate = rs.getDate("diagnosis_date");
                if (diagnosisDate != null) {
                    patient.setDiagnosisDate(diagnosisDate.toLocalDate());
                }

                patient.setDiabetesType(rs.getString("diabetes_type"));
                patient.setNotes(rs.getString("notes"));

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

                patient.setUser(user);

                // Doktor bilgisini de yükleyelim
                try {
                    Doctor doctor = doctorDao.findById(patient.getDoctorId());
                    patient.setDoctor(doctor);
                } catch (SQLException e) {
                    // Hata durumunda sadece hasta bilgilerini döndürmek için hatayı yut
                }

                patients.add(patient);
            }
        }

        return patients;
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setUserId(rs.getInt("user_id"));
        patient.setDoctorId(rs.getInt("doctor_id"));

        Date diagnosisDate = rs.getDate("diagnosis_date");
        if (diagnosisDate != null) {
            patient.setDiagnosisDate(diagnosisDate.toLocalDate());
        }

        patient.setDiabetesType(rs.getString("diabetes_type"));
        patient.setNotes(rs.getString("notes"));

        // Kullanıcı ve doktor bilgilerini yükleme
        try {
            User user = userDao.findById(patient.getUserId());
            patient.setUser(user);
        } catch (SQLException e) {
            // Hata durumunda sadece hasta bilgilerini döndürmek için hatayı yut
        }

        try {
            Doctor doctor = doctorDao.findById(patient.getDoctorId());
            patient.setDoctor(doctor);
        } catch (SQLException e) {
            // Hata durumunda sadece hasta bilgilerini döndürmek için hatayı yut
        }

        return patient;
    }
}