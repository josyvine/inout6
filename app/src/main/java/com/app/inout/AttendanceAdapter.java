package com.inout.app.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inout.app.R;
import com.inout.app.models.AttendanceRecord;
import com.inout.app.utils.TimeUtils;

import java.util.List;

/**
 * Professional Adapter for the 14-column CSV attendance table.
 * UPDATED: Handles logic for Paid Medical Leave (Full Shift Credit) and Resume/Late Start hours.
 */
public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<AttendanceRecord> attendanceList;

    public AttendanceAdapter(List<AttendanceRecord> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_row, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = attendanceList.get(position);

        // Reset Alpha for recycled views
        holder.tvDate.setAlpha(1.0f);
        holder.tvDay.setAlpha(1.0f);

        // 1. Date & Day
        holder.tvDate.setText(record.getDate());
        holder.tvDay.setText(record.getDayOfWeek() != null ? record.getDayOfWeek() : "--");

        // 2. Check-In
        holder.tvIn.setText(record.getCheckInTime() != null ? record.getCheckInTime() : "--:--");

        // 3. Transit Route
        holder.tvTransit.setText(record.getTransitSummary());

        // 4. Check-Out
        holder.tvOut.setText(record.getCheckOutTime() != null ? record.getCheckOutTime() : "--:--");

        // 5. Assigned Shift
        holder.tvShift.setText(record.getAssignedShift() != null ? record.getAssignedShift() : "--");

        // 6. Total Hours Logic (Enhanced for Resume/Medical)
        String hoursDisplay = record.getTotalHours() != null ? record.getTotalHours() : "0h 00m";

        // Scenario A: Emergency Leave (Not Resumed)
        if (record.getEmergencyLeaveTime() != null && record.getCheckOutTime() == null) {
            hoursDisplay = TimeUtils.calculateDuration(record.getCheckInTime(), record.getEmergencyLeaveTime());
        }
        // Scenario B: Paid Medical Leave + Resumed Work (Full Shift Credit)
        else if ("paid".equals(record.getMedicalLeaveType()) && record.getCheckOutTime() != null) {
            hoursDisplay = calculateShiftDuration(record.getAssignedShift());
        }
        
        holder.tvTotalHours.setText(hoursDisplay);

        // 7. Overtime
        holder.tvOvertime.setText(record.getOvertimeHours() != null ? record.getOvertimeHours() : "--");

        // 8. Location Name
        holder.tvLocation.setText(record.getLocationName() != null ? record.getLocationName() : "N/A");

        // 9. Distance
        if (record.getCheckInTime() != null) {
            holder.tvDistance.setText(Math.round(record.getDistanceMeters()) + "m");
        } else {
            holder.tvDistance.setText("--");
        }

        // 10. Fingerprint Verification
        if (record.getCheckInTime() != null) {
            holder.ivFingerprint.setImageResource(record.isFingerprintVerified() ? 
                    R.drawable.ic_status_present : R.drawable.ic_status_absent);
        } else {
            holder.ivFingerprint.setImageResource(R.drawable.ic_status_absent);
        }

        // 11. GPS Verification
        if (record.getCheckInTime() != null) {
            holder.ivGps.setImageResource(record.isGpsVerified() ? 
                    R.drawable.ic_status_present : R.drawable.ic_status_absent);
        } else {
            holder.ivGps.setImageResource(R.drawable.ic_status_absent);
        }

        // 12. Overall Status
        String status = record.getStatus();
        if (status.equals("Present")) {
            holder.ivStatus.setImageResource(R.drawable.ic_status_present);
        } else if (status.equals("Partial")) {
            holder.ivStatus.setImageResource(R.drawable.ic_status_partial);
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_status_absent);
            holder.tvDate.setAlpha(0.5f);
            holder.tvDay.setAlpha(0.5f);
        }

        // 13. Remarks
        holder.tvRemarks.setText(record.getRemarks() != null ? record.getRemarks() : "");
    }

    /**
     * Helper to calculate the full duration of an assigned shift string for UI credit.
     */
    private String calculateShiftDuration(String shiftStr) {
        if (shiftStr == null || !shiftStr.contains("-")) return "0h 00m";
        try {
            String[] parts = shiftStr.split("-");
            if (parts.length == 2) {
                return TimeUtils.calculateDuration(parts[0].trim(), parts[1].trim());
            }
        } catch (Exception e) {
            Log.e("AttendanceAdapter", "Shift parse error", e);
        }
        return "0h 00m";
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDay, tvIn, tvTransit, tvOut, tvShift, tvTotalHours, tvOvertime, tvLocation, tvDistance, tvRemarks;
        ImageView ivFingerprint, ivGps, ivStatus;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_col_date);
            tvDay = itemView.findViewById(R.id.tv_col_day);
            tvIn = itemView.findViewById(R.id.tv_col_in);
            tvTransit = itemView.findViewById(R.id.tv_col_transit);
            tvOut = itemView.findViewById(R.id.tv_col_out);
            tvShift = itemView.findViewById(R.id.tv_col_shift);
            tvTotalHours = itemView.findViewById(R.id.tv_col_hours);
            tvOvertime = itemView.findViewById(R.id.tv_col_overtime);
            tvLocation = itemView.findViewById(R.id.tv_col_location);
            tvDistance = itemView.findViewById(R.id.tv_col_distance);
            ivFingerprint = itemView.findViewById(R.id.iv_col_fingerprint);
            ivGps = itemView.findViewById(R.id.iv_col_gps);
            ivStatus = itemView.findViewById(R.id.iv_col_status);
            tvRemarks = itemView.findViewById(R.id.tv_col_remarks);
        }
    }
}