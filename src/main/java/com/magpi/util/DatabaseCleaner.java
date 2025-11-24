package com.magpi.util;

import com.magpi.db.Database;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseCleaner {
    public static void main(String[] args) {
        System.out.println("Starting database cleanup...");
        try (Connection conn = Database.getInstance().getConnection();
                Statement stmt = conn.createStatement()) {

            // Disable foreign keys temporarily to avoid constraint violations during
            // deletion
            stmt.execute("PRAGMA foreign_keys = OFF");

            String[] tables = {
                    "measurements",
                    "session_parts",
                    "sessions",
                    "parameter_history",
                    "parameters",
                    "parts",
                    "operators",
                    "users"
            };

            conn.setAutoCommit(false);
            for (String table : tables) {
                try {
                    stmt.executeUpdate("DELETE FROM " + table);
                    // Reset auto-increment counter
                    stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='" + table + "'");
                    System.out.println("Cleared table: " + table);
                } catch (Exception e) {
                    System.err.println("Error clearing table " + table + ": " + e.getMessage());
                }
            }
            conn.commit();

            // Re-enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");

            System.out.println("All data cleared successfully.");

            // Re-init to ensure default admin exists
            Database.getInstance().init();
            System.out.println("Database re-initialized (default admin created).");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
