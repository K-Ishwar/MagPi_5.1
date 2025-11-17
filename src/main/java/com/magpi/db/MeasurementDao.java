package com.magpi.db;

import java.sql.*;

public class MeasurementDao {
    public void insert(long sessionPartId, String meterType, int shotIndex, double current, double duration) throws SQLException {
        String sql = "INSERT INTO measurements(session_part_id, meter_type, shot_index, current, duration) VALUES(?,?,?,?,?)";
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sessionPartId);
            ps.setString(2, meterType);
            ps.setInt(3, shotIndex);
            ps.setDouble(4, current);
            ps.setDouble(5, duration);
            ps.executeUpdate();
        }
    }
}
