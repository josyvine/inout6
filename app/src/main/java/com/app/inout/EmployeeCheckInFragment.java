package com.inout.app;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inout.app.databinding.FragmentEmployeeCheckinBinding;
import com.inout.app.models.AttendanceRecord;
import com.inout.app.models.CompanyConfig;
import com.inout.app.models.User;
import com.inout.app.utils.BiometricHelper;
import com.inout.app.utils.LocationHelper;
import com.inout.app.utils.TimeUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fragment where employees perform Check-In, Transit, and Check-Out.
 * UPDATED: Includes logic for Resume mode, Medical Leave status, and strict Shift Start Time restriction.
 */
public class EmployeeCheckInFragment extends Fragment {

    private static final String TAG = "CheckInFrag";
    private FragmentEmployeeCheckinBinding binding;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LocationHelper locationHelper;
    
    private User currentUser;
    private CompanyConfig assignedLocation;
    private AttendanceRecord todayRecord;

    // Action Constants
    private static final int ACTION_IN = 1;
    private static final int ACTION_TRANSIT = 2;
    private static final int ACTION_OUT = 3;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEmployeeCheckinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        locationHelper = new LocationHelper(requireContext());

        updateButtonState(false, false, false);

        loadUserDataAndStatus();

        binding.btnCheckIn.setOnClickListener(v -> initiateAction(ACTION_IN));
        binding.btnTransit.setOnClickListener(v -> initiateAction(ACTION_TRANSIT));
        binding.btnCheckOut.setOnClickListener(v -> initiateAction(ACTION_OUT));
    }

    private void updateButtonState(boolean in, boolean transit, boolean out) {
        binding.btnCheckIn.setEnabled(in);
        binding.btnTransit.setEnabled(transit);
        binding.btnCheckOut.setEnabled(out);
        
        binding.btnCheckIn.setAlpha(in ? 1.0f : 0.5f);
        binding.btnTransit.setAlpha(transit ? 1.0f : 0.5f);
        binding.btnCheckOut.setAlpha(out ? 1.0f : 0.5f);
    }

    private void loadUserDataAndStatus() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        
        db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null) return;
            
            if (doc != null && doc.exists()) {
                currentUser = doc.toObject(User.class);
                
                if (currentUser != null) {
                    binding.tvEmployeeName.setText(currentUser.getName() != null ? currentUser.getName() : "Unknown User");
                    binding.tvEmployeeName.setVisibility(View.VISIBLE);
                    binding.tvEmployeeId.setText(currentUser.getEmployeeId() != null ? currentUser.getEmployeeId() : "Pending ID");

                    String locId = currentUser.getAssignedLocationId();
                    
                    if (locId != null && !locId.isEmpty()) {
                        fetchAssignedLocationDetails(locId);
                    } else {
                        binding.tvStatus.setText("Status: No workplace assigned by Admin.");
                        updateButtonState(false, false, false);
                    }
                    
                    loadTodayAttendance();
                }
            }
        });
    }

    private void fetchAssignedLocationDetails(String locId) {
        db.collection("locations").document(locId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                assignedLocation = doc.toObject(CompanyConfig.class);
                assignedLocation.setId(doc.getId());
                Log.d(TAG, "Assigned to: " + assignedLocation.getName());
                updateUIBasedOnStatus();
            } else {
                binding.tvStatus.setText("Status: Workplace record not found.");
            }
        }).addOnFailureListener(e -> binding.tvStatus.setText("Status: Error fetching location."));
    }

    private void loadTodayAttendance() {
        if (currentUser == null || currentUser.getEmployeeId() == null) return;
        
        String dateId = TimeUtils.getCurrentDateId();
        String recordId = currentUser.getEmployeeId() + "_" + dateId;

        db.collection("attendance").document(recordId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                todayRecord = snapshot.toObject(AttendanceRecord.class);
            } else {
                todayRecord = null;
            }
            updateUIBasedOnStatus();
        });
    }

    private void updateUIBasedOnStatus() {
        if (currentUser == null || assignedLocation == null) return;

        String locName = assignedLocation.getName();

        // Case 1: Start of Day (todayRecord is null OR checkInTime is null but resume was requested)
        if (todayRecord == null || (todayRecord.getCheckInTime() == null && todayRecord.isResumeRequested())) {
            
            // LOGIC: Check if system time has reached assigned shift start time
            boolean isTimeReached = TimeUtils.isTimeReached(currentUser.getShiftStartTime());
            // Resume requested bypasses the time restriction
            boolean canCheckInGate = isTimeReached || (todayRecord != null && todayRecord.isResumeRequested());

            if (todayRecord != null && todayRecord.isResumeRequested()) {
                updateButtonState(true, false, false);
                binding.tvStatus.setText("Resume Mode: Ready to Check-In at " + locName);
            } else if (!isTimeReached) {
                // LOCK: Disable button before shift start time
                updateButtonState(false, false, false);
                binding.tvStatus.setText("Shift starts at " + currentUser.getShiftStartTime() + ". Please wait.");
            } else if ("approved".equals(currentUser.getMedicalLeaveStatus())) {
                updateButtonState(false, false, false);
                binding.tvStatus.setText("Status: Medical Leave (" + currentUser.getMedicalLeaveType().toUpperCase() + "). Click Resume to work.");
            } else if (currentUser.isTraveling()) {
                updateButtonState(true, false, false);
                binding.tvStatus.setText("Status: Traveling Mode Enabled. Ready to Start.");
            } else {
                updateButtonState(true, false, false);
                binding.tvStatus.setText("Status: Ready to Check-In at " + locName);
            }
            
        } else if (todayRecord.getCheckOutTime() == null || todayRecord.getCheckOutTime().isEmpty()) {
            // Case 2: Currently Checked In
            String lastLocId = todayRecord.getLastVerifiedLocationId();
            String currentLocId = assignedLocation.getId();
            boolean allowTransit = false;
            
            if (lastLocId != null && !lastLocId.equals(currentLocId)) {
                allowTransit = true;
                binding.tvStatus.setText("Transit Required: Move to " + locName);
            } else {
                if (todayRecord.getEmergencyLeaveTime() != null) {
                    binding.tvStatus.setText("Status: On Emergency Leave. (Resumed duty? You can still transit or check-out)");
                } else {
                    binding.tvStatus.setText("Status: Working at " + locName);
                }
            }
            
            updateButtonState(false, allowTransit, true);
            
        } else {
            // Case 3: Shift Completed
            updateButtonState(false, false, false);
            binding.tvStatus.setText("Status: Shift Completed (" + todayRecord.getTotalHours() + ")");
        }
    }

    private void initiateAction(int actionType) {
        if (assignedLocation == null) {
            Toast.makeText(getContext(), "Error: Office location not assigned.", Toast.LENGTH_LONG).show();
            return;
        }

        BiometricHelper.authenticate(requireActivity(), new BiometricHelper.BiometricCallback() {
            @Override
            public void onAuthenticationSuccess() {
                verifyLocationAndProceed(actionType);
            }

            @Override
            public void onAuthenticationError(String errorMsg) {
                Toast.makeText(getContext(), "Auth Error: " + errorMsg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(getContext(), "Fingerprint not recognized.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyLocationAndProceed(int actionType) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        locationHelper.getCurrentLocation(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationResult(Location location) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (location != null) {
                    boolean inRange = LocationHelper.isWithinRadius(
                            location.getLatitude(), location.getLongitude(),
                            assignedLocation.getLatitude(), assignedLocation.getLongitude(),
                            assignedLocation.getRadius());

                    // Traveling Mode Bypass
                    if (actionType == ACTION_IN && currentUser.isTraveling()) {
                        performCheckIn(location, 0, true); 
                    } 
                    else if (inRange) {
                        float dist = LocationHelper.calculateDistance(
                                location.getLatitude(), location.getLongitude(),
                                assignedLocation.getLatitude(), assignedLocation.getLongitude());
                        
                        if (actionType == ACTION_IN) performCheckIn(location, dist, false);
                        else if (actionType == ACTION_TRANSIT) performTransit(location, dist);
                        else if (actionType == ACTION_OUT) performCheckOut(location);
                    } else {
                        String msg = "Denied: You are not at " + assignedLocation.getName() + ".";
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "GPS Error: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @param isRemoteStart If true, user is checking in from home/travel, not the office.
     */
    private void performCheckIn(Location loc, float distance, boolean isRemoteStart) {
        String dateId = TimeUtils.getCurrentDateId();
        String recordId = currentUser.getEmployeeId() + "_" + dateId;

        AttendanceRecord record;
        if (todayRecord != null) {
            record = todayRecord; 
        } else {
            record = new AttendanceRecord(currentUser.getEmployeeId(), currentUser.getName(), dateId, TimeUtils.getCurrentTimestamp());
            record.setRecordId(recordId);
        }

        record.setCheckInTime(TimeUtils.getCurrentTime());
        record.setCheckInLat(loc.getLatitude());
        record.setCheckInLng(loc.getLongitude());
        record.setFingerprintVerified(true);
        record.setLocationVerified(true);
        record.setDistanceMeters(distance);
        
        String shiftInfo = "N/A";
        if (currentUser.getShiftStartTime() != null && currentUser.getShiftEndTime() != null) {
            shiftInfo = currentUser.getShiftStartTime() + " - " + currentUser.getShiftEndTime();
        }
        record.setAssignedShift(shiftInfo);

        List<String> moves = new ArrayList<>();
        
        if (isRemoteStart) {
            String addressName = getAddressName(loc);
            record.setStartLocationName(addressName); 
            record.setLocationName(assignedLocation.getName()); 
            moves.add("Started at " + addressName); 
        } else {
            record.setLocationName(assignedLocation.getName());
            moves.add(assignedLocation.getName());
        }
        
        record.setMovementLog(moves);
        record.setLastVerifiedLocationId(assignedLocation.getId());

        db.collection("attendance").document(recordId).set(record)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Check-In Success!", Toast.LENGTH_SHORT).show());
    }

    private void performTransit(Location loc, float distance) {
        if (todayRecord == null) return;

        float newTotalDist = todayRecord.getDistanceMeters() + distance;
        String newLocName = assignedLocation.getName();

        db.collection("attendance").document(todayRecord.getRecordId())
                .update(
                    "distanceMeters", newTotalDist,
                    "locationName", newLocName,
                    "lastVerifiedLocationId", assignedLocation.getId(),
                    "movementLog", FieldValue.arrayUnion(newLocName)
                )
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Transit Verified!", Toast.LENGTH_SHORT).show());
    }

    private void performCheckOut(Location loc) {
        if (todayRecord == null) return;

        String checkOutTime = TimeUtils.getCurrentTime();
        String totalHrs = TimeUtils.calculateDuration(todayRecord.getCheckInTime(), checkOutTime);
        String overtimeStr = calculateOvertime(todayRecord.getCheckInTime(), checkOutTime);

        db.collection("attendance").document(todayRecord.getRecordId())
                .update(
                        "checkOutTime", checkOutTime,
                        "checkOutLat", loc.getLatitude(),
                        "checkOutLng", loc.getLongitude(),
                        "totalHours", totalHrs,
                        "overtimeHours", overtimeStr 
                )
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Check-Out Success!", Toast.LENGTH_SHORT).show());
    }

    private String calculateOvertime(String inTime, String outTime) {
        if (currentUser.getShiftStartTime() == null || currentUser.getShiftEndTime() == null) return "0h 00m";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date shiftStart = sdf.parse(currentUser.getShiftStartTime());
            Date shiftEnd = sdf.parse(currentUser.getShiftEndTime());
            long shiftMillis = shiftEnd.getTime() - shiftStart.getTime();

            Date actualIn = sdf.parse(inTime);
            Date actualOut = sdf.parse(outTime);
            long workedMillis = actualOut.getTime() - actualIn.getTime();

            if (workedMillis > shiftMillis) {
                long otMillis = workedMillis - shiftMillis;
                long hours = TimeUnit.MILLISECONDS.toHours(otMillis);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(otMillis) % 60;
                return String.format(Locale.US, "%dh %02dm", hours, minutes);
            }
        } catch (Exception e) {
            Log.e(TAG, "Overtime calc failed", e);
        }
        return "0h 00m";
    }

    private String getAddressName(Location loc) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                String street = addr.getThoroughfare() != null ? addr.getThoroughfare() : "";
                String city = addr.getLocality() != null ? addr.getLocality() : "";
                return (street + " " + city).trim();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed", e);
        }
        return "Remote Location";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}