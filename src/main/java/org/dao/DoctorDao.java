package org.dao;

import org.model.Doctor;
import org.model.Patient;
import org.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DoctorDao arayüzünün implementasyonu.
 */
public class DoctorDao implements IDoctorDao {

    private final DatabaseConnectionManager connectionManager;
    private final UserDao userDao;

    public DoctorDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
        userDao = new UserDao();
    }

    @Override
    public Doctor findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        Doctor doctor = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    User user = userDao.findById(userId);
                    if (user != null) {
                        doctor = new Doctor(user);
                        doctor.setDoctor_id(id);
                    }
                }
            }
        }

        return doctor;
    }

    @Override
    public List<Doctor> findAll() throws SQLException {
        String sql = "SELECT * FROM doctors";
        List<Doctor> doctors = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int doctorId = rs.getInt("doctor_id");
                int userId = rs.getInt("user_id");
                User user = userDao.findById(userId);
                if (user != null) {
                    Doctor doctor = new Doctor(user);
                    doctor.setDoctor_id(doctorId);
                    doctors.add(doctor);
                }
            }
        }

        return doctors;
    }

    @Override
    public boolean save(Doctor doctor) throws SQLException {
        if (doctor.getUser_id() == null) {
            // Önce User kaydedilmeli
            boolean userSaved = userDao.save(doctor);
            if (!userSaved) {
                return false;
            }
        }

        if (doctor.getDoctor_id() == null) {
            // Insert
            String sql = "INSERT INTO doctors (user_id) VALUES (?) RETURNING doctor_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, doctor.getUser_id());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        doctor.setDoctor_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update (sadece User güncellenir, doctor-user ilişkisi değişmez)
            return userDao.save(doctor);
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        // Önce ilgili doktorun user_id'sini almalıyız
        Doctor doctor = findById(id);
        if (doctor == null) {
            return false;
        }

        String sql = "DELETE FROM doctors WHERE doctor_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Doktor silindikten sonra User da silinmeli
                return userDao.delete(doctor.getUser_id());
            }
        }

        return false;
    }

    @Override
    public Doctor findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE user_id = ?";
        Doctor doctor = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int doctorId = rs.getInt("doctor_id");
                    User user = userDao.findById(userId);
                    if (user != null) {
                        doctor = new Doctor(user);
                        doctor.setDoctor_id(doctorId);
                    }
                }
            }
        }

        return doctor;
    }

    @Override
    public int getPatientCount(Integer doctorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients WHERE doctor_id = ?";
        int count = 0;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        }

        return count;
    }

    @Override
    public List<Patient> getPatients(Integer doctorId) throws SQLException {
        String sql = "SELECT p.patient_id, p.user_id FROM patients p WHERE p.doctor_id = ?";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int patientId = rs.getInt("patient_id");
                    int userId = rs.getInt("user_id");

                    User user = userDao.findById(userId);
                    if (user != null) {
                        Patient patient = new Patient(user);
                        patient.setPatient_id(patientId);

                        // Doktor bilgisini ekle
                        Doctor doctor = this.findById(doctorId);
                        if (doctor != null) {
                            patient.setDoctor(doctor);
                        }

                        patients.add(patient);
                    }
                }
            }
        }

        return patients;
    }
}