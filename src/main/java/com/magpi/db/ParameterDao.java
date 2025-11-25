package com.magpi.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParameterDao {
    public static class Param {
        public final double head;
        public final double coil;

        public Param(double h, double c) {
            head = h;
            coil = c;
        }
    }

    public Param getCurrent(String partDescription) throws SQLException {
        int partId = new PartDao().ensurePart(partDescription);
        String sql = "SELECT headshot, coilshot FROM parameters WHERE part_id = ?";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, partId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Param(rs.getDouble(1), rs.getDouble(2));
                return null;
            }
        }
    }

    public void saveCurrent(String partDescription, double head, double coil) throws SQLException {
        int partId = new PartDao().ensurePart(partDescription);
        // Upsert parameters
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO parameters(part_id, headshot, coilshot) VALUES(?,?,?) " +
                                "ON CONFLICT(part_id) DO UPDATE SET headshot=excluded.headshot, coilshot=excluded.coilshot, updated_at=CURRENT_TIMESTAMP")) {
            ps.setInt(1, partId);
            ps.setDouble(2, head);
            ps.setDouble(3, coil);
            ps.executeUpdate();
        }
        // Append to history
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c
                        .prepareStatement("INSERT INTO parameter_history(part_id, headshot, coilshot) VALUES(?,?,?)")) {
            ps.setInt(1, partId);
            ps.setDouble(2, head);
            ps.setDouble(3, coil);
            ps.executeUpdate();
        }
    }

    public List<Param> getHistory(String partDescription) throws SQLException {
        int partId = new PartDao().ensurePart(partDescription);
        String sql = "SELECT headshot, coilshot FROM parameter_history WHERE part_id = ? ORDER BY id DESC";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, partId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Param> list = new ArrayList<>();
                while (rs.next())
                    list.add(new Param(rs.getDouble(1), rs.getDouble(2)));
                return list;
            }
        }
    }

    public void removeSpecific(String partDescription, double headshot, double coilshot) throws SQLException {
        int partId = new PartDao().ensurePart(partDescription);
        // Delete only the most recent matching entry from parameter_history
        String sql = "DELETE FROM parameter_history WHERE id = " +
                "(SELECT id FROM parameter_history WHERE part_id = ? AND headshot = ? AND coilshot = ? ORDER BY id DESC LIMIT 1)";
        try (Connection c = Database.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, partId);
            ps.setDouble(2, headshot);
            ps.setDouble(3, coilshot);
            ps.executeUpdate();
        }
    }
}
