package org.dao;

import org.model.User;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDao arayüzünün implementasyonu.
 */
public class UserDao implements IUserDao {

    private final DatabaseConnectionManager connectionManager;

    public UserDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public User findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        User user = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }
        }

        return user;
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    @Override
    public boolean save(User user) throws SQLException {
        // Temel null kontrolü
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // TC kimlik ve email kontrolü
        if (user.getTc_kimlik() == null || user.getTc_kimlik().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("TC kimlik ve email zorunludur");
        }

        if (user.getUser_id() == null) {
            // Insert
            String sql = "INSERT INTO users (tc_kimlik, password, email, ad, soyad, dogum_tarihi, cinsiyet, " +
                    "profil_resmi, kullanici_tipi, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING user_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, user.getTc_kimlik());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getAd());
                stmt.setString(5, user.getSoyad());
                stmt.setDate(6, java.sql.Date.valueOf(user.getDogum_tarihi()));
                stmt.setString(7, String.valueOf(user.getCinsiyet()));
                stmt.setBytes(8, user.getProfil_resmi());
                stmt.setString(9, user.getKullanici_tipi());
                stmt.setTimestamp(10, Timestamp.valueOf(user.getCreated_at() != null ? user.getCreated_at() : LocalDateTime.now()));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        user.setUser_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE users SET tc_kimlik = ?, password = ?, email = ?, ad = ?, soyad = ?, " +
                    "dogum_tarihi = ?, cinsiyet = ?, profil_resmi = ?, kullanici_tipi = ? WHERE user_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, user.getTc_kimlik());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getAd());
                stmt.setString(5, user.getSoyad());
                stmt.setDate(6, java.sql.Date.valueOf(user.getDogum_tarihi()));
                stmt.setString(7, String.valueOf(user.getCinsiyet()));
                stmt.setBytes(8, user.getProfil_resmi());
                stmt.setString(9, user.getKullanici_tipi());
                stmt.setInt(10, user.getUser_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public User findByTcKimlik(String tcKimlik) throws SQLException {
        String sql = "SELECT * FROM users WHERE tc_kimlik = ?";
        User user = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tcKimlik);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }
        }

        return user;
    }

    @Override
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        User user = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }
        }

        return user;
    }

    @Override
    public User authenticate(String tcKimlik, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE tc_kimlik = ? AND password = ?";
        User user = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tcKimlik);
            stmt.setString(2, password); // Normalde şifre hash'lenmiş olarak karşılaştırılmalıdır

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                    updateLastLogin(user.getUser_id());
                }
            }
        }

        return user;
    }

    @Override
    public boolean updateLastLogin(Integer userId) throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE user_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // ResultSet'ten User nesnesine dönüştürme yardımcı metodu
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUser_id(rs.getInt("user_id"));
        user.setTc_kimlik(rs.getString("tc_kimlik"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setAd(rs.getString("ad"));
        user.setSoyad(rs.getString("soyad"));
        user.setDogum_tarihi(rs.getDate("dogum_tarihi").toLocalDate());
        user.setCinsiyet(rs.getString("cinsiyet").charAt(0));
        user.setProfil_resmi(rs.getBytes("profil_resmi"));
        user.setKullanici_tipi(rs.getString("kullanici_tipi"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreated_at(createdAt.toLocalDateTime());
        }

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLast_login(lastLogin.toLocalDateTime());
        }

        return user;
    }
}