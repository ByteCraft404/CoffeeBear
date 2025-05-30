package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText etEmailSignup, etPasswordSignup, etConfirmPasswordSignup;
    Button btnSignupAction;
    TextView tvSigninTabOnSignup;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://coffeebear592-default-rtdb.europe-west1.firebasedatabase.app/"); // Replace with your Firebase database URL
        mDatabase = database.getReference("users"); // Create a 'users' node to store user data

        etEmailSignup = findViewById(R.id.et_email_signup);
        etPasswordSignup = findViewById(R.id.et_password_signup);
        etConfirmPasswordSignup = findViewById(R.id.et_confirm_password_signup);
        btnSignupAction = findViewById(R.id.btn_signup_action);
        tvSigninTabOnSignup = findViewById(R.id.tv_signin_tab_on_signup);



        btnSignupAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailSignup.getText().toString().trim();
                String password = etPasswordSignup.getText().toString().trim();
                String confirmPassword = etConfirmPasswordSignup.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create user with Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Store additional user information in the database
                                    String userId = user.getUid();
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("email", email);
                                    // You can add more user details here (e.g., username, phone number)

                                    mDatabase.child(userId).setValue(userData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(SignupActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(SignupActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                                // If saving data fails, you might want to delete the Firebase Auth user
                                                user.delete();
                                            });
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(SignupActivity.this, "Sign Up failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        tvSigninTabOnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                startActivity(intent);
                finish(); // Optional
            }
        });
    }
}