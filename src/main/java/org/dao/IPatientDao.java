package org.dao;

import org.model.Patient;
import java.sql.SQLException;
import java.util.List;

/**
 * Hasta veri erişimi için arayüz.
 */
public interface IPatientDao extends IGenericDao<Patient, Integer> {

    /**
     * Kullanıcı ID'sine göre hasta bulma
     * @param userId Kullanıcı ID'si
     * @return Bulunan hasta veya null
     * @throws SQLException SQL hatası durumunda
     */
    Patient findByUserId(Integer userId) throws SQLException;

    /**
     * Doktor ID'sine göre hastaları bulma
     * @param doctorId Doktor ID'si
     * @return Hastaların listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<Patient> findByDoctorId(Integer doctorId) throws SQLException;

    /**
     * Hasta için doktor değiştirme
     * @param patientId Hasta ID'si
     * @param newDoctorId Yeni doktor ID'si
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean changeDoctor(Integer patientId, Integer newDoctorId) throws SQLException;
}