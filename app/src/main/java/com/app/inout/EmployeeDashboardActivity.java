package com.inout.app;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.inout.app.databinding.ActivityEmployeeDashboardBinding;
import com.inout.app.models.AttendanceRecord;
import com.inout.app.models.User;
import com.inout.app.utils.EncryptionHelper;
import com.inout.app.utils.LocationHelper;
import com.inout.app.utils.TimeUtils;

/**
 * Main dashboard for Employees.
 * Handles navigation between Check-In/Out and Attendance History.
 * Monitors Admin Approval status and Profile completeness.
 * UPDATED: Handles Emergency Leave request logic from Top Menu.
 */
public class EmployeeDashboardActivity extends AppCompatActivity {

    private ActivityEmployeeDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User currentUser;
    private AttendanceRecord todayRecord;
    private ListenerRegistration userListener;
    private ListenerRegistration attendanceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeeDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbar);

        // Setup Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_employee);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // Destinations that are considered "Top Level"
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_employee_checkin, 
                    R.id.nav_employee_history)
                    .build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);
        }

        checkUserProfileAndStatus();
        observeAttendanceStatus();
    }

    private void observeAttendanceStatus() {
        FirebaseUser fbUser = mAuth.getCurrentUser();
        if (fbUser == null) return;

        // FIXED LOGIC: Changed .get() to .addSnapshotListener to ensure real-time sync of employeeId
        userListener = db.collection("users").document(fbUser.getUid()).addSnapshotListener((userDoc, error) -> {
            if (userDoc != null && userDoc.exists()) {
                User u = userDoc.toObject(User.class);
                if (u != null && u.getEmployeeId() != null) {
                    
                    // Avoid creating multiple attendance listeners
                    if (attendanceListener != null) attendanceListener.remove();

                    String dateId = TimeUtils.getCurrentDateId();
                    String recordId = u.getEmployeeId() + "_" + dateId;

                    attendanceListener = db.collection("attendance").document(recordId).addSnapshotListener((snapshot, e) -> {
                        if (snapshot != null && snapshot.exists()) {
                            todayRecord = snapshot.toObject(AttendanceRecord.class);
                        } else {
                            todayRecord = null;
                        }
                        // Refresh the menu state immediately when check-in data changes
                        invalidateOptionsMenu(); 
                    });
                }
            }
        });
    }

    private void checkUserProfileAndStatus() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        db.collection("users").document(firebaseUser.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;

                    if (snapshot != null && snapshot.exists()) {
                        currentUser = snapshot.toObject(User.class);
                        if (currentUser != null) {
                            // 1. Check if basic profile data is missing
                            if (currentUser.getPhone() == null || currentUser.getPhone().isEmpty() || 
                                currentUser.getPhotoUrl() == null || currentUser.getPhotoUrl().isEmpty()) {

                                Toast.makeText(this, "Please complete your profile first.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, EmployeeProfileActivity.class));
                                // We don't finish() here so they can come back after saving
                                return;
                            }

                            // 2. Check for Admin Approval
                            if (!currentUser.isApproved()) {
                                showWaitingOverlay(true);
                            } else {
                                showWaitingOverlay(false);
                            }
                        }
                    }
                });
    }

    /**
     * Shows or hides an overlay that blocks interaction until the admin approves the account.
     */
    private void showWaitingOverlay(boolean show) {
        if (show) {
            binding.layoutWaitingApproval.setVisibility(View.VISIBLE);
            binding.navView.setVisibility(View.GONE);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Pending Approval");
        } else {
            binding.layoutWaitingApproval.setVisibility(View.GONE);
            binding.navView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.employee_top_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem emergencyItem = menu.findItem(R.id.action_emergency_leave);
        if (emergencyItem != null) {
            // Logic: Enabled only if Checked In AND Not Checked Out
            boolean canTakeLeave = todayRecord != null && 
                                   todayRecord.getCheckInTime() != null && 
                                   todayRecord.getCheckOutTime() == null;

            emergencyItem.setEnabled(canTakeLeave);

            // FIXED: Added null check for getIcon() to prevent NullPointerException
            if (emergencyItem.getIcon() != null) {
                emergencyItem.getIcon().setAlpha(canTakeLeave ? 255 : 128);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_profile) {
            startActivity(new Intent(this, EmployeeProfileActivity.class));
            return true;
        } else if (id == R.id.action_emergency_leave) {
            handleEmergencyLeaveRequest();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleEmergencyLeaveRequest() {
        if (todayRecord == null || currentUser == null) return;

        new LocationHelper(this).getCurrentLocation(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationResult(Location location) {
                String leaveTime = TimeUtils.getCurrentTime();
                String leaveLoc = todayRecord.getLocationName();
                String remarks = "Emergency leave at " + leaveLoc + " took at " + leaveTime;

                // Update Attendance Record
                db.collection("attendance").document(todayRecord.getRecordId())
                        .update("emergencyLeaveTime", leaveTime,
                                "emergencyLeaveLocation", leaveLoc,
                                "remarks", remarks)
                        .addOnSuccessListener(aVoid -> {
                            // Update User Status to Pending
                            db.collection("users").document(currentUser.getUid())
                                    .update("emergencyLeaveStatus", "pending")
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(EmployeeDashboardActivity.this, "Emergency Leave Requested. Waiting for Admin Approval.", Toast.LENGTH_LONG).show();
                                    });
                        });
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(EmployeeDashboardActivity.this, "Location required for Emergency Leave.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * UPDATED: Full Logout Logic.
     * 1. Signs out of Firebase.
     * 2. Signs out of Google (forces account picker for next login).
     * 3. Clears the "Employee" role from local storage.
     * 4. Returns to the absolute landing page (Splash/Role Selection).
     */
    private void logout() {
        // Clean up listeners
        if (userListener != null) userListener.remove();
        if (attendanceListener != null) attendanceListener.remove();
        
        // 1. Sign out from Firebase
        mAuth.signOut();

        // 2. Configure and sign out from Google to allow picking a different Gmail next time
        String webClientId = EncryptionHelper.getInstance(this).getWebClientId();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // 3. Clear the stored Role (Employee) locally
            EncryptionHelper.getInstance(EmployeeDashboardActivity.this).clearUserRole();

            // 4. Return to SplashActivity and clear the entire activity history stack
            Intent intent = new Intent(EmployeeDashboardActivity.this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}