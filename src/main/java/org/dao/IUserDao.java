package org.dao;

import org.model.User;
import java.sql.SQLException;

/**
 * Kullanıcı veri erişimi için arayüz.
 */
public interface IUserDao extends IGenericDao<User, Integer> {

    /**
     * TC Kimlik numarasına göre kullanıcı bulma
     * @param tcKimlik TC Kimlik numarası
     * @return Bulunan kullanıcı veya null
     * @throws SQLException SQL hatası durumunda
     */
    User findByTcKimlik(String tcKimlik) throws SQLException;

    /**
     * E-posta adresine göre kullanıcı bulma
     * @param email E-posta adresi
     * @return Bulunan kullanıcı veya null
     * @throws SQLException SQL hatası durumunda
     */
    User findByEmail(String email) throws SQLException;

    /**
     * Kullanıcı girişi doğrulama
     * @param tcKimlik TC Kimlik numarası
     * @param password Şifre
     * @return Giriş başarılı ise kullanıcı, değilse null
     * @throws SQLException SQL hatası durumunda
     */
    User authenticate(String tcKimlik, String password) throws SQLException;

    /**
     * Son giriş zamanını güncelleme
     * @param userId Kullanıcı ID'si
     * @return İşlem başarılı ise true, değilse false
     * @throws SQLException SQL hatası durumunda
     */
    boolean updateLastLogin(Integer userId) throws SQLException;
}