package org.dao;

import org.model.Doctor;
import org.model.Patient;
import java.sql.SQLException;
import java.util.List;

/**
 * Doktor veri erişimi için arayüz.
 */
public interface IDoctorDao extends IGenericDao<Doctor, Integer> {

    /**
     * Kullanıcı ID'sine göre doktor bulma
     * @param userId Kullanıcı ID'si
     * @return Bulunan doktor veya null
     * @throws SQLException SQL hatası durumunda
     */
    Doctor findByUserId(Integer userId) throws SQLException;

    /**
     * Doktorun hasta sayısını getirme
     * @param doctorId Doktor ID'si
     * @return Hasta sayısı
     * @throws SQLException SQL hatası durumunda
     */
    int getPatientCount(Integer doctorId) throws SQLException;

    /**
     * Doktorun hastalarını getirme
     * @param doctorId Doktor ID'si
     * @return Hastaların listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<Patient> getPatients(Integer doctorId) throws SQLException;
}