package com.magpi.auth;

import com.magpi.db.UserDao;

public class AuthService {
    private static final AuthService INSTANCE = new AuthService();
    private final UserDao userDao = new UserDao();

    private AuthService() {}

    public static AuthService getInstance() { return INSTANCE; }

    public boolean authenticate(String username, String password) {
        try {
            return userDao.validateCredentials(username, password);
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    public void register(String username, String password) {
        try {
            userDao.createUser(username, password, "operator");
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }
}
