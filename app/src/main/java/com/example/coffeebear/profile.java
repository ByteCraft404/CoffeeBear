package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // <--- Import Button
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class profile extends AppCompatActivity {

    // Change this from LinearLayout to Button
    private Button editProfileButton; // CORRECTED TYPE

    private TextView emailTextView;
    private FirebaseAuth mAuth;

    // Chip Navigation Bar
    private ChipNavigationBar bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        // This line will now correctly cast to Button
        editProfileButton = findViewById(R.id.editProfileButton);
        emailTextView = findViewById(R.id.emailTextView);

        // Initialize Chip Navigation Bar
        // IMPORTANT: Ensure the ID here matches the ID in your activity_profile.xml
        // In your XML, the ID is @id/bottomnav. In your Java, you're looking for @id/bottom_navigation_bar.
        // I will correct the Java here to match your XML:
        bottomNavigationBar = findViewById(R.id.bottomnav); // CORRECTED ID TO MATCH YOUR XML

        // Fetch and display user's email
        displayUserEmail();

        // Set an OnClickListener for the Edit Profile button
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(profile.this, EditProfile.class);
                startActivity(intent);
            }
        });

        // Set up ChipNavigationBar listener
        bottomNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.home) { // Make sure these match your @menu/bottom_nav_menu IDs
                    navigateTo(MainActivity2.class);
                } else if (i == R.id.favorites) {
                    navigateTo(Favourites.class);
                } else if (i == R.id.cart) {
                    navigateTo(Cart.class);
                } else if (i == R.id.profile) {
                    // Already on Profile Activity, do nothing or provide feedback
                    // Toast.makeText(profile.this, "You are already in Profile!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set Profile item as selected by default
        // Make sure R.id.profile matches an item ID in your bottom_nav_menu.xml
        bottomNavigationBar.setItemSelected(R.id.profile, true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(profile.this, destination);
        startActivity(intent);
        finish(); // Finish the current activity to prevent it from stacking
    }

    private void displayUserEmail() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            emailTextView.setText(email);
        } else {
            emailTextView.setText("Could not fetch email");
            Toast.makeText(this, "Failed to retrieve user email.", Toast.LENGTH_SHORT).show();
            // Optionally, you might want to redirect the user to the login screen
        }
    }
}