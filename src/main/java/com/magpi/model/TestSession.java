package com.magpi.model;

import com.magpi.ui.HistoryPage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a testing session with session information and the collection of
 * parts tested
 */
public class TestSession {
    private Long id; // DB id
    private String operatorName;
    private String machineId;
    private String supervisorId;
    private String companyName;
    private String partDescription;
    private double headShotThreshold;
    private double coilShotThreshold;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<TestPart> parts;
    private HistoryPage historyPanel;

    public TestSession() {
        this.startTime = LocalDateTime.now();
        this.parts = new ArrayList<>();
        this.headShotThreshold = 0.0;
        this.coilShotThreshold = 0.0;
    }

    public void addPart(TestPart part) {
        parts.add(part);
    }

    /**
     * Returns the most recent TestPart for the given base part number.
     * This is important when there are retests (e.g. 3, 3-1, 3-2) – we always
     * want the latest instance rather than the original.
     */
    public TestPart getPartByNumber(int partNumber) {
        for (int i = parts.size() - 1; i >= 0; i--) {
            TestPart part = parts.get(i);
            if (part.getPartNumber() == partNumber) {
                return part;
            }
        }
        return null;
    }

    public void endTest() {
        this.endTime = LocalDateTime.now();
    }

    public int getTotalPartsCount() {
        return parts.size();
    }

    public int getAcceptedPartsCount() {
        return (int) parts.stream()
                .filter(part -> "Accept".equals(part.getStatus()))
                .count();
    }

    public int getRejectedPartsCount() {
        return (int) parts.stream()
                .filter(part -> "Reject".equals(part.getStatus()))
                .count();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPartDescription() {
        return partDescription;
    }

    public void setPartDescription(String partDescription) {
        this.partDescription = partDescription;
    }

    public double getHeadShotThreshold() {
        return headShotThreshold;
    }

    public void setHeadShotThreshold(double headShotThreshold) {
        this.headShotThreshold = headShotThreshold;
    }

    public double getCoilShotThreshold() {
        return coilShotThreshold;
    }

    public void setCoilShotThreshold(double coilShotThreshold) {
        this.coilShotThreshold = coilShotThreshold;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<TestPart> getParts() {
        return parts;
    }

    public HistoryPage getHistoryPanel() {
        return historyPanel;
    }

    public void setHistoryPanel(HistoryPage historyPanel) {
        this.historyPanel = historyPanel;
    }

}