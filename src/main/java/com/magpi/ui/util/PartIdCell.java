package com.magpi.ui.util;

import java.util.Objects;

/**
 * Table cell value for Part No column that preserves equality with base integer
 * while displaying optional recheck suffix.
 */
public class PartIdCell {
    private final int basePartNumber;
    private final int recheckCount; // 0 for original, >0 for rechecks

    public PartIdCell(int basePartNumber, int recheckCount) {
        this.basePartNumber = basePartNumber;
        this.recheckCount = Math.max(0, recheckCount);
    }

    public int getBasePartNumber() { return basePartNumber; }
    public int getRecheckCount() { return recheckCount; }

    @Override
    public String toString() {
        return recheckCount > 0 ? (basePartNumber + "-" + recheckCount) : String.valueOf(basePartNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof PartIdCell) {
            PartIdCell other = (PartIdCell) obj;
            return this.basePartNumber == other.basePartNumber && this.recheckCount == other.recheckCount;
        }
        if (obj instanceof Integer) {
            return this.basePartNumber == (Integer) obj;
        }
        if (obj instanceof String) {
            // Accept compare with display string
            return this.toString().equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePartNumber, recheckCount);
    }
}
