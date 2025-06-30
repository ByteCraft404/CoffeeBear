package com.example.coffeebear;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SigninActivity extends AppCompatActivity {

    EditText etEmailSignin, etPasswordSignin;
    Button btnSigninAction;
    TextView tvSignupTabOnSignin, tvForgotPassword;
    private FirebaseAuth mAuth;

    ImageView googleIconSignin;
    GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        ConstraintLayout contentAreaSignin = findViewById(R.id.content_area_signin);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();



        etEmailSignin = findViewById(R.id.et_email_signin);
        etPasswordSignin = findViewById(R.id.et_password_signin);
        btnSigninAction = findViewById(R.id.btn_signin_action);
        tvSignupTabOnSignin = findViewById(R.id.tv_signup_tab_on_signin);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        googleIconSignin = findViewById(R.id.icon_google_signin);



        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            // Google Sign In failed, update UI appropriately
                            Toast.makeText(SigninActivity.this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.w("Google Sign In", "Google sign in failed", e);
                        }
                    } else {
                        Toast.makeText(SigninActivity.this, "Google sign in was cancelled.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Set the Google icon and click listener if the ImageView is found
        if (googleIconSignin != null) {
            googleIconSignin.setImageResource(R.drawable.google);
            googleIconSignin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signInWithGoogle();
                }
            });
        } else {
            Log.e("SigninActivity", "Google icon ImageView (signin) is null");
        }

        btnSigninAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailSignin.getText().toString().trim();
                String password = etPasswordSignin.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SigninActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SigninActivity.this, task -> {
                            if (task.isSuccessful()) {

                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    Toast.makeText(SigninActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SigninActivity.this, MainActivity2.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SigninActivity.this, "Error: Could not get current user.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(SigninActivity.this, "Sign In failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        tvSignupTabOnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter your email to receive the reset link.");

                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString();
                        if (!mail.isEmpty()) {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(mail)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(SigninActivity.this, "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SigninActivity.this, "Error! Reset link not sent: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(SigninActivity.this, "Please enter an email address.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", null);

                passwordResetDialog.create().show();
            }
        });

    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String email = user.getEmail();
                                // For Google Sign-in, we don't have a separate password in the traditional sense.
                                // You might store some identifier or simply rely on the Firebase Auth token.
                                // For this example, we'll just store the email.
                                mDatabase.child("users").child(user.getUid()).child("email").setValue(email);
                                Toast.makeText(SigninActivity.this, "Google Sign In Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SigninActivity.this, DashBoardActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SigninActivity.this, "Error: Could not get current user after Google Sign In.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SigninActivity.this, "Google sign in failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // You might want to handle a previously signed-in Google user here,
        // for example, by directly navigating to the Dashboard.
        // GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // if (account != null && currentUser != null) {
        //     Intent intent = new Intent(SigninActivity.this, DashBoardActivity.class);
        //     startActivity(intent);
        //     finish();
        // }
    }
}