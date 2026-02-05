package com.inout.app.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.List;

/**
 * Professional Model class for a daily attendance record.
 * Supports Check-In, 14-column CSV, Transit Logic, and Shift/Overtime tracking.
 * UPDATED: Added Resume tracking and Medical Leave type for daily reporting.
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
    private String locationName;    // The office name assigned (Destination)
    private float distanceMeters;   // Distance from target at check-in
    
    // TRANSIT LOGIC FIELDS
    private List<String> movementLog; // Stores sequence ["Loc A", "Loc B"]
    private String lastVerifiedLocationId; // ID of the place currently checked in/transited to

    // FIELDS FOR SHIFT & TRAVELING
    private String assignedShift;   // e.g. "09:00 AM - 06:00 PM"
    private String overtimeHours;   // e.g. "3h 00m"
    private String startLocationName; // Where the travel started (e.g. "Home")

    // FIELDS FOR EMERGENCY LEAVE
    private String emergencyLeaveTime;
    private String emergencyLeaveLocation;
    private String remarks;

    // NEW FIELDS FOR MEDICAL LEAVE & RESUME
    private boolean resumeRequested; 
    private String medicalLeaveType; // "none", "paid", "unpaid"

    // Security flags
    private boolean fingerprintVerified;
    private boolean gpsVerified; 
    
    private long timestamp; 

    /**
     * Default constructor required for Firestore.
     */
    public AttendanceRecord() {
        this.movementLog = new ArrayList<>();
        this.medicalLeaveType = "none";
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
        this.medicalLeaveType = "none";
    }

    /**
     * Helper to determine status for the UI logic.
     * UPDATED: Handles Medical Leave and Resume status priorities.
     */
    public String getStatus() {
        // If Medical Leave was granted and they haven't finished a resumed shift
        if (!"none".equals(medicalLeaveType) && checkOutTime == null) {
            return "Absent";
        }
        // Existing Emergency Leave logic
        if (emergencyLeaveTime != null && checkOutTime == null) {
            return "Absent";
        }
        // Standard Presence logic
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
     */
    public String getTransitSummary() {
        if (movementLog == null || movementLog.size() <= 1) {
            if (startLocationName != null && !startLocationName.isEmpty()) {
                return "Started at " + startLocationName + " → " + locationName;
            }
            return "No transit record today";
        }
        
        StringBuilder builder = new StringBuilder();
        if (startLocationName != null && !startLocationName.isEmpty()) {
            builder.append(startLocationName).append(" → ");
        }

        for (int i = 0; i < movementLog.size(); i++) {
            builder.append(movementLog.get(i));
            if (i < movementLog.size() - 1) {
                builder.append(" → ");
            }
        }
        return builder.toString();
    }

    // Getters and Setters with PropertyName mapping

    @PropertyName("recordId")
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    @PropertyName("employeeId")
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    @PropertyName("employeeName")
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    @PropertyName("date")
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @PropertyName("dayOfWeek")
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    @PropertyName("checkInTime")
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    @PropertyName("checkInLat")
    public double getCheckInLat() { return checkInLat; }
    public void setCheckInLat(double checkInLat) { this.checkInLat = checkInLat; }

    @PropertyName("checkInLng")
    public double getCheckInLng() { return checkInLng; }
    public void setCheckInLng(double checkInLng) { this.checkInLng = checkInLng; }

    @PropertyName("checkOutTime")
    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    @PropertyName("checkOutLat")
    public double getCheckOutLat() { return checkOutLat; }
    public void setCheckOutLat(double checkOutLat) { this.checkOutLat = checkOutLat; }

    @PropertyName("checkOutLng")
    public double getCheckOutLng() { return checkOutLng; }
    public void setCheckOutLng(double checkOutLng) { this.checkOutLng = checkOutLng; }

    @PropertyName("totalHours")
    public String getTotalHours() { return totalHours; }
    public void setTotalHours(String totalHours) { this.totalHours = totalHours; }

    @PropertyName("locationName")
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    @PropertyName("distanceMeters")
    public float getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(float distanceMeters) { this.distanceMeters = distanceMeters; }

    @PropertyName("movementLog")
    public List<String> getMovementLog() { return movementLog; }
    public void setMovementLog(List<String> movementLog) { this.movementLog = movementLog; }

    @PropertyName("lastVerifiedLocationId")
    public String getLastVerifiedLocationId() { return lastVerifiedLocationId; }
    public void setLastVerifiedLocationId(String lastVerifiedLocationId) { this.lastVerifiedLocationId = lastVerifiedLocationId; }

    @PropertyName("assignedShift")
    public String getAssignedShift() { return assignedShift; }
    public void setAssignedShift(String assignedShift) { this.assignedShift = assignedShift; }

    @PropertyName("overtimeHours")
    public String getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(String overtimeHours) { this.overtimeHours = overtimeHours; }

    @PropertyName("startLocationName")
    public String getStartLocationName() { return startLocationName; }
    public void setStartLocationName(String startLocationName) { this.startLocationName = startLocationName; }

    @PropertyName("emergencyLeaveTime")
    public String getEmergencyLeaveTime() { return emergencyLeaveTime; }
    public void setEmergencyLeaveTime(String emergencyLeaveTime) { this.emergencyLeaveTime = emergencyLeaveTime; }

    @PropertyName("emergencyLeaveLocation")
    public String getEmergencyLeaveLocation() { return emergencyLeaveLocation; }
    public void setEmergencyLeaveLocation(String emergencyLeaveLocation) { this.emergencyLeaveLocation = emergencyLeaveLocation; }

    @PropertyName("remarks")
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    @PropertyName("resumeRequested")
    public boolean isResumeRequested() { return resumeRequested; }
    public void setResumeRequested(boolean resumeRequested) { this.resumeRequested = resumeRequested; }

    @PropertyName("medicalLeaveType")
    public String getMedicalLeaveType() { return medicalLeaveType; }
    public void setMedicalLeaveType(String medicalLeaveType) { this.medicalLeaveType = medicalLeaveType; }

    @PropertyName("fingerprintVerified")
    public boolean isFingerprintVerified() { return fingerprintVerified; }
    public void setFingerprintVerified(boolean fingerprintVerified) { this.fingerprintVerified = fingerprintVerified; }

    @PropertyName("gpsVerified")
    public boolean isGpsVerified() { return gpsVerified; }
    public void setGpsVerified(boolean gpsVerified) { this.gpsVerified = gpsVerified; }

    @PropertyName("timestamp")
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public void setLocationVerified(boolean verified) { this.gpsVerified = verified; }
}