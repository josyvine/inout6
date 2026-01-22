package com.inout.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inout.app.databinding.ActivityEmployeeProfileBinding;
import com.inout.app.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for Employees to set up their Profile (Name, Phone, Photo).
 * Photo is stored in Firebase Storage, details in Firestore.
 */
public class EmployeeProfileActivity extends AppCompatActivity {

    private ActivityEmployeeProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    
    private Uri selectedImageUri;
    private String existingPhotoUrl;

    // Launcher for picking an image from Gallery
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    binding.ivProfilePhoto.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeeProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        loadCurrentUserData();

        binding.btnSelectPhoto.setOnClickListener(v -> openGallery());
        binding.btnSaveProfile.setOnClickListener(v -> validateAndSave());
    }

    private void loadCurrentUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        db.collection("users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            binding.etName.setText(user.getName());
                            binding.etPhone.setText(user.getPhone());
                            existingPhotoUrl = user.getPhotoUrl();
                            // Note: Image loading from URL would typically use Glide/Picasso
                        }
                    }
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void validateAndSave() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.etName.setError("Name required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            binding.etPhone.setError("Phone required");
            return;
        }
        if (selectedImageUri == null && TextUtils.isEmpty(existingPhotoUrl)) {
            Toast.makeText(this, "Please select a profile photo", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveProfile.setEnabled(false);

        if (selectedImageUri != null) {
            uploadPhotoAndSaveData(name, phone);
        } else {
            saveFirestoreData(name, phone, existingPhotoUrl);
        }
    }

    private void uploadPhotoAndSaveData(String name, String phone) {
        String uid = mAuth.getCurrentUser().getUid();
        StorageReference photoRef = storage.getReference().child("profile_photos/" + uid + ".jpg");

        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveFirestoreData(name, phone, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, "Photo upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveFirestoreData(String name, String phone, String photoUrl) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("photoUrl", photoUrl);

        db.collection("users").document(uid)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}