package org.dao;

import org.model.BloodSugarMeasurement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Kan şekeri ölçümü veri erişimi için arayüz.
 */
public interface IMeasurementDao extends IGenericDao<BloodSugarMeasurement, Integer> {

    /**
     * Hastaya göre ölçümleri bulma
     * @param patientId Hasta ID'si
     * @return Ölçümlerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<BloodSugarMeasurement> findByPatientId(Integer patientId) throws SQLException;

    /**
     * Tarih aralığına göre ölçümleri bulma
     * @param patientId Hasta ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Ölçümlerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<BloodSugarMeasurement> findByDateRange(Integer patientId, LocalDate startDate, LocalDate endDate) throws SQLException;

    /**
     * Ölçüm zamanına göre ölçümleri bulma (sabah, öğle vb.)
     * @param patientId Hasta ID'si
     * @param period Ölçüm zamanı (sabah, ogle, ikindi, aksam, gece)
     * @return Ölçümlerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<BloodSugarMeasurement> findByPeriod(Integer patientId, String period) throws SQLException;

    /**
     * Günlük ortalama ölçümü hesaplama
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Ortalama ölçüm değeri
     * @throws SQLException SQL hatası durumunda
     */
    double getDailyAverage(Integer patientId, LocalDate date) throws SQLException;

    /**
     * Son n ölçümü getirme
     * @param patientId Hasta ID'si
     * @param count İstenilen ölçüm sayısı
     * @return Ölçümlerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<BloodSugarMeasurement> getLastMeasurements(Integer patientId, int count) throws SQLException;
}