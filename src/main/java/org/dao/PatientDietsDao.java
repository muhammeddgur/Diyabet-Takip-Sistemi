package org.dao;

import org.model.PatientDiet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDietsDao implements IPatientDietsDao {

    private final DatabaseConnectionManager dbManager;

    public PatientDietsDao(){
        dbManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public PatientDiet findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM patient_diets WHERE patient_diet_id = ?";
        try (Connection connection = dbManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapToPatientDiet(rs);
            }
        }
        return null;
    }

    @Override
    public List<PatientDiet> findAll() throws SQLException {
        String sql = "SELECT * FROM patient_diets";
        List<PatientDiet> list = new ArrayList<>();
        try (Connection connection = dbManager.getConnection();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapToPatientDiet(rs));
            }
        }
        return list;
    }

    @Override
    public boolean save(PatientDiet entity) throws SQLException {
        if (entity.getPatientDietId() > 0) {
            // Güncelleme
            String sql = "UPDATE patient_diets SET patient_id = ?, diet_id = ?, doctor_id = ?, baslangic_tarihi = ? WHERE patient_diet_id = ?";
            try (Connection connection = dbManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, entity.getPatientId());
                ps.setInt(2, entity.getDietId());
                ps.setInt(3, entity.getDoctorId());
                ps.setDate(4, Date.valueOf(entity.getBaslangicTarihi()));
                ps.setInt(5, entity.getPatientDietId());
                return ps.executeUpdate() > 0;
            }
        } else {
            // Yeni kayıt
            String sql = "INSERT INTO patient_diets (patient_id, diet_id, doctor_id, baslangic_tarihi) VALUES (?, ?, ?, ?)";
            try (Connection connection = dbManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, entity.getPatientId());
                ps.setInt(2, entity.getDietId());
                ps.setInt(3, entity.getDoctorId());
                ps.setDate(4, Date.valueOf(entity.getBaslangicTarihi()));
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) {
                        entity.setPatientDietId(keys.getInt(1));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM patient_diets WHERE patient_diet_id = ?";
        try (Connection connection = dbManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<PatientDiet> findByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM patient_diets WHERE patient_id = ?";
        List<PatientDiet> list = new ArrayList<>();
        try (Connection connection = dbManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToPatientDiet(rs));
            }
        }
        return list;
    }

    @Override
    public List<PatientDiet> findByDoctorId(int doctorId) throws SQLException {
        String sql = "SELECT * FROM patient_diets WHERE doctor_id = ?";
        List<PatientDiet> list = new ArrayList<>();
        try (Connection connection = dbManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToPatientDiet(rs));
            }
        }
        return list;
    }

    public Integer findLatestPatientDietIdByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT patient_diet_id FROM patient_diets WHERE patient_id = ? ORDER BY baslangic_tarihi DESC LIMIT 1";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("patient_diet_id");
            } else {
                throw new SQLException("Bu hastaya ait bir diyet kaydı bulunamadı.");
            }

        }
    }


    @Override
    public PatientDiet findByPatientAndDiet(int patientId, int dietId) throws SQLException {
        String sql = "SELECT * FROM patient_diets WHERE patient_id = ? AND diet_id = ?";
        try (Connection connection = dbManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, dietId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapToPatientDiet(rs);
            }
        }
        return null;
    }

    private PatientDiet mapToPatientDiet(ResultSet rs) throws SQLException {
        PatientDiet pd = new PatientDiet();
        pd.setPatientDietId(rs.getInt("patient_diet_id"));
        pd.setPatientId(rs.getInt("patient_id"));
        pd.setDietId(rs.getInt("diet_id"));
        pd.setDoctorId(rs.getInt("doctor_id"));
        pd.setBaslangicTarihi(rs.getDate("baslangic_tarihi").toLocalDate());
        return pd;
    }
}
