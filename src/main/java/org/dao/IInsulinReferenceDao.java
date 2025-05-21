package org.dao;

import org.model.InsulinReference;
import java.sql.SQLException;
import java.util.List;

/**
 * İnsülin referans değerleri için DAO arayüzü
 */
public interface IInsulinReferenceDao {

    /**
     * ID'ye göre referans bilgisi bulur
     * @param id Referans ID
     * @return Referans bilgisi veya null
     * @throws SQLException
     */
    InsulinReference findById(Integer id) throws SQLException;

    /**
     * Tüm referans bilgilerini getirir
     * @return Referans bilgilerinin listesi
     * @throws SQLException
     */
    List<InsulinReference> findAll() throws SQLException;

    /**
     * Referans bilgisi kaydeder veya günceller
     * @param reference Kaydedilecek veya güncellenecek referans bilgisi
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException
     */
    boolean save(InsulinReference reference) throws SQLException;

    /**
     * Referans bilgisi siler
     * @param id Silinecek referans ID
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException
     */
    boolean delete(Integer id) throws SQLException;

    /**
     * Kan şekeri değerine göre uygun insülin referansını bulur
     * @param bloodSugarValue Kan şekeri değeri
     * @return İnsülin referans bilgisi veya null
     * @throws SQLException
     */
    InsulinReference findByBloodSugarValue(Integer bloodSugarValue) throws SQLException;
}