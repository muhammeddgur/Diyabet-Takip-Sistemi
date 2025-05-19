package org.dao;

import org.model.InsulinRecommendation;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * İnsülin önerisi veri erişimi için arayüz.
 */
public interface IInsulinRecommendationDao extends IGenericDao<InsulinRecommendation, Integer> {

    /**
     * Hastaya göre önerileri bulma
     * @param patientId Hasta ID'si
     * @return Önerilerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<InsulinRecommendation> findByPatientId(Integer patientId) throws SQLException;

    /**
     * Tarihe göre öneri bulma
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Öneri veya null
     * @throws SQLException SQL hatası durumunda
     */
    InsulinRecommendation findByDate(Integer patientId, LocalDate date) throws SQLException;

    /**
     * Tarih aralığına göre önerileri bulma
     * @param patientId Hasta ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Önerilerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<InsulinRecommendation> findByDateRange(Integer patientId, LocalDate startDate, LocalDate endDate) throws SQLException;

    /**
     * Öneriyi uygulandı olarak işaretleme
     * @param recommendationId Öneri ID'si
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean markAsApplied(Integer recommendationId) throws SQLException;
}