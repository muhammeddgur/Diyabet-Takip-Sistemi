package org.dao;

import org.model.Alert;
import java.sql.SQLException;
import java.util.List;

/**
 * Uyarı veri erişimi için arayüz.
 */
public interface IAlertDao extends IGenericDao<Alert, Integer> {

    /**
     * Hastaya göre uyarıları bulma
     * @param patientId Hasta ID'si
     * @return Uyarıların listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<Alert> findByPatientId(Integer patientId) throws SQLException;

    /**
     * Doktora göre uyarıları bulma
     * @param doctorId Doktor ID'si
     * @return Uyarıların listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<Alert> findByDoctorId(Integer doctorId) throws SQLException;

    /**
     * Okunmamış uyarıları bulma
     * @param doctorId Doktor ID'si
     * @return Okunmamış uyarıların listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<Alert> findUnreadAlerts(Integer doctorId) throws SQLException;

    /**
     * Uyarıyı okundu olarak işaretleme
     * @param alertId Uyarı ID'si
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean markAsRead(Integer alertId) throws SQLException;
}