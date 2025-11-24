package com.magpi.db;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

/**
 * SQLite database manager (singleton)
 */
public class Database {
    private static final String APP_DIR = System.getProperty("user.home") + File.separator + "MagPi";
    private static final String DB_PATH = APP_DIR + File.separator + "magpi.db";

    private static Database instance;
    private Connection connection;

    private Database() {
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            ensureAppDir();
            String url = "jdbc:sqlite:" + DB_PATH;
            connection = DriverManager.getConnection(url);
            // Enforce foreign keys
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    private void ensureAppDir() {
        try {
            Path p = Paths.get(APP_DIR);
            if (!Files.exists(p)) {
                Files.createDirectories(p);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create app dir: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize schema and seed default data
     */
    public void init() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'operator', " +
                    "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");

            // Operators
            st.executeUpdate("CREATE TABLE IF NOT EXISTS operators (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE" +
                    ")");

            // Parts
            st.executeUpdate("CREATE TABLE IF NOT EXISTS parts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "description TEXT NOT NULL UNIQUE" +
                    ")");

            // Current parameters per part
            st.executeUpdate("CREATE TABLE IF NOT EXISTS parameters (" +
                    "part_id INTEGER PRIMARY KEY, " +
                    "headshot REAL NOT NULL, " +
                    "coilshot REAL NOT NULL, " +
                    "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(part_id) REFERENCES parts(id) ON DELETE CASCADE" +
                    ")");

            // Parameter history per part
            st.executeUpdate("CREATE TABLE IF NOT EXISTS parameter_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "part_id INTEGER NOT NULL, " +
                    "headshot REAL NOT NULL, " +
                    "coilshot REAL NOT NULL, " +
                    "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(part_id) REFERENCES parts(id) ON DELETE CASCADE" +
                    ")");

            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_parts_desc ON parts(description)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_paramhist_part ON parameter_history(part_id)");

            // Sessions of testing
            st.executeUpdate("CREATE TABLE IF NOT EXISTS sessions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "company_name TEXT, " +
                    "machine_id TEXT, " +
                    "supervisor_id TEXT, " +
                    "operator_name TEXT, " +
                    "part_description TEXT, " +
                    "headshot_threshold REAL, " +
                    "coilshot_threshold REAL, " +
                    "start_time TEXT NOT NULL, " +
                    "end_time TEXT" +
                    ")");

            // Parts tested within a session
            st.executeUpdate("CREATE TABLE IF NOT EXISTS session_parts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "session_id INTEGER NOT NULL, " +
                    "part_number INTEGER NOT NULL, " +
                    "part_description TEXT, " +
                    "status TEXT, " +
                    "crack_detected INTEGER, " +
                    "crack_image_path TEXT, " +
                    "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(session_id) REFERENCES sessions(id) ON DELETE CASCADE" +
                    ")");

            // Attempt to add crack_detected and crack_image_path to existing DBs
            try {
                st.executeUpdate("ALTER TABLE session_parts ADD COLUMN crack_detected INTEGER");
            } catch (Exception ignore) {
            }
            try {
                st.executeUpdate("ALTER TABLE session_parts ADD COLUMN crack_image_path TEXT");
            } catch (Exception ignore) {
            }
            try {
                st.executeUpdate("ALTER TABLE session_parts ADD COLUMN demag_status TEXT");
            } catch (Exception ignore) {
            }
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_session_parts_sess ON session_parts(session_id)");

            // Individual measurements for a session part
            st.executeUpdate("CREATE TABLE IF NOT EXISTS measurements (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "session_part_id INTEGER NOT NULL, " +
                    "meter_type TEXT NOT NULL, " +
                    "shot_index INTEGER NOT NULL, " +
                    "current REAL NOT NULL, " +
                    "duration REAL NOT NULL, " +
                    "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(session_part_id) REFERENCES session_parts(id) ON DELETE CASCADE" +
                    ")");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_meas_part ON measurements(session_part_id)");
        }
        // Ensure there is a default admin
        com.magpi.db.UserDao userDao = new com.magpi.db.UserDao();
        userDao.ensureDefaultAdmin();
    }

    /**
     * Get all operator names from the database
     * 
     * @return List of operator names
     */
    public java.util.List<String> getAllOperators() {
        java.util.List<String> operators = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT name FROM operators ORDER BY name")) {
            while (rs.next()) {
                operators.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching operators: " + e.getMessage());
        }
        return operators;
    }
}
