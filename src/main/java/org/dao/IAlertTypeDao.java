package org.dao;

import org.model.AlertType;
import java.sql.SQLException;

/**
 * Uyarı tipi veri erişimi için arayüz.
 */
public interface IAlertTypeDao extends IGenericDao<AlertType, Integer> {

    /**
     * İsme göre uyarı tipi bulma
     * @param name Uyarı tipi adı
     * @return Bulunan uyarı tipi veya null
     * @throws SQLException SQL hatası durumunda
     */
    AlertType findByName(String name) throws SQLException;
}