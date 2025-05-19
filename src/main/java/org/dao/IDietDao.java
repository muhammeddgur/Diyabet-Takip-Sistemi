package org.dao;

import org.model.Diet;
import java.sql.SQLException;
import java.util.List;

/**
 * Diyet veri erişimi için arayüz.
 */
public interface IDietDao extends IGenericDao<Diet, Integer> {

    /**
     * İsme göre diyet bulma
     * @param name Diyet adı
     * @return Bulunan diyet veya null
     * @throws SQLException SQL hatası durumunda
     */
    Diet findByName(String name) throws SQLException;

    /**
     * Hastanın diyetlerini bulma
     * @param patientId Hasta ID'si
     * @return Diyetlerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<Diet> getPatientDiets(Integer patientId) throws SQLException;

    /**
     * Hastaya diyet atama
     * @param patientId Hasta ID'si
     * @param dietId Diyet ID'si
     * @param doctorId Doktor ID'si
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean assignDietToPatient(Integer patientId, Integer dietId, Integer doctorId) throws SQLException;
}