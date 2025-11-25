package com.magpi.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartDao {
    public List<String> getAll() throws SQLException {
        String sql = "SELECT description FROM parts ORDER BY description";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<String> list = new ArrayList<>();
            while (rs.next())
                list.add(rs.getString(1));
            return list;
        }
    }

    public int ensurePart(String description) throws SQLException {
        // Insert or ignore
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO parts(description) VALUES(?)")) {
            ps.setString(1, description);
            ps.executeUpdate();
        }
        // Fetch id
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id FROM parts WHERE description = ?")) {
            ps.setString(1, description);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
                throw new SQLException("Failed to ensure part: " + description);
            }
        }
    }

    public void add(String description) throws SQLException {
        ensurePart(description);
    }

    public boolean isEmpty() throws SQLException {
        String sql = "SELECT 1 FROM parts LIMIT 1";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return !rs.next();
        }
    }

    public void remove(String description) throws SQLException {
        String sql = "DELETE FROM parts WHERE description = ?";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, description);
            ps.executeUpdate();
        }
    }
}
