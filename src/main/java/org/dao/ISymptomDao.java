package org.dao;

import org.model.Symptom;
import java.sql.SQLException;
import java.util.List;

/**
 * Belirti veri erişimi için arayüz.
 */
public interface ISymptomDao extends IGenericDao<Symptom, Integer> {

    /**
     * İsme göre belirti bulma
     * @param name Belirti adı
     * @return Bulunan belirti veya null
     * @throws SQLException SQL hatası durumunda
     */
    Symptom findByName(String name) throws SQLException;

    /**
     * Hastanın belirtilerini bulma
     * @param patientId Hasta ID'si
     * @return Belirtilerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<Symptom> getPatientSymptoms(Integer patientId) throws SQLException;

    /**
     * Hastaya belirti ekleme
     * @param patientId Hasta ID'si
     * @param symptomId Belirti ID'si
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean addSymptomToPatient(Integer patientId, Integer symptomId) throws SQLException;

    /**
     * Kural ile ilişkili belirtileri bulma
     * @param ruleId Kural ID'si
     * @return Belirtilerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<Symptom> getRuleSymptoms(Integer ruleId) throws SQLException;
}