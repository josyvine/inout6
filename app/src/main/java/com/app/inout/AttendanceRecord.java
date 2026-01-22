package com.inout.app.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Model class for a daily attendance record.
 * Stored in Firestore under: attendance/{employeeId}/{dateId}
 * or in a root collection: attendance_logs (depending on query needs).
 * We will use a root collection for easier Admin queries: attendance/{recordId}
 */
@IgnoreExtraProperties
public class AttendanceRecord {

    private String recordId;        // Typically composite: employeeId_date
    private String employeeId;
    private String employeeName;    // Denormalized for easier display in lists
    private String date;            // YYYY-MM-DD
    
    private String checkInTime;     // Display format (e.g., 09:00 AM)
    private double checkInLat;
    private double checkInLng;
    
    private String checkOutTime;    // Display format (e.g., 05:00 PM)
    private double checkOutLat;
    private double checkOutLng;
    
    private String totalHours;
    
    // Security flags
    private boolean fingerprintVerified;
    private boolean locationVerified;
    
    private long timestamp; // Unix timestamp for sorting

    public AttendanceRecord() {
        // Default constructor required for Firestore
    }

    public AttendanceRecord(String employeeId, String employeeName, String date, long timestamp) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.timestamp = timestamp;
        this.fingerprintVerified = true; // Always true if created via the app logic
        this.locationVerified = true;    // Always true if created via the app logic
    }

    // Getters and Setters

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public double getCheckInLat() {
        return checkInLat;
    }

    public void setCheckInLat(double checkInLat) {
        this.checkInLat = checkInLat;
    }

    public double getCheckInLng() {
        return checkInLng;
    }

    public void setCheckInLng(double checkInLng) {
        this.checkInLng = checkInLng;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public double getCheckOutLat() {
        return checkOutLat;
    }

    public void setCheckOutLat(double checkOutLat) {
        this.checkOutLat = checkOutLat;
    }

    public double getCheckOutLng() {
        return checkOutLng;
    }

    public void setCheckOutLng(double checkOutLng) {
        this.checkOutLng = checkOutLng;
    }

    public String getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(String totalHours) {
        this.totalHours = totalHours;
    }

    public boolean isFingerprintVerified() {
        return fingerprintVerified;
    }

    public void setFingerprintVerified(boolean fingerprintVerified) {
        this.fingerprintVerified = fingerprintVerified;
    }

    public boolean isLocationVerified() {
        return locationVerified;
    }

    public void setLocationVerified(boolean locationVerified) {
        this.locationVerified = locationVerified;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}