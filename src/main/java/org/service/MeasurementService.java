package org.service;

import org.dao.MeasurementDao;
import org.model.BloodSugarMeasurement;
import java.time.LocalDateTime;
import java.sql.Connection;

public class MeasurementService {
    private final MeasurementDao dao;
    public MeasurementService(Connection conn) { this.dao = new MeasurementDao(conn); }

    public void recordMeasurement(int patientId, double value) {
        BloodSugarMeasurement m = new BloodSugarMeasurement(0, null, value, LocalDateTime.now());
        try {
            dao.save(m);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
