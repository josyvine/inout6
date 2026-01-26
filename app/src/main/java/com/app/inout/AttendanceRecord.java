package com.inout.app.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Updated Model class for a daily attendance record.
 * Includes all columns required for professional CSV proof.
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
    
    // Security flags
    private boolean fingerprintVerified;
    private boolean gpsVerified; // Renamed from locationVerified for clarity
    
    private long timestamp; 

    public AttendanceRecord() {
        // Default constructor required for Firestore
    }

    // Helper to determine status for the UI logic
    public String getStatus() {
        if (checkInTime != null && checkOutTime != null && fingerprintVerified && gpsVerified) {
            return "Present";
        } else if (checkInTime != null) {
            return "Partial";
        } else {
            return "Absent";
        }
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

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public float getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(float distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public boolean isFingerprintVerified() {
        return fingerprintVerified;
    }

    public void setFingerprintVerified(boolean fingerprintVerified) {
        this.fingerprintVerified = fingerprintVerified;
    }

    public boolean isGpsVerified() {
        return gpsVerified;
    }

    public void setGpsVerified(boolean gpsVerified) {
        this.gpsVerified = gpsVerified;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}