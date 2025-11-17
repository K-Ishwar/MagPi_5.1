package com.magpi.db;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UserDao {
    public static class User {
        public final int id;
        public final String username;
        public final String role;
        public User(int id, String username, String role) {
            this.id = id; this.username = username; this.role = role;
        }
    }

    public void ensureDefaultAdmin() throws SQLException {
        if (findByUsername("admin") == null) {
            createUser("admin", "admin123", "admin");
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, role FROM users WHERE username = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
                }
                return null;
            }
        }
    }

    public boolean validateCredentials(String username, String password) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String hash = rs.getString("password_hash");
                return BCrypt.checkpw(password, hash);
            }
        }
    }

    public User createUser(String username, String password, String role) throws SQLException {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users(username, password_hash, role) VALUES (?,?,?)";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, role == null ? "operator" : role);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int id = rs.next() ? rs.getInt(1) : -1;
                return new User(id, username, role);
            }
        }
    }
}
