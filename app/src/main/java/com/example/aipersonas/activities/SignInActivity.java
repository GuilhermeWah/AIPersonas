package com.example.aipersonas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aipersonas.activities.MainActivity;
import com.example.aipersonas.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private EditText etSignInEmail, etSignInPassword;
    private Button btnSignIn;
    private TextView tvSignUpLink;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // we are initializing the firebase authentication here
        auth = FirebaseAuth.getInstance();

        // we are initializing the UI elements here (the views)
        etSignInEmail = findViewById(R.id.etSignInEmail);
        etSignInPassword = findViewById(R.id.etSignInPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);

        // we are setting up the listeners here
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser();
            }
        });

        tvSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });
    }

    // method to sign in the user, we are our firebase object here (auth) to sign the user
    private void signInUser() {
        String email = etSignInEmail.getText().toString().trim();
        String password = etSignInPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                            // Redirect to main activity
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    // validating the when signing in the user
    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etSignInEmail.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etSignInPassword.setError("Password is required");
            return false;
        }
        return true;
    }
}
