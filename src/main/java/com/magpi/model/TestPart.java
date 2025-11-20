package com.magpi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a part being tested with its measurements and status
 */
public class TestPart {
    private Long id; // DB id
    private int partNumber;
    private String partDescription;
    private LocalDateTime testTime;
    private List<Measurement> headshotMeasurements;
    private List<Measurement> coilshotMeasurements;
    private String status; // "PASS" or "ERROR"
    private int recheckCount; // 0 for original, >0 for rechecks
    private String crackImagePath; // Optional path to captured crack image

    public TestPart(int partNumber, String partDescription) {
        this.partNumber = partNumber;
        this.partDescription = partDescription;
        this.testTime = LocalDateTime.now();
        this.headshotMeasurements = new ArrayList<>();
        this.coilshotMeasurements = new ArrayList<>();
        this.status = "";
        this.recheckCount = 0;
        this.crackImagePath = null;
    }

    public void addHeadshotMeasurement(Measurement measurement) {
        headshotMeasurements.add(measurement);
    }

    public void addCoilshotMeasurement(Measurement measurement) {
        coilshotMeasurements.add(measurement);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getPartNumber() {
        return partNumber;
    }

    public String getPartDescription() {
        return partDescription;
    }

    public LocalDateTime getTestTime() {
        return testTime;
    }

    public List<Measurement> getHeadshotMeasurements() {
        return headshotMeasurements;
    }

    public List<Measurement> getCoilshotMeasurements() {
        return coilshotMeasurements;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRecheckCount() { return recheckCount; }
    public void setRecheckCount(int recheckCount) { this.recheckCount = recheckCount; }

    public String getCrackImagePath() { return crackImagePath; }
    public void setCrackImagePath(String crackImagePath) { this.crackImagePath = crackImagePath; }

    /**
     * Gets a specific headshot measurement by index
     * @param index The index of the measurement to retrieve (0-based)
     * @return The measurement or null if out of bounds
     */
    public Measurement getHeadshotMeasurement(int index) {
        if (index >= 0 && index < headshotMeasurements.size()) {
            return headshotMeasurements.get(index);
        }
        return null;
    }

    /**
     * Gets a specific coilshot measurement by index
     * @param index The index of the measurement to retrieve (0-based)
     * @return The measurement or null if out of bounds
     */
    public Measurement getCoilshotMeasurement(int index) {
        if (index >= 0 && index < coilshotMeasurements.size()) {
            return coilshotMeasurements.get(index);
        }
        return null;
    }

    /**
     * Gets the highest current value from headshot measurements
     */
    public double getHighestHeadshotCurrent() {
        return headshotMeasurements.stream()
                .mapToDouble(Measurement::getCurrent)
                .max()
                .orElse(0);
    }

    /**
     * Gets the highest current value from coilshot measurements
     */
    public double getHighestCoilshotCurrent() {
        return coilshotMeasurements.stream()
                .mapToDouble(Measurement::getCurrent)
                .max()
                .orElse(0);
    }
}
