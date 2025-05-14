package org.dao;

import org.model.User;
import org.util.DatabaseConnection;
import org.util.Password;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-05-14 15:45:49
 * Current User's Login: Emirhan-Karabulut
 *
 * User tablosu için veritabanı işlemlerini yöneten DAO sınıfı
 */
public class UserDao {
    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());

    /**
     * Yeni bir kullanıcı ekler
     *
     * @param user Eklenecek kullanıcı nesnesi
     * @return Eklenen kullanıcının ID'si, başarısızsa null
     * @throws SQLException SQL hatası durumunda
     */
    public Integer save(User user) throws SQLException {
        // SQL sorgusu - users tablosuna veri ekleme
        String sql = "INSERT INTO users (tc_identity, first_name, last_name, password, email, " +
                "birth_date, gender, user_type, profile_photo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Bağlantı al
            conn = DatabaseConnection.getConnection();

            // Şifreyi hashle (güvenlik için)
            String hashedPassword = Password.hashPassword(user.getPassword());

            // PreparedStatement oluştur ve parametreleri ayarla
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getTcIdentity());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, hashedPassword);
            stmt.setString(5, user.getEmail());

            // Doğum tarihi null olabilir
            if (user.getBirthDate() != null) {
                stmt.setDate(6, Date.valueOf(user.getBirthDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setString(7, user.getGender());
            stmt.setString(8, user.getUserType());

            // Profil fotoğrafı null olabilir
            if (user.getProfilePhoto() != null) {
                stmt.setBytes(9, user.getProfilePhoto());
            } else {
                stmt.setNull(9, Types.BINARY);
            }

            // Sorguyu çalıştır
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Kullanıcı oluşturma başarısız, hiç satır etkilenmedi.");
            }

            // Oluşturulan ID'yi al
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Kullanıcı oluşturma başarısız, ID alınamadı.");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Kullanıcı kaydedilirken hata oluştu", e);
            throw e;
        } finally {
            // Kaynakları temizle
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    /**
     * Kullanıcı bilgilerini günceller
     *
     * @param user Güncellenecek kullanıcı nesnesi
     * @throws SQLException SQL hatası durumunda
     */
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, " +
                "birth_date = ?, gender = ?, profile_photo = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getEmail());

            if (user.getBirthDate() != null) {
                stmt.setDate(4, Date.valueOf(user.getBirthDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, user.getGender());

            if (user.getProfilePhoto() != null) {
                stmt.setBytes(6, user.getProfilePhoto());
            } else {
                stmt.setNull(6, Types.BINARY);
            }

            stmt.setInt(7, user.getUserId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Kullanıcı güncellenirken hata oluştu", e);
            throw e;
        }
    }

    /**
     * Kullanıcının şifresini değiştirir
     *
     * @param userId Kullanıcı ID
     * @param newPassword Yeni şifre
     * @throws SQLException SQL hatası durumunda
     */
    public void changePassword(Integer userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Yeni şifreyi hashle
            String hashedPassword = Password.hashPassword(newPassword);

            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);

            stmt.executeUpdate();
            LOGGER.info("Kullanıcı şifresi başarıyla değiştirildi. UserId: " + userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Şifre değiştirilirken hata oluştu", e);
            throw e;
        }
    }

    /**
     * ID'ye göre kullanıcı arar
     *
     * @param userId Kullanıcı ID
     * @return Kullanıcı nesnesi, bulunamazsa null
     * @throws SQLException SQL hatası durumunda
     */
    public User findById(Integer userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "ID'ye göre kullanıcı bulunurken hata oluştu", e);
            throw e;
        }

        return null;
    }

    /**
     * TC kimlik numarasına göre kullanıcı arar
     *
     * @param tcIdentity TC kimlik numarası
     * @return Kullanıcı nesnesi, bulunamazsa null
     * @throws SQLException SQL hatası durumunda
     */
    public User findByTcIdentity(String tcIdentity) throws SQLException {
        String sql = "SELECT * FROM users WHERE tc_identity = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tcIdentity);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TC kimlik numarasına göre kullanıcı bulunurken hata oluştu", e);
            throw e;
        }

        return null;
    }

    /**
     * E-posta adresine göre kullanıcı arar
     *
     * @param email E-posta adresi
     * @return Kullanıcı nesnesi, bulunamazsa null
     * @throws SQLException SQL hatası durumunda
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "E-posta adresine göre kullanıcı bulunurken hata oluştu", e);
            throw e;
        }

        return null;
    }

    /**
     * Tüm kullanıcıları listeler
     *
     * @return Kullanıcı listesi
     * @throws SQLException SQL hatası durumunda
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Tüm kullanıcılar listelenirken hata oluştu", e);
            throw e;
        }

        return users;
    }

    /**
     * Kullanıcı tipine göre kullanıcıları listeler
     *
     * @param userType Kullanıcı tipi (DOCTOR, PATIENT)
     * @return Kullanıcı listesi
     * @throws SQLException SQL hatası durumunda
     */
    public List<User> findByUserType(String userType) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_type = ?";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Kullanıcı tipine göre listeleme hatası", e);
            throw e;
        }

        return users;
    }

    /**
     * Kullanıcıyı siler
     *
     * @param userId Silinecek kullanıcının ID'si
     * @throws SQLException SQL hatası durumunda
     */
    public void delete(Integer userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Kullanıcı silinirken hata oluştu", e);
            throw e;
        }
    }

    /**
     * ResultSet'ten User nesnesine dönüşüm yapar
     *
     * @param rs ResultSet
     * @return User nesnesi
     * @throws SQLException SQL hatası durumunda
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setUserId(rs.getInt("user_id"));
        user.setTcIdentity(rs.getString("tc_identity"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));

        // Date null olabilir
        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            user.setBirthDate(birthDate.toLocalDate());
        }

        user.setGender(rs.getString("gender"));
        user.setUserType(rs.getString("user_type"));
        user.setProfilePhoto(rs.getBytes("profile_photo"));

        // Timestamp'ler
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        return user;
    }
}