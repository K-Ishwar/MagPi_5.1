package com.magpi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class representing a system calibration log entry
 */
public class CalibrationLog {
    private Long id;
    private LocalDate date;
    private Double machineCalibrationDvcon; // µW/cm²
    private Double blackLightIntensity; // µW/cm²
    private String magneticBathConcentration; // 2-3 Division range
    private Boolean pieGaugeStatus; // true = OK, false = Not OK
    private LocalDateTime createdAt;

    public CalibrationLog() {
    }

    public CalibrationLog(LocalDate date, Double machineCalibrationDvcon,
            Double blackLightIntensity, String magneticBathConcentration,
            Boolean pieGaugeStatus) {
        this.date = date;
        this.machineCalibrationDvcon = machineCalibrationDvcon;
        this.blackLightIntensity = blackLightIntensity;
        this.magneticBathConcentration = magneticBathConcentration;
        this.pieGaugeStatus = pieGaugeStatus;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getMachineCalibrationDvcon() {
        return machineCalibrationDvcon;
    }

    public void setMachineCalibrationDvcon(Double machineCalibrationDvcon) {
        this.machineCalibrationDvcon = machineCalibrationDvcon;
    }

    public Double getBlackLightIntensity() {
        return blackLightIntensity;
    }

    public void setBlackLightIntensity(Double blackLightIntensity) {
        this.blackLightIntensity = blackLightIntensity;
    }

    public String getMagneticBathConcentration() {
        return magneticBathConcentration;
    }

    public void setMagneticBathConcentration(String magneticBathConcentration) {
        this.magneticBathConcentration = magneticBathConcentration;
    }

    public Boolean getPieGaugeStatus() {
        return pieGaugeStatus;
    }

    public void setPieGaugeStatus(Boolean pieGaugeStatus) {
        this.pieGaugeStatus = pieGaugeStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CalibrationLog{" +
                "id=" + id +
                ", date=" + date +
                ", machineCalibrationDvcon=" + machineCalibrationDvcon +
                ", blackLightIntensity=" + blackLightIntensity +
                ", magneticBathConcentration='" + magneticBathConcentration + '\'' +
                ", pieGaugeStatus=" + (pieGaugeStatus ? "OK" : "Not OK") +
                ", createdAt=" + createdAt +
                '}';
    }
}
