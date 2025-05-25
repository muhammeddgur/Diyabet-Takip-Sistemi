package org.dao;

import org.model.PatientDiet;
import java.sql.SQLException;
import java.util.List;

/**
 * Hasta-Diyet ilişkisine özel DAO arayüzü.
 */
public interface IPatientDietsDao extends IGenericDao<PatientDiet, Integer> {

    /**
     * Belirli bir hastanın tüm diyet ilişkilerini getirir.
     * @param patientId Hasta ID'si
     * @return İlgili hasta-diyet ilişkileri listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<PatientDiet> findByPatientId(int patientId) throws SQLException;

    /**
     * Belirli bir doktorun eklediği hasta-diyet ilişkilerini getirir.
     * @param doctorId Doktor ID'si
     * @return Doktorun atadığı diyet ilişkileri listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<PatientDiet> findByDoctorId(int doctorId) throws SQLException;

    /**
     * Hasta ve diyet ID'sine göre ilişkiyi getirir.
     * @param patientId Hasta ID'si
     * @param dietId Diyet ID'si
     * @return Hasta-diyet ilişkisi veya null
     * @throws SQLException SQL hatası durumunda
     */
    PatientDiet findByPatientAndDiet(int patientId, int dietId) throws SQLException;
}
