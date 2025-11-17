package com.magpi.db;

import com.magpi.model.TestSession;

import java.sql.*;
import java.time.format.DateTimeFormatter;

public class SessionDao {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public long insert(TestSession s) throws SQLException {
        String sql = "INSERT INTO sessions(company_name, machine_id, supervisor_id, operator_name, part_description, headshot_threshold, coilshot_threshold, start_time) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getCompanyName());
            ps.setString(2, s.getMachineId());
            ps.setString(3, s.getSupervisorId());
            ps.setString(4, s.getOperatorName());
            ps.setString(5, s.getPartDescription());
            ps.setDouble(6, s.getHeadShotThreshold());
            ps.setDouble(7, s.getCoilShotThreshold());
            ps.setString(8, s.getStartTime().format(TS));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Failed to insert session");
    }

    public void setEndTime(long sessionId, String endTimeStr) throws SQLException {
        String sql = "UPDATE sessions SET end_time = ? WHERE id = ?";
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, endTimeStr);
            ps.setLong(2, sessionId);
            ps.executeUpdate();
        }
    }
}
