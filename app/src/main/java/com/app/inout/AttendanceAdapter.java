package com.inout.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inout.app.R;
import com.inout.app.models.AttendanceRecord;

import java.util.List;

/**
 * Professional Adapter for the 13-column CSV attendance table.
 * UPDATED: Handles Transit Route, Assigned Shift, and Overtime columns.
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

        // Reset Alpha for recycled views to prevent visual glitches
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

        // 5. NEW: Assigned Shift
        holder.tvShift.setText(record.getAssignedShift() != null ? record.getAssignedShift() : "--");

        // 6. Total Hours
        holder.tvTotalHours.setText(record.getTotalHours() != null ? record.getTotalHours() : "0h 00m");

        // 7. NEW: Overtime
        holder.tvOvertime.setText(record.getOvertimeHours() != null ? record.getOvertimeHours() : "--");

        // 8. Location Name
        holder.tvLocation.setText(record.getLocationName() != null ? record.getLocationName() : "N/A");

        // 9. Distance
        if (record.getCheckInTime() != null) {
            holder.tvDistance.setText(Math.round(record.getDistanceMeters()) + "m");
        } else {
            holder.tvDistance.setText("--");
        }

        // 10. Fingerprint Verification (Icon)
        if (record.getCheckInTime() != null) {
            holder.ivFingerprint.setImageResource(record.isFingerprintVerified() ? 
                    R.drawable.ic_status_present : R.drawable.ic_status_absent);
        } else {
            holder.ivFingerprint.setImageResource(R.drawable.ic_status_absent);
        }

        // 11. GPS Verification (Icon)
        if (record.getCheckInTime() != null) {
            holder.ivGps.setImageResource(record.isGpsVerified() ? 
                    R.drawable.ic_status_present : R.drawable.ic_status_absent);
        } else {
            holder.ivGps.setImageResource(R.drawable.ic_status_absent);
        }

        // 12. Overall Status Logic
        String status = record.getStatus();
        if (status.equals("Present")) {
            holder.ivStatus.setImageResource(R.drawable.ic_status_present);
        } else if (status.equals("Partial")) {
            holder.ivStatus.setImageResource(R.drawable.ic_status_partial);
        } else {
            // Absent
            holder.ivStatus.setImageResource(R.drawable.ic_status_absent);
            // Gray out the date/day for absent rows
            holder.tvDate.setAlpha(0.5f);
            holder.tvDay.setAlpha(0.5f);
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    /**
     * ViewHolder maps the 13 columns defined in item_attendance_row.xml
     */
    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDay, tvIn, tvTransit, tvOut, tvShift, tvTotalHours, tvOvertime, tvLocation, tvDistance;
        ImageView ivFingerprint, ivGps, ivStatus;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_col_date);
            tvDay = itemView.findViewById(R.id.tv_col_day);
            tvIn = itemView.findViewById(R.id.tv_col_in);
            tvTransit = itemView.findViewById(R.id.tv_col_transit);
            tvOut = itemView.findViewById(R.id.tv_col_out);
            tvShift = itemView.findViewById(R.id.tv_col_shift); // New Field
            tvTotalHours = itemView.findViewById(R.id.tv_col_hours);
            tvOvertime = itemView.findViewById(R.id.tv_col_overtime); // New Field
            tvLocation = itemView.findViewById(R.id.tv_col_location);
            tvDistance = itemView.findViewById(R.id.tv_col_distance);
            ivFingerprint = itemView.findViewById(R.id.iv_col_fingerprint);
            ivGps = itemView.findViewById(R.id.iv_col_gps);
            ivStatus = itemView.findViewById(R.id.iv_col_status);
        }
    }
}