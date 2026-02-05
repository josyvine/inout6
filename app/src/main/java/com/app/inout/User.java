package com.inout.app.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

/**
 * Model class representing a user in the 'users' Firestore collection.
 * This is the bridge between Firestore and the app memory.
 * FIXED: Added PropertyName annotations to ensure data syncs correctly in Release APKs.
 * UPDATED: Added Emergency Leave and Medical Leave status tracking.
 */
@IgnoreExtraProperties
public class User {

    private String uid;
    private String name;
    private String email;
    private String phone;
    private String role; // "admin" or "employee"
    private boolean approved;
    private String employeeId; // Assigned by Admin (e.g., EMP001)
    private String photoUrl;

    // For Employees: The ID of the location they are assigned to for check-in
    private String assignedLocationId; 

    // FIELDS FOR TRAVELING AND SHIFTS
    private boolean isTraveling;
    private String shiftStartTime; 
    private String shiftEndTime;   

    // FIELD FOR EMERGENCY LEAVE
    private String emergencyLeaveStatus; // "none", "pending", "approved"

    // NEW FIELDS FOR MEDICAL LEAVE
    private String medicalLeaveStatus; // "none", "pending", "approved"
    private String medicalLeaveType;   // "none", "paid", "unpaid"

    public User() {
        // Default constructor required for Firestore
        this.emergencyLeaveStatus = "none";
        this.medicalLeaveStatus = "none";
        this.medicalLeaveType = "none";
    }

    public User(String uid, String email, String role) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.approved = false;
        this.emergencyLeaveStatus = "none";
        this.medicalLeaveStatus = "none";
        this.medicalLeaveType = "none";
    }

    // Getters and Setters with explicit PropertyName mapping

    @PropertyName("uid")
    public String getUid() {
        return uid;
    }

    @PropertyName("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    @PropertyName("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("phone")
    public String getPhone() {
        return phone;
    }

    @PropertyName("phone")
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @PropertyName("role")
    public String getRole() {
        return role;
    }

    @PropertyName("role")
    public void setRole(String role) {
        this.role = role;
    }

    @PropertyName("approved")
    public boolean isApproved() {
        return approved;
    }

    @PropertyName("approved")
    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @PropertyName("employeeId")
    public String getEmployeeId() {
        return employeeId;
    }

    @PropertyName("employeeId")
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    @PropertyName("photoUrl")
    public String getPhotoUrl() {
        return photoUrl;
    }

    @PropertyName("photoUrl")
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @PropertyName("assignedLocationId")
    public String getAssignedLocationId() {
        return assignedLocationId;
    }

    @PropertyName("assignedLocationId")
    public void setAssignedLocationId(String assignedLocationId) {
        this.assignedLocationId = assignedLocationId;
    }

    @PropertyName("isTraveling")
    public boolean isTraveling() {
        return isTraveling;
    }

    @PropertyName("isTraveling")
    public void setTraveling(boolean traveling) {
        isTraveling = traveling;
    }

    @PropertyName("shiftStartTime")
    public String getShiftStartTime() {
        return shiftStartTime;
    }

    @PropertyName("shiftStartTime")
    public void setShiftStartTime(String shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    @PropertyName("shiftEndTime")
    public String getShiftEndTime() {
        return shiftEndTime;
    }

    @PropertyName("shiftEndTime")
    public void setShiftEndTime(String shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    @PropertyName("emergencyLeaveStatus")
    public String getEmergencyLeaveStatus() {
        return emergencyLeaveStatus;
    }

    @PropertyName("emergencyLeaveStatus")
    public void setEmergencyLeaveStatus(String emergencyLeaveStatus) {
        this.emergencyLeaveStatus = emergencyLeaveStatus;
    }

    @PropertyName("medicalLeaveStatus")
    public String getMedicalLeaveStatus() {
        return medicalLeaveStatus;
    }

    @PropertyName("medicalLeaveStatus")
    public void setMedicalLeaveStatus(String medicalLeaveStatus) {
        this.medicalLeaveStatus = medicalLeaveStatus;
    }

    @PropertyName("medicalLeaveType")
    public String getMedicalLeaveType() {
        return medicalLeaveType;
    }

    public void setMedicalLeaveType(String medicalLeaveType) {
        this.medicalLeaveType = medicalLeaveType;
    }
}