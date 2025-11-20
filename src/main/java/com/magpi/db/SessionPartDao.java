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

    public void updateCrackImagePath(long partId, String imagePath) throws SQLException {
        String sql = "UPDATE session_parts SET crack_image_path = ? WHERE id = ?";
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, imagePath);
            ps.setLong(2, partId);
            ps.executeUpdate();
        }
    }

    /**
     * Checks if a given part number/description combination already exists in history.
     * This is used to prevent creating a new base part with a number that has already
     * been used for the same part description in any previous session.
     */
    public boolean existsPartNumberForDescription(int partNumber, String partDescription) throws SQLException {
        String sql = "SELECT 1 FROM session_parts WHERE part_number = ? AND part_description = ? LIMIT 1";
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, partNumber);
            ps.setString(2, partDescription);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
