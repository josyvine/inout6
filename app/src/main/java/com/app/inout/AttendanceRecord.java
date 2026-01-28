package com.inout.app.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * Professional Model class for a daily attendance record.
 * Fixed to support Check-In, 10-column CSV table, and NEW Transit Logic.
 */
@IgnoreExtraProperties
public class AttendanceRecord {

    private String recordId;        
    private String employeeId;
    private String employeeName;    
    private String date;            // YYYY-MM-DD
    private String dayOfWeek;       // Monday, Tuesday, etc.
    
    private String checkInTime;     
    private double checkInLat;
    private double checkInLng;
    
    private String checkOutTime;    
    private double checkOutLat;
    private double checkOutLng;
    
    private String totalHours;
    private String locationName;    // The office name assigned
    private float distanceMeters;   // Distance from target at check-in
    
    // TRANSIT LOGIC FIELDS (NEW)
    private List<String> movementLog; // Stores sequence ["Loc A", "Loc B"]
    private String lastVerifiedLocationId; // ID of the place currently checked in/transited to

    // Security flags
    private boolean fingerprintVerified;
    private boolean gpsVerified; 
    
    private long timestamp; 

    /**
     * Default constructor required for Firestore.
     */
    public AttendanceRecord() {
        // Initialize list to prevent null pointers
        this.movementLog = new ArrayList<>();
    }

    /**
     * Parameterized constructor required by EmployeeCheckInFragment.
     */
    public AttendanceRecord(String employeeId, String employeeName, String date, long timestamp) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.timestamp = timestamp;
        this.fingerprintVerified = true; 
        this.gpsVerified = true;    
        this.movementLog = new ArrayList<>();
    }

    /**
     * Helper to determine status for the UI logic.
     */
    public String getStatus() {
        if (checkInTime != null && checkOutTime != null && fingerprintVerified && gpsVerified) {
            return "Present";
        } else if (checkInTime != null) {
            return "Partial";
        } else {
            return "Absent";
        }
    }

    /**
     * Helper to generate the Transit Summary string for CSV and UI.
     * Logic: If only 1 location in list -> "No transit". If > 1 -> "A -> B -> C".
     */
    public String getTransitSummary() {
        if (movementLog == null || movementLog.size() <= 1) {
            return "No transit record today";
        }
        
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < movementLog.size(); i++) {
            builder.append(movementLog.get(i));
            if (i < movementLog.size() - 1) {
                builder.append(" → ");
            }
        }
        return builder.toString();
    }

    // Getters and Setters

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public double getCheckInLat() { return checkInLat; }
    public void setCheckInLat(double checkInLat) { this.checkInLat = checkInLat; }

    public double getCheckInLng() { return checkInLng; }
    public void setCheckInLng(double checkInLng) { this.checkInLng = checkInLng; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    public double getCheckOutLat() { return checkOutLat; }
    public void setCheckOutLat(double checkOutLat) { this.checkOutLat = checkOutLat; }

    public double getCheckOutLng() { return checkOutLng; }
    public void setCheckOutLng(double checkOutLng) { this.checkOutLng = checkOutLng; }

    public String getTotalHours() { return totalHours; }
    public void setTotalHours(String totalHours) { this.totalHours = totalHours; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public float getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(float distanceMeters) { this.distanceMeters = distanceMeters; }

    // NEW TRANSIT GETTERS/SETTERS
    public List<String> getMovementLog() { return movementLog; }
    public void setMovementLog(List<String> movementLog) { this.movementLog = movementLog; }

    public String getLastVerifiedLocationId() { return lastVerifiedLocationId; }
    public void setLastVerifiedLocationId(String lastVerifiedLocationId) { this.lastVerifiedLocationId = lastVerifiedLocationId; }

    public boolean isFingerprintVerified() { return fingerprintVerified; }
    public void setFingerprintVerified(boolean fingerprintVerified) { this.fingerprintVerified = fingerprintVerified; }

    public boolean isGpsVerified() { return gpsVerified; }
    public void setGpsVerified(boolean gpsVerified) { this.gpsVerified = gpsVerified; }

    /**
     * Alias for setGpsVerified to maintain compatibility with existing Fragment logic.
     */
    public void setLocationVerified(boolean verified) {
        this.gpsVerified = verified;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}