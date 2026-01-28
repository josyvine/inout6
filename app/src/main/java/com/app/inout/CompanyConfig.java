package com.inout.app.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

/**
 * Model class representing an Office Location / Company Configuration.
 * Stored in Firestore under 'locations' collection.
 * FIXED: Added PropertyName annotations to prevent mapping failure in Release builds.
 */
@IgnoreExtraProperties
public class CompanyConfig {

    private String id;
    private String name;        // e.g., "Headquarters", "Branch A"
    private double latitude;
    private double longitude;
    private float radius;       // Allowed radius in meters (default 100)

    public CompanyConfig() {
        // Default constructor required for Firestore
        this.radius = 100.0f; // Default safety radius
    }

    public CompanyConfig(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = 100.0f;
    }

    // Getters and Setters with explicit PropertyName mapping

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("latitude")
    public double getLatitude() {
        return latitude;
    }

    @PropertyName("latitude")
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @PropertyName("longitude")
    public double getLongitude() {
        return longitude;
    }

    @PropertyName("longitude")
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @PropertyName("radius")
    public float getRadius() {
        return radius;
    }

    @PropertyName("radius")
    public void setRadius(float radius) {
        this.radius = radius;
    }
}