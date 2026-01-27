package com.inout.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.inout.app.databinding.ActivityAdminDashboardBinding;
import com.inout.app.utils.EncryptionHelper;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(binding.toolbar);

        // Setup Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_admin);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_admin_employees, 
                    R.id.nav_admin_attendance, 
                    R.id.nav_admin_locations, 
                    R.id.nav_admin_qr)
                    .build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        } else if (item.getItemId() == R.id.action_switch_company) {
            switchCompany();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * UPDATED: Full Logout Logic.
     * 1. Signs out of Firebase.
     * 2. Clears the "Admin" role from local storage.
     * 3. Returns to the absolute landing page (Splash/Role Selection).
     */
    private void logout() {
        // Sign out from Firebase
        mAuth.signOut();
        
        // Clear the stored Role (Admin) so they must declare it again
        EncryptionHelper.getInstance(this).clearUserRole();
        
        // Return to SplashActivity and clear the activity history stack
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void switchCompany() {
        // To switch company, we go back to the Setup screen
        mAuth.signOut();
        // Note: For switching company, we might not clear the role, 
        // just go back to AdminSetupActivity.
        Intent intent = new Intent(this, AdminSetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}