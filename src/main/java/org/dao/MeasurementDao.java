package org.dao;

import org.model.BloodSugarMeasurement;
import org.model.Patient;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MeasurementDao arayüzünün implementasyonu.
 */
public class MeasurementDao implements IMeasurementDao {

    private final DatabaseConnectionManager connectionManager;
    private final PatientDao patientDao;

    public MeasurementDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
        patientDao = new PatientDao();
    }

    @Override
    public BloodSugarMeasurement findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE measurement_id = ?";
        BloodSugarMeasurement measurement = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    measurement = mapResultSetToMeasurement(rs);
                }
            }
        }

        return measurement;
    }

    @Override
    public List<BloodSugarMeasurement> findAll() throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BloodSugarMeasurement measurement = mapResultSetToMeasurement(rs);
                if (measurement != null) {
                    measurements.add(measurement);
                }
            }
        }

        return measurements;
    }

    @Override
    public boolean save(BloodSugarMeasurement measurement) throws SQLException {
        if (measurement.getMeasurement_id() == null) {
            // Insert
            String sql = "INSERT INTO blood_sugar_measurements (patient_id, olcum_degeri, olcum_zamani, olcum_tarihi, insülin_miktari, is_valid_time, category_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING measurement_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, measurement.getPatient_id());
                stmt.setInt(2, measurement.getOlcum_degeri());
                stmt.setString(3, measurement.getOlcum_zamani());
                stmt.setTimestamp(4, Timestamp.valueOf(measurement.getOlcum_tarihi()));

                if (measurement.getInsulin_miktari() != null) {
                    stmt.setDouble(5, measurement.getInsulin_miktari());
                } else {
                    stmt.setNull(5, Types.NUMERIC);
                }

                stmt.setBoolean(6, measurement.getIs_valid_time());

                if (measurement.getCategory_id() != null) {
                    stmt.setInt(7, measurement.getCategory_id());
                } else {
                    stmt.setNull(7, Types.INTEGER);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        measurement.setMeasurement_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE blood_sugar_measurements SET patient_id = ?, olcum_degeri = ?, olcum_zamani = ?, " +
                    "olcum_tarihi = ?, insülin_miktari = ?, is_valid_time = ?, category_id = ? WHERE measurement_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, measurement.getPatient_id());
                stmt.setInt(2, measurement.getOlcum_degeri());
                stmt.setString(3, measurement.getOlcum_zamani());
                stmt.setTimestamp(4, Timestamp.valueOf(measurement.getOlcum_tarihi()));

                if (measurement.getInsulin_miktari() != null) {
                    stmt.setDouble(5, measurement.getInsulin_miktari());
                } else {
                    stmt.setNull(5, Types.NUMERIC);
                }

                stmt.setBoolean(6, measurement.getIs_valid_time());

                if (measurement.getCategory_id() != null) {
                    stmt.setInt(7, measurement.getCategory_id());
                } else {
                    stmt.setNull(7, Types.INTEGER);
                }

                stmt.setInt(8, measurement.getMeasurement_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM blood_sugar_measurements WHERE measurement_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<BloodSugarMeasurement> findByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? ORDER BY olcum_tarihi DESC";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BloodSugarMeasurement measurement = mapResultSetToMeasurement(rs);
                    if (measurement != null) {
                        measurements.add(measurement);
                    }
                }
            }
        }

        return measurements;
    }

    @Override
    public List<BloodSugarMeasurement> findByDateRange(Integer patientId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? " +
                "AND olcum_tarihi >= ? AND olcum_tarihi < ? ORDER BY olcum_tarihi";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setTimestamp(2, Timestamp.valueOf(startDate.atStartOfDay()));
            stmt.setTimestamp(3, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BloodSugarMeasurement measurement = mapResultSetToMeasurement(rs);
                    if (measurement != null) {
                        measurements.add(measurement);
                    }
                }
            }
        }

        return measurements;
    }

    @Override
    public List<BloodSugarMeasurement> findByPeriod(Integer patientId, String period) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? AND olcum_zamani = ? ORDER BY olcum_tarihi DESC";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setString(2, period);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BloodSugarMeasurement measurement = mapResultSetToMeasurement(rs);
                    if (measurement != null) {
                        measurements.add(measurement);
                    }
                }
            }
        }

        return measurements;
    }

    @Override
    public double getDailyAverage(Integer patientId, LocalDate date) throws SQLException {
        // Sadece geçerli saatlerde yapılan ölçümleri ortalamaya dahil et
        String sql = "SELECT AVG(olcum_degeri) FROM blood_sugar_measurements WHERE patient_id = ? " +
                "AND olcum_tarihi >= ? AND olcum_tarihi < ? AND is_valid_time = TRUE";
        double average = 0;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));
            stmt.setTimestamp(3, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    average = rs.getDouble(1);
                }
            }
        }

        return average;
    }

    public void updateInsulinAmount(Integer measurementId, Double insulinAmount) throws SQLException {
        String sql = "UPDATE blood_sugar_measurements SET insülin_miktari = ? WHERE measurement_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, insulinAmount);
            stmt.setInt(2, measurementId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Ölçüm güncellenemedi, böyle bir ID bulunamadı: " + measurementId);
            }
        }
    }
    public void updateFlag(Integer measurementId) throws SQLException {
        String sql = "UPDATE blood_sugar_measurements SET is_valid_time = ? WHERE measurement_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, true);
            stmt.setInt(2, measurementId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Ölçüm güncellenemedi, böyle bir ID bulunamadı: " + measurementId);
            }
        }
    }

    @Override
    public List<BloodSugarMeasurement> getLastMeasurements(Integer patientId, int count) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? ORDER BY olcum_tarihi DESC LIMIT ?";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, count);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BloodSugarMeasurement measurement = mapResultSetToMeasurement(rs);
                    if (measurement != null) {
                        measurements.add(measurement);
                    }
                }
            }
        }

        return measurements;
    }

    /**
     * Geçerli saatlerde yapılan ölçümleri getirir
     * @param patientId Hasta ID
     * @return Geçerli saatlerde yapılan ölçümler
     * @throws SQLException
     */
    public List<BloodSugarMeasurement> getValidTimeMeasurements(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? AND is_valid_time = TRUE ORDER BY olcum_tarihi DESC";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BloodSugarMeasurement measurement = mapResultSetToMeasurement(rs);
                    if (measurement != null) {
                        measurements.add(measurement);
                    }
                }
            }
        }

        return measurements;
    }

    /**
     * Geçersiz saatlerde yapılan ölçümleri getirir
     * @param patientId Hasta ID
     * @return Geçersiz saatlerde yapılan ölçümler
     * @throws SQLException
     */
    public List<BloodSugarMeasurement> getInvalidTimeMeasurements(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? AND is_valid_time = FALSE ORDER BY olcum_tarihi DESC";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BloodSugarMeasurement measurement = mapResultSetToMeasurement(rs);
                    if (measurement != null) {
                        measurements.add(measurement);
                    }
                }
            }
        }

        return measurements;
    }

    /**
     * Hastanın en son N adet kan şekeri ölçümünü alır - İlave metot
     *
     * @param patientId Hasta ID
     * @param count Kaç adet ölçüm alınacağı
     * @return Son ölçümler listesi
     * @throws SQLException Veritabanı hatası durumunda
     */
    public List<BloodSugarMeasurement> findLatestMeasurements(Integer patientId, int count) throws SQLException {
        return getLastMeasurements(patientId, count);
    }

    // ResultSet'ten BloodSugarMeasurement nesnesine dönüştürme yardımcı metodu
    private BloodSugarMeasurement mapResultSetToMeasurement(ResultSet rs) throws SQLException {
        BloodSugarMeasurement measurement = new BloodSugarMeasurement();
        measurement.setMeasurement_id(rs.getInt("measurement_id"));
        measurement.setOlcum_degeri(rs.getInt("olcum_degeri"));
        measurement.setOlcum_zamani(rs.getString("olcum_zamani"));
        measurement.setOlcum_tarihi(rs.getTimestamp("olcum_tarihi").toLocalDateTime());

        Double insulin = rs.getDouble("insülin_miktari");
        if (!rs.wasNull()) {
            measurement.setInsulin_miktari(insulin);
        }

        // is_valid_time değerini oku
        measurement.setIs_valid_time(rs.getBoolean("is_valid_time"));

        // category_id değerini oku
        Integer categoryId = rs.getInt("category_id");
        if (!rs.wasNull()) {
            measurement.setCategory_id(categoryId);
        }

        // Hasta bilgisini ekle
        int patientId = rs.getInt("patient_id");
        measurement.setPatient_id(patientId);
        Patient patient = patientDao.findById(patientId);
        if (patient != null) {
            measurement.setPatient(patient);
        }

        return measurement;
    }
}