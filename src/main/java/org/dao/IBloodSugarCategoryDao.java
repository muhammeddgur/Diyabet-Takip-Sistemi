package org.dao;

import org.model.BloodSugarCategory;
import java.sql.SQLException;
import java.util.List;

/**
 * Kan şekeri kategorileri için DAO arayüzü
 */
public interface IBloodSugarCategoryDao {

    /**
     * ID'ye göre kategori bulur
     * @param id Kategori ID
     * @return Kategori veya null
     * @throws SQLException
     */
    BloodSugarCategory findById(Integer id) throws SQLException;

    /**
     * Tüm kategorileri getirir
     * @return Kategorilerin listesi
     * @throws SQLException
     */
    List<BloodSugarCategory> findAll() throws SQLException;

    /**
     * Kategori kaydeder veya günceller
     * @param category Kaydedilecek veya güncellenecek kategori
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException
     */
    boolean save(BloodSugarCategory category) throws SQLException;

    /**
     * Kategori siler
     * @param id Silinecek kategori ID
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException
     */
    boolean delete(Integer id) throws SQLException;

    /**
     * Kan şekeri değerine göre kategori bulur
     * @param bloodSugarValue Kan şekeri değeri
     * @return Kategori veya null
     * @throws SQLException
     */
    BloodSugarCategory findByBloodSugarValue(Integer bloodSugarValue) throws SQLException;
}