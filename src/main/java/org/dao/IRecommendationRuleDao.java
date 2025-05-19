package org.dao;

import org.model.RecommendationRule;
import java.sql.SQLException;
import java.util.List;

/**
 * Öneri kuralları veri erişimi için arayüz.
 */
public interface IRecommendationRuleDao extends IGenericDao<RecommendationRule, Integer> {

    /**
     * Kan şekeri seviyesine göre kural bulma
     * @param level Kan şekeri seviyesi
     * @return Uygun kuralların listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<RecommendationRule> findByBloodSugarLevel(int level) throws SQLException;

    /**
     * Belirtilere göre kural bulma
     * @param symptomIds Belirti ID'leri listesi
     * @return Uygun kuralların listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<RecommendationRule> findBySymptoms(List<Integer> symptomIds) throws SQLException;

    /**
     * Kurala belirti ekleme
     * @param ruleId Kural ID'si
     * @param symptomId Belirti ID'si
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean addSymptomToRule(Integer ruleId, Integer symptomId) throws SQLException;
}