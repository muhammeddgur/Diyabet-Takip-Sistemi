package org.service;

import org.dao.AlertDao;
import org.model.Alert;
import java.time.LocalDateTime;
import java.sql.Connection;

public class AlertService {
    private final AlertDao dao;
    public AlertService(Connection conn) { this.dao = new AlertDao(conn); }

    public void sendAlert(int patientId, String type, String message) {
        Alert a = new Alert(0, null, type, message, LocalDateTime.now());
        try {
            dao.save(a);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
