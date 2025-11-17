package com.magpi.db;

import com.magpi.model.TestPart;

import java.sql.*;

public class SessionPartDao {
    public long insert(long sessionId, TestPart part) throws SQLException {
        String sql = "INSERT INTO session_parts(session_id, part_number, part_description, status) VALUES(?,?,?,?)";
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, sessionId);
            ps.setInt(2, part.getPartNumber());
            ps.setString(3, part.getPartDescription());
            ps.setString(4, part.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Failed to insert session part");
    }

    public void updateStatus(long partId, String status) throws SQLException {
        String sql = "UPDATE session_parts SET status = ? WHERE id = ?";
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, partId);
            ps.executeUpdate();
        }
    }

    public void updateCrackDetected(long partId, boolean crackDetected) throws SQLException {
        String sql = "UPDATE session_parts SET crack_detected = ? WHERE id = ?";
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, crackDetected ? 1 : 0);
            ps.setLong(2, partId);
            ps.executeUpdate();
        }
    }
}
