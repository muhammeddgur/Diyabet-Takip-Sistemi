package org.dao;

import org.model.DietTracking;

import java.sql.SQLException;
import java.util.List;

/**
 * DietTracking tablosu için DAO arayüzü.
 */
public interface IDietTrackingDao extends IGenericDao<DietTracking, Integer> {

    /**
     * Belirli bir hastanın tüm diyet takiplerini getirir.
     * @param patientId hasta ID'si
     * @return diyet takip listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<DietTracking> findAllByPatientId(int patientId) throws SQLException;
}
