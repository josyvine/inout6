package com.inout.app.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

/**
 * Model class representing a user in the 'users' Firestore collection.
 * Updated to support Traveling Mode and Shift Times.
 */
@IgnoreExtraProperties
public class User {

    private String uid;
    private String name;
    private String email;
    private String phone;
    private String role; // "admin" or "employee"
    private boolean approved;
    private String employeeId; 
    private String photoUrl;
    private String assignedLocationId; 
    
    // NEW FIELDS FOR TRAVELING AND SHIFTS
    private boolean isTraveling;
    private String shiftStartTime; // e.g. "09:00 AM"
    private String shiftEndTime;   // e.g. "06:00 PM"

    public User() {
        // Default constructor required for Firestore
    }

    public User(String uid, String email, String role) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.approved = false;
        this.isTraveling = false; // Default to normal mode
    }

    // Getters and Setters with PropertyName annotations

    @PropertyName("uid")
    public String getUid() { return uid; }
    @PropertyName("uid")
    public void setUid(String uid) { this.uid = uid; }

    @PropertyName("name")
    public String getName() { return name; }
    @PropertyName("name")
    public void setName(String name) { this.name = name; }

    @PropertyName("email")
    public String getEmail() { return email; }
    @PropertyName("email")
    public void setEmail(String email) { this.email = email; }

    @PropertyName("phone")
    public String getPhone() { return phone; }
    @PropertyName("phone")
    public void setPhone(String phone) { this.phone = phone; }

    @PropertyName("role")
    public String getRole() { return role; }
    @PropertyName("role")
    public void setRole(String role) { this.role = role; }

    @PropertyName("approved")
    public boolean isApproved() { return approved; }
    @PropertyName("approved")
    public void setApproved(boolean approved) { this.approved = approved; }

    @PropertyName("employeeId")
    public String getEmployeeId() { return employeeId; }
    @PropertyName("employeeId")
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    @PropertyName("photoUrl")
    public String getPhotoUrl() { return photoUrl; }
    @PropertyName("photoUrl")
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    @PropertyName("assignedLocationId")
    public String getAssignedLocationId() { return assignedLocationId; }
    @PropertyName("assignedLocationId")
    public void setAssignedLocationId(String assignedLocationId) { this.assignedLocationId = assignedLocationId; }

    // NEW FIELD ACCESSORS
    @PropertyName("isTraveling")
    public boolean isTraveling() { return isTraveling; }
    @PropertyName("isTraveling")
    public void setTraveling(boolean traveling) { isTraveling = traveling; }

    @PropertyName("shiftStartTime")
    public String getShiftStartTime() { return shiftStartTime; }
    @PropertyName("shiftStartTime")
    public void setShiftStartTime(String shiftStartTime) { this.shiftStartTime = shiftStartTime; }

    @PropertyName("shiftEndTime")
    public String getShiftEndTime() { return shiftEndTime; }
    @PropertyName("shiftEndTime")
    public void setShiftEndTime(String shiftEndTime) { this.shiftEndTime = shiftEndTime; }
}