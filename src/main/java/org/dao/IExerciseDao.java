package org.dao;

import org.model.Exercise;
import java.sql.SQLException;
import java.util.List;

/**
 * Egzersiz veri erişimi için arayüz.
 */
public interface IExerciseDao extends IGenericDao<Exercise, Integer> {

    /**
     * İsme göre egzersiz bulma
     * @param name Egzersiz adı
     * @return Bulunan egzersiz veya null
     * @throws SQLException SQL hatası durumunda
     */
    Exercise findByName(String name) throws SQLException;

    /**
     * Hastanın egzersizlerini bulma
     * @param patientId Hasta ID'si
     * @return Egzersizlerin listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<Exercise> getPatientExercises(Integer patientId) throws SQLException;

    /**
     * Hastaya egzersiz atama
     * @param patientId Hasta ID'si
     * @param exerciseId Egzersiz ID'si
     * @param doctorId Doktor ID'si
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean assignExerciseToPatient(Integer patientId, Integer exerciseId, Integer doctorId) throws SQLException;
}