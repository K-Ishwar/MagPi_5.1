package com.magpi.util;

import com.magpi.db.OperatorDao;
import com.magpi.db.PartDao;
import com.magpi.db.ParameterDao;

import java.io.*;
import java.util.*;

/**
 * Manages persistent storage of operator names, part descriptions, and
 * part-specific parameters.
 * Now backed by SQLite. Includes one-time migration from legacy text files.
 */
public class PersistentLibrary {
    private static final String LIBRARY_DIR = System.getProperty("user.home") + "/MagPi/Library";
    private static final String OPERATORS_FILE = LIBRARY_DIR + "/operators.txt";
    private static final String PARTS_FILE = LIBRARY_DIR + "/parts.txt";
    private static final String PARAMETERS_FILE = LIBRARY_DIR + "/parameters.txt";
    private static final String PARAMETER_HISTORY_FILE = LIBRARY_DIR + "/parameter_history.txt";

    private static PersistentLibrary instance;

    private final OperatorDao operatorDao = new OperatorDao();
    private final PartDao partDao = new PartDao();
    private final ParameterDao parameterDao = new ParameterDao();

    private PersistentLibrary() {
        migrateFromLegacyFilesIfNeeded();
    }

    public static PersistentLibrary getInstance() {
        if (instance == null) {
            instance = new PersistentLibrary();
        }
        return instance;
    }

    // Operator methods
    public void addOperator(String operator) {
        try {
            operatorDao.add(operator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getOperators() {
        try {
            return operatorDao.getAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeOperator(String operator) {
        try {
            operatorDao.remove(operator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Part description methods
    public void addPartDescription(String description) {
        try {
            partDao.add(description);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getPartDescriptions() {
        try {
            return partDao.getAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removePartDescription(String description) {
        try {
            partDao.remove(description);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeSpecificParameter(String partDescription, double headshotThreshold, double coilshotThreshold) {
        try {
            parameterDao.removeSpecific(partDescription, headshotThreshold, coilshotThreshold);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Parameter methods
    public void savePartParameters(String partDescription, double headshotThreshold, double coilshotThreshold) {
        try {
            parameterDao.saveCurrent(partDescription, headshotThreshold, coilshotThreshold);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PartParameters getPartParameters(String partDescription) {
        try {
            ParameterDao.Param p = parameterDao.getCurrent(partDescription);
            if (p == null)
                return null;
            return new PartParameters(p.head, p.coil);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<PartParameters> getPartParameterHistory(String partDescription) {
        try {
            List<ParameterDao.Param> raw = parameterDao.getHistory(partDescription);
            List<PartParameters> list = new ArrayList<>();
            for (ParameterDao.Param p : raw)
                list.add(new PartParameters(p.head, p.coil));
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * One-time migration from legacy text files into SQLite if DB is empty.
     */
    private void migrateFromLegacyFilesIfNeeded() {
        try {
            boolean hasAnyLegacy = new File(OPERATORS_FILE).exists() || new File(PARTS_FILE).exists() ||
                    new File(PARAMETERS_FILE).exists() || new File(PARAMETER_HISTORY_FILE).exists();
            if (!hasAnyLegacy)
                return;

            // Read legacy files
            Set<String> operators = readLinesToSet(OPERATORS_FILE);
            Set<String> parts = readLinesToSet(PARTS_FILE);
            Map<String, PartParameters> currentParams = readCurrentParams(PARAMETERS_FILE);
            Map<String, List<PartParameters>> historyParams = readHistory(PARAMETER_HISTORY_FILE);
            for (String op : operators) {
                if (!op.isBlank())
                    operatorDao.add(op);
            }
            // Insert parts
            for (String part : parts) {
                if (!part.isBlank())
                    partDao.add(part);
            }
            // Insert parameters and history
            for (Map.Entry<String, PartParameters> e : currentParams.entrySet()) {
                PartParameters p = e.getValue();
                parameterDao.saveCurrent(e.getKey(), p.headshotThreshold, p.coilshotThreshold);
            }
            for (Map.Entry<String, List<PartParameters>> e : historyParams.entrySet()) {
                for (PartParameters p : e.getValue()) {
                    parameterDao.saveCurrent(e.getKey(), p.headshotThreshold, p.coilshotThreshold);
                }
            }

            // Rename legacy files to mark migrated
            renameIfExists(OPERATORS_FILE);
            renameIfExists(PARTS_FILE);
            renameIfExists(PARAMETERS_FILE);
            renameIfExists(PARAMETER_HISTORY_FILE);
        } catch (Exception ex) {
            // Best-effort migration; log to stderr
            ex.printStackTrace();
        }
    }

    private void renameIfExists(String path) {
        File f = new File(path);
        if (f.exists()) {
            File dest = new File(path + ".migrated");
            // noinspection ResultOfMethodCallIgnored
            f.renameTo(dest);
        }
    }

    private Set<String> readLinesToSet(String filename) {
        Set<String> set = new HashSet<>();
        File file = new File(filename);
        if (!file.exists())
            return set;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String v = line.trim();
                if (!v.isEmpty())
                    set.add(v);
            }
        } catch (IOException ignored) {
        }
        return set;
    }

    private Map<String, PartParameters> readCurrentParams(String filename) {
        Map<String, PartParameters> map = new HashMap<>();
        File file = new File(filename);
        if (!file.exists())
            return map;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String partDesc = parts[0].trim();
                    double headshot = Double.parseDouble(parts[1].trim());
                    double coilshot = Double.parseDouble(parts[2].trim());
                    map.put(partDesc, new PartParameters(headshot, coilshot));
                }
            }
        } catch (IOException ignored) {
        }
        return map;
    }

    private Map<String, List<PartParameters>> readHistory(String filename) {
        Map<String, List<PartParameters>> map = new HashMap<>();
        File file = new File(filename);
        if (!file.exists())
            return map;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String partDesc = parts[0].trim();
                    double headshot = Double.parseDouble(parts[1].trim());
                    double coilshot = Double.parseDouble(parts[2].trim());
                    map.computeIfAbsent(partDesc, k -> new ArrayList<>())
                            .add(new PartParameters(headshot, coilshot));
                }
            }
        } catch (IOException ignored) {
        }
        return map;
    }

    public static class PartParameters {
        private final double headshotThreshold;
        private final double coilshotThreshold;

        public PartParameters(double headshotThreshold, double coilshotThreshold) {
            this.headshotThreshold = headshotThreshold;
            this.coilshotThreshold = coilshotThreshold;
        }

        public double getHeadshotThreshold() {
            return headshotThreshold;
        }

        public double getCoilshotThreshold() {
            return coilshotThreshold;
        }

        @Override
        public String toString() {
            return String.format("Headshot: %.2f, Coilshot: %.2f", headshotThreshold, coilshotThreshold);
        }
    }
}
