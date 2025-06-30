package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private LinearLayout editProfileButton; // Changed to LinearLayout if that's the actual view in your XML
    private TextView emailTextView;
    private FirebaseAuth mAuth;

    // Bottom Navigation Views
    private LinearLayout homeButton;
    private LinearLayout favoritesButton;
    private LinearLayout cartButton;
    private LinearLayout profileButton;

    private ImageView homeIcon;
    private TextView homeText;
    private ImageView favoritesIcon;
    private TextView favoritesText;
    private ImageView cartIcon;
    private TextView cartText;
    private ImageView profileIcon;
    private TextView profileText;

    private int colorCoffeeBeigeLight;
    private int colorA0A0A0;
    private int colorCoffeeDarkText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        editProfileButton = findViewById(R.id.editProfileButton);
        emailTextView = findViewById(R.id.emailTextView);

        // Initialize Bottom Navigation Views
        homeButton = findViewById(R.id.homeButton);
        favoritesButton = findViewById(R.id.favoritesButton);
        cartButton = findViewById(R.id.cartButton);
        profileButton = findViewById(R.id.profileButton);

        // Find ImageView and TextView within each LinearLayout
        homeIcon = homeButton.findViewById(R.id.homeIcon);
        homeText = homeButton.findViewById(R.id.homeText);
        favoritesIcon = favoritesButton.findViewById(R.id.favoritesIcon);
        favoritesText = favoritesButton.findViewById(R.id.favoritesText);
        cartIcon = cartButton.findViewById(R.id.cartIcon);
        cartText = cartButton.findViewById(R.id.cartText);
        profileIcon = profileButton.findViewById(R.id.profileIcon);
        profileText = profileButton.findViewById(R.id.profileText);

        // Get color values from resources
        colorCoffeeBeigeLight = getResources().getColor(R.color.coffee_beige_light, getTheme());
        colorA0A0A0 = getResources().getColor(android.R.color.darker_gray, getTheme());
        colorCoffeeDarkText = getResources().getColor(R.color.coffee_dark_text, getTheme());

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

        // Set up bottom navigation click listeners
        homeButton.setOnClickListener(v -> navigateTo(MainActivity2.class));
        favoritesButton.setOnClickListener(v -> navigateTo(Favourites.class)); // Replace with your Favorites Activity
        cartButton.setOnClickListener(v -> navigateTo(Cart.class));
        profileButton.setOnClickListener(v -> {}); // Already on Profile Activity

        // Set default selected button to Profile and change its color
        setDefaultSelected();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setDefaultSelected() {
        // Reset colors of all buttons
        if (homeIcon != null && homeText != null) {
            homeIcon.setColorFilter(colorCoffeeDarkText);
            homeText.setTextColor(colorA0A0A0);
        }

        if (favoritesIcon != null && favoritesText != null) {
            favoritesIcon.setColorFilter(colorCoffeeDarkText);
            favoritesText.setTextColor(colorA0A0A0);
        }

        if (cartIcon != null && cartText != null) {
            cartIcon.setColorFilter(colorCoffeeDarkText);
            cartText.setTextColor(colorA0A0A0);
        }

        if (profileIcon != null && profileText != null) {
            profileIcon.setColorFilter(colorCoffeeBeigeLight);
            profileText.setTextColor(colorCoffeeBeigeLight);
        }
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(profile.this, destination);
        startActivity(intent);
        finish();
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