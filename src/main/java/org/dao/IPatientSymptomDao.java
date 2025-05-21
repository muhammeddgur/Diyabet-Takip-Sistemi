package org.dao;

import org.model.PatientSymptom;
import org.model.Symptom;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Hasta belirtileri için DAO arayüzü
 */
public interface IPatientSymptomDao {

    /**
     * ID'ye göre hasta belirtisini bulur
     * @param id Hasta belirti ID
     * @return Hasta belirtisi veya null
     * @throws SQLException
     */
    PatientSymptom findById(Integer id) throws SQLException;

    /**
     * Hasta belirtisini kaydeder veya günceller
     * @param patientSymptom Hasta belirti nesnesi
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException
     */
    boolean save(PatientSymptom patientSymptom) throws SQLException;

    /**
     * Hasta belirtisini siler
     * @param id Silinecek hasta belirti ID
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException
     */
    boolean delete(Integer id) throws SQLException;

    /**
     * Hasta ID'sine göre belirtileri getirir
     * @param patientId Hasta ID'si
     * @return Hasta belirtilerinin listesi
     * @throws SQLException
     */
    List<PatientSymptom> findByPatientId(Integer patientId) throws SQLException;

    /**
     * Belirli bir tarihteki hasta belirtilerini getirir
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Hasta belirtilerinin listesi
     * @throws SQLException
     */
    List<PatientSymptom> findByPatientIdAndDate(Integer patientId, LocalDate date) throws SQLException;

    /**
     * Tarih aralığındaki hasta belirtilerini getirir
     * @param patientId Hasta ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Hasta belirtilerinin listesi
     * @throws SQLException
     */
    List<PatientSymptom> findByDateRange(Integer patientId, LocalDate startDate, LocalDate endDate) throws SQLException;
}