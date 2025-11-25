package com.magpi.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OperatorDao {
    public List<String> getAll() throws SQLException {
        String sql = "SELECT name FROM operators ORDER BY name";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<String> list = new ArrayList<>();
            while (rs.next())
                list.add(rs.getString(1));
            return list;
        }
    }

    public void add(String name) throws SQLException {
        String sql = "INSERT OR IGNORE INTO operators(name) VALUES(?)";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    public boolean isEmpty() throws SQLException {
        String sql = "SELECT 1 FROM operators LIMIT 1";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return !rs.next();
        }
    }

    public void remove(String name) throws SQLException {
        String sql = "DELETE FROM operators WHERE name = ?";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }
}
