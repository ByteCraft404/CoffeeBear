package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class profile extends AppCompatActivity {

    private Button editProfileButton;
    private TextView emailTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        editProfileButton = findViewById(R.id.editProfileButton);
        emailTextView = findViewById(R.id.emailTextView);

        // Fetch and display user's email
        displayUserEmail();

        // Set an OnClickListener for the Edit Profile button
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the EditProfileActivity
                Intent intent = new Intent(profile.this, EditProfile.class);
                // Start the new activity
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void displayUserEmail() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            emailTextView.setText(email);
        } else {
            // Handle the case where the user is not logged in or there's an error
            emailTextView.setText("Could not fetch email");
            Toast.makeText(this, "Failed to retrieve user email.", Toast.LENGTH_SHORT).show();
            // Optionally, you might want to redirect the user to the login screen
        }
    }
}