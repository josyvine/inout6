package com.inout.app;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.inout.app.databinding.FragmentAdminLocationsBinding;
import com.inout.app.models.CompanyConfig;
import com.inout.app.utils.LocationHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminLocationsFragment extends Fragment {

    private static final String TAG = "AdminLocationsFrag";
    private FragmentAdminLocationsBinding binding;
    private FirebaseFirestore db;
    private LocationHelper locationHelper;
    
    private double capturedLat = 0;
    private double capturedLng = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminLocationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        locationHelper = new LocationHelper(requireContext());

        setupClickListeners();
        listenForLocations();
    }

    private void setupClickListeners() {
        // Button to capture current GPS coordinates for the new office location
        binding.btnCaptureGps.setOnClickListener(v -> captureCurrentLocation());

        // Button to save the location to Firestore
        binding.btnSaveLocation.setOnClickListener(v -> saveLocationToFirestore());
    }

    private void captureCurrentLocation() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCaptureGps.setEnabled(false);

        locationHelper.getCurrentLocation(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationResult(Location location) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCaptureGps.setEnabled(true);

                if (location != null) {
                    capturedLat = location.getLatitude();
                    capturedLng = location.getLongitude();
                    
                    binding.tvCapturedCoords.setText(String.format("Lat: %.6f\nLng: %.6f", capturedLat, capturedLng));
                    binding.tvCapturedCoords.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Coordinates Captured Successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMsg) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCaptureGps.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveLocationToFirestore() {
        String locName = binding.etLocationName.getText().toString().trim();

        if (TextUtils.isEmpty(locName)) {
            binding.etLocationName.setError("Location Name is required");
            return;
        }

        if (capturedLat == 0 || capturedLng == 0) {
            Toast.makeText(getContext(), "Please capture GPS coordinates first", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        CompanyConfig config = new CompanyConfig(locName, capturedLat, capturedLng);
        // Default radius is set to 100m in the model constructor

        db.collection("locations")
                .add(config)
                .addOnSuccessListener(documentReference -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Location Added Successfully", Toast.LENGTH_SHORT).show();
                    clearInputs();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error saving location", e);
                    Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearInputs() {
        binding.etLocationName.setText("");
        binding.tvCapturedCoords.setText("");
        binding.tvCapturedCoords.setVisibility(View.GONE);
        capturedLat = 0;
        capturedLng = 0;
    }

    private void listenForLocations() {
        // This logic is for the list view of already saved locations
        db.collection("locations")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed.", error);
                            return;
                        }

                        if (value != null) {
                            StringBuilder sb = new StringBuilder("Saved Locations:\n");
                            for (DocumentSnapshot doc : value) {
                                CompanyConfig config = doc.toObject(CompanyConfig.class);
                                if (config != null) {
                                    sb.append("- ").append(config.getName()).append(" (100m)\n");
                                }
                            }
                            binding.tvLocationList.setText(sb.toString());
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}