package org.dao;

import org.model.Doctor;
import org.model.Patient;
import org.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PatientDao arayüzünün implementasyonu.
 */
public class PatientDao implements IPatientDao {

    private final DatabaseConnectionManager connectionManager;
    private final UserDao userDao;
    private final DoctorDao doctorDao;

    public PatientDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
        userDao = new UserDao();
        doctorDao = new DoctorDao();
    }

    @Override
    public Patient findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        Patient patient = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    patient = mapResultSetToPatient(rs);
                }
            }
        }

        return patient;
    }

    @Override
    public List<Patient> findAll() throws SQLException {
        String sql = "SELECT * FROM patients";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient patient = mapResultSetToPatient(rs);
                if (patient != null) {
                    patients.add(patient);
                }
            }
        }

        return patients;
    }

    @Override
    public boolean save(Patient patient) throws SQLException {
        if (patient.getUser_id() == null) {
            // Önce User kaydedilmeli
            boolean userSaved = userDao.save(patient);
            if (!userSaved) {
                return false;
            }
        }

        if (patient.getPatient_id() == null) {
            // Insert
            String sql = "INSERT INTO patients (user_id, doctor_id) VALUES (?, ?) RETURNING patient_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, patient.getUser_id());
                stmt.setInt(2, patient.getDoctor().getDoctor_id());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        patient.setPatient_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE patients SET doctor_id = ? WHERE patient_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, patient.getDoctor().getDoctor_id());
                stmt.setInt(2, patient.getPatient_id());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    // Hasta güncellendiğinde User bilgileri de güncellenmeli
                    return userDao.save(patient);
                }
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        // Önce ilgili hastanın user_id'sini almalıyız
        Patient patient = findById(id);
        if (patient == null) {
            return false;
        }

        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Hasta silindikten sonra User da silinmeli
                return userDao.delete(patient.getUser_id());
            }
        }

        return false;
    }

    @Override
    public Patient findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE user_id = ?";
        Patient patient = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    patient = mapResultSetToPatient(rs);
                }
            }
        }

        return patient;
    }

    @Override
    public List<Patient> findByDoctorId(Integer doctorId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE doctor_id = ?";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Patient patient = mapResultSetToPatient(rs);
                    if (patient != null) {
                        patients.add(patient);
                    }
                }
            }
        }

        return patients;
    }

    @Override
    public boolean changeDoctor(Integer patientId, Integer newDoctorId) throws SQLException {
        String sql = "UPDATE patients SET doctor_id = ? WHERE patient_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newDoctorId);
            stmt.setInt(2, patientId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // ResultSet'ten Patient nesnesine dönüştürme yardımcı metodu
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        int patientId = rs.getInt("patient_id");
        int userId = rs.getInt("user_id");
        int doctorId = rs.getInt("doctor_id");

        User user = userDao.findById(userId);
        if (user == null) {
            return null;
        }

        Patient patient = new Patient(user);
        patient.setPatient_id(patientId);

        // Doktor bilgisini ekle
        Doctor doctor = doctorDao.findById(doctorId);
        if (doctor != null) {
            patient.setDoctor(doctor);
        }

        return patient;
    }
}