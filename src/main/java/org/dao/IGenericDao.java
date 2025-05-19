package org.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Tüm DAO sınıfları için temel operasyonları tanımlayan jenerik arayüz.
 * @param <T> İşlem yapılacak varlık tipi
 * @param <ID> Varlık ID türü
 */
public interface IGenericDao<T, ID> {

    /**
     * ID'ye göre varlık bulma
     * @param id Varlık ID'si
     * @return Bulunan varlık veya null
     * @throws SQLException SQL hatası durumunda
     */
    T findById(ID id) throws SQLException;

    /**
     * Tüm varlıkları getirme
     * @return Varlıkların listesi
     * @throws SQLException SQL hatası durumunda
     */
    List<T> findAll() throws SQLException;

    /**
     * Varlık kaydetme veya güncelleme
     * @param entity Kaydedilecek veya güncellenecek varlık
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean save(T entity) throws SQLException;

    /**
     * Varlık silme
     * @param id Silinecek varlığın ID'si
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean delete(ID id) throws SQLException;
}