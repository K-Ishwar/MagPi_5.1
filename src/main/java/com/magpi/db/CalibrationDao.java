package com.magpi.db;

import com.magpi.model.CalibrationLog;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Access Object for calibration logs
 */
public class CalibrationDao {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Insert a new calibration log entry
     * 
     * @param log The calibration log to insert
     * @return The generated ID
     * @throws SQLException if database error occurs
     */
    public long insert(CalibrationLog log) throws SQLException {
        String sql = "INSERT INTO calibration_logs(date, machine_calibration_dvcon, black_light_intensity, " +
                "magnetic_bath_concentration, pie_gauge_status, created_at) VALUES (?,?,?,?,?,?)";

        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, log.getDate().format(DATE_FORMATTER));
            ps.setDouble(2, log.getMachineCalibrationDvcon());
            ps.setDouble(3, log.getBlackLightIntensity());
            ps.setString(4, log.getMagneticBathConcentration());
            ps.setInt(5, log.getPieGaugeStatus() ? 1 : 0);
            ps.setString(6, log.getCreatedAt().format(TIMESTAMP_FORMATTER));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to insert calibration log");
    }

    /**
     * Get calibration log for a specific date
     * 
     * @param date The date to search for
     * @return CalibrationLog if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public CalibrationLog getCalibrationForDate(LocalDate date) throws SQLException {
        String sql = "SELECT * FROM calibration_logs WHERE date = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.format(DATE_FORMATTER));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCalibrationLog(rs);
                }
            }
        }
        return null;
    }

    /**
     * Check if calibration exists for today
     * 
     * @return true if calibration exists for today, false otherwise
     */
    public boolean hasCalibrationForToday() {
        try {
            CalibrationLog log = getCalibrationForDate(LocalDate.now());
            return log != null;
        } catch (SQLException e) {
            System.err.println("Error checking today's calibration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the most recent calibration log
     * 
     * @return The latest CalibrationLog, or null if none exist
     * @throws SQLException if database error occurs
     */
    public CalibrationLog getLatestCalibration() throws SQLException {
        String sql = "SELECT * FROM calibration_logs ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = Database.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return mapResultSetToCalibrationLog(rs);
            }
        }
        return null;
    }

    /**
     * Map a ResultSet row to a CalibrationLog object
     */
    private CalibrationLog mapResultSetToCalibrationLog(ResultSet rs) throws SQLException {
        CalibrationLog log = new CalibrationLog();
        log.setId(rs.getLong("id"));
        log.setDate(LocalDate.parse(rs.getString("date"), DATE_FORMATTER));
        log.setMachineCalibrationDvcon(rs.getDouble("machine_calibration_dvcon"));
        log.setBlackLightIntensity(rs.getDouble("black_light_intensity"));
        log.setMagneticBathConcentration(rs.getString("magnetic_bath_concentration"));
        log.setPieGaugeStatus(rs.getInt("pie_gauge_status") == 1);
        log.setCreatedAt(LocalDateTime.parse(rs.getString("created_at"), TIMESTAMP_FORMATTER));
        return log;
    }
}
