package org.service;

import org.dao.UserDao;
import org.model.User;
import java.sql.Connection;

public class AuthenticationService {
    private final UserDao userDao;

    public AuthenticationService(Connection conn) {
        this.userDao = new UserDao(conn);
    }

    public User authenticate(String tc, String password) {
        try {
            return userDao.findByTcAndPassword(tc, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
