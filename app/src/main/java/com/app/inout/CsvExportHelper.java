package com.inout.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.inout.app.models.AttendanceRecord;
import com.inout.app.utils.TimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Utility to generate and share professional attendance reports.
 * UPDATED: Handles 14-column layout with complex logic for Medical Leave (Paid/Unpaid) and Resume (Late Start).
 */
public class CsvExportHelper {

    private static final String TAG = "CsvExportHelper";

    /**
     * Converts the full month list into a CSV-formatted string and opens the share menu.
     */
    public static void exportAttendanceToCsv(Context context, List<AttendanceRecord> records, String fileName) {
        
        // 1. Create the CSV Header Row (14 Columns)
        StringBuilder csvData = new StringBuilder();
        csvData.append("Date,Day,CheckIn,TransitRoute,CheckOut,AssignedShift,TotalHours,Overtime,Location,DistanceMeters,FingerprintVerified,GPSVerified,Status,Remarks\n");

        // 2. Loop through all records and format rows
        for (AttendanceRecord record : records) {
            String date = record.getDate();
            String day = record.getDayOfWeek();
            String in = (record.getCheckInTime() != null) ? record.getCheckInTime() : "--";
            String transit = record.getTransitSummary();
            String out = (record.getCheckOutTime() != null) ? record.getCheckOutTime() : "--";
            String shiftInfo = (record.getAssignedShift() != null) ? record.getAssignedShift() : "--";
            String overtime = (record.getOvertimeHours() != null) ? record.getOvertimeHours() : "--";
            String location = (record.getLocationName() != null) ? record.getLocationName() : "N/A";
            String distance = (record.getCheckInTime() != null) ? String.valueOf(Math.round(record.getDistanceMeters())) : "--";
            
            String finger = record.isFingerprintVerified() ? "YES" : "NO";
            String gps = record.isGpsVerified() ? "YES" : "NO";
            
            // LOGIC SETUP
            String status = record.getStatus();
            String hours = (record.getTotalHours() != null) ? record.getTotalHours() : "0h 00m";
            String remarks = (record.getRemarks() != null) ? record.getRemarks() : "";

            // SCENARIO 1: Emergency Leave (Not Resumed)
            if (record.getEmergencyLeaveTime() != null && record.getCheckOutTime() == null) {
                status = "Absent";
                hours = TimeUtils.calculateDuration(record.getCheckInTime(), record.getEmergencyLeaveTime());
            }

            // SCENARIO 2: Resumed Work (Check-Out exists)
            if (record.getCheckOutTime() != null) {
                String shiftDuration = calculateShiftDuration(shiftInfo);
                
                // If it was Paid Medical Leave and they resumed work
                if ("paid".equals(record.getMedicalLeaveType())) {
                    hours = shiftDuration; // Record as normal working hours
                } 
                // If it was a Resume (Late on duty or Unpaid Medical)
                else if (record.isResumeRequested()) {
                    String lateRemark = " | Checked out after " + hours + " (Assigned: " + shiftDuration + ")";
                    if ("none".equals(record.getMedicalLeaveType()) || record.getMedicalLeaveType() == null) {
                        remarks += " | Late Start" + lateRemark;
                    } else {
                        remarks += lateRemark;
                    }
                }
            }

            // Append row to string (Wrap multi-word strings in quotes)
            csvData.append(date).append(",")
                    .append(day).append(",")
                    .append(in).append(",")
                    .append("\"").append(transit).append("\",")
                    .append(out).append(",")
                    .append(shiftInfo).append(",")
                    .append(hours).append(",")
                    .append(overtime).append(",")
                    .append("\"").append(location).append("\",")
                    .append(distance).append(",")
                    .append(finger).append(",")
                    .append(gps).append(",")
                    .append(status).append(",")
                    .append("\"").append(remarks).append("\"\n");
        }

        // 3. Save and Share
        try {
            File folder = new File(context.getCacheDir(), "reports");
            if (!folder.exists()) folder.mkdirs();
            
            File file = new File(folder, fileName + ".csv");
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(csvData.toString().getBytes());
            outStream.close();

            shareCsvFile(context, file);

        } catch (IOException e) {
            Log.e(TAG, "CSV Generation failed", e);
            Toast.makeText(context, "Error generating CSV file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper to extract duration from a shift string like "09:00 AM - 06:00 PM"
     */
    private static String calculateShiftDuration(String shiftStr) {
        if (shiftStr == null || !shiftStr.contains("-")) return "0h 00m";
        try {
            String[] parts = shiftStr.split("-");
            if (parts.length == 2) {
                return TimeUtils.calculateDuration(parts[0].trim(), parts[1].trim());
            }
        } catch (Exception e) {
            Log.e(TAG, "Shift parse error", e);
        }
        return "0h 00m";
    }

    private static void shareCsvFile(Context context, File file) {
        Uri path = FileProvider.getUriForFile(context, "com.inout.app.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Attendance Report Export");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, path);
        context.startActivity(Intent.createChooser(intent, "Export Report via:"));
    }
}