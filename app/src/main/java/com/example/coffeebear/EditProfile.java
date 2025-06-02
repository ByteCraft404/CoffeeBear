package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private ImageView backButton, profileImageView, editIconImageView;
    private EditText yourNameEditText, yourEmailEditText, phoneEditText, passwordEditText, confirmPasswordEditText;
    private Button updateButton, logoutButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();


        backButton = findViewById(R.id.backButton);
        profileImageView = findViewById(R.id.profileImageView);
        editIconImageView = findViewById(R.id.editIconImageView);
        yourNameEditText = findViewById(R.id.yourNameEditText);
        yourEmailEditText = findViewById(R.id.yourEmailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        updateButton = findViewById(R.id.updateButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Fetch user data
        if (currentUser != null) {
            loadUserData();
        }

        // Set up listeners
        backButton.setOnClickListener(v -> finish()); // Go back to the previous screen
        logoutButton.setOnClickListener(v -> logoutUser());
        updateButton.setOnClickListener(v -> updateProfile());
        editIconImageView.setOnClickListener(v -> showImageNotEditableToast());
    }

    private void showImageNotEditableToast() {
        Toast.makeText(this, "You cannot edit the profile image.", Toast.LENGTH_SHORT).show();
    }

    private void loadUserData() {
        mFirestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        String imageUrl = documentSnapshot.getString("imageUrl"); // Still fetching in case you display it

                        yourNameEditText.setText(name);
                        yourEmailEditText.setText(email);
                        phoneEditText.setText(phone);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.cappuchino) // Placeholder image
                                    .into(profileImageView);
                        }
                    } else {
                        Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EditProfile", "Error loading user data: ", e);
                    Toast.makeText(this, "Error loading user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(EditProfile.this, SigninActivity.class); // Replace SignInActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void updateProfile() {
        String name = yourNameEditText.getText().toString().trim();
        String email = yourEmailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all the required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", name);
        updatedData.put("email", email);
        updatedData.put("phone", phone);

        // Update email if changed
        if (!email.equals(currentUser.getEmail())) {
            updateEmail(email);
        } else {
            // Only update Firestore data if email hasn't changed or email update was successful
            updateFirestoreData(updatedData);
        }

        // Update password if provided
        if (!password.isEmpty()) {
            updatePassword(password);
        }
    }

    private void updateEmail(String newEmail) {
        // Prompt user for re-authentication (for security)
        // This is a simplified example - you might want a more user-friendly UI for this
        Toast.makeText(this, "Re-authentication required to change email.", Toast.LENGTH_LONG).show();
        // You would typically show an AlertDialog or a new activity to get current password
        // For this example, we'll assume the user re-authenticated successfully

        currentUser.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfile.this, "Email updated successfully.", Toast.LENGTH_SHORT).show();
                        // Update Firestore data after successful email update
                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("email", newEmail);
                        updateFirestoreData(updatedData);
                    } else {
                        Toast.makeText(EditProfile.this, "Failed to update email.", Toast.LENGTH_SHORT).show();
                        Log.e("EditProfile", "Error updating email: ", task.getException());
                    }
                });
    }

    private void updatePassword(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfile.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfile.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                        Log.e("EditProfile", "Error updating password: ", task.getException());
                    }
                });
    }

    private void updateFirestoreData(Map<String, Object> updatedData) {
        mFirestore.collection("users").document(currentUser.getUid())
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfile.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    // Optionally, update the name TextView on the previous screen
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedName", (String) updatedData.get("name"));
                    setResult(RESULT_OK, resultIntent);
                    finish(); // Go back to the profile screen
                })
                .addOnFailureListener(e -> {
                    Log.e("EditProfile", "Error updating Firestore data: ", e);
                    Toast.makeText(EditProfile.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                });
    }
}