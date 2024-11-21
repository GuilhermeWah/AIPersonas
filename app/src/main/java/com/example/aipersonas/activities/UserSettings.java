package com.example.aipersonas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aipersonas.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserSettings extends AppCompatActivity {

    private TextView userName, statusText;
    private ImageView profilePicture;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        userName = findViewById(R.id.userName);
        statusText = findViewById(R.id.statusText);
        profilePicture = findViewById(R.id.profilePicture);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadUserDetails();

        findViewById(R.id.updateProfileNameButton).setOnClickListener(v -> {

            updateProfileName();
        });

        findViewById(R.id.updateStatusButton).setOnClickListener(v -> {

            updateStatus();
        });

        findViewById(R.id.logOutButton).setOnClickListener(v -> {

            logOut();
        });

        findViewById(R.id.deleteAccountButton).setOnClickListener(v -> {

            deleteAccount();
        });
    }

    private void loadUserDetails() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String fullName = firstName + " " + lastName;
                        userName.setText(fullName);
                      //  statusText.setText("Status: " + documentSnapshot.getString("status"));

                    } else {
                        Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading user details", Toast.LENGTH_SHORT).show());
    }

    private void updateProfileName() {
        //@TODO: Implement the functionalities; we have to match our architecture;
        //don't want to fetch all that data in this activity.
        Toast.makeText(this, "Update profile name clicked", Toast.LENGTH_SHORT).show();
    }

    private void updateStatus() {

        Toast.makeText(this, "Update status clicked", Toast.LENGTH_SHORT).show();
    }

    private void logOut() {
        auth.signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, SignInActivity.class);
    }

    //@TODO: Delegate this to the repository, maybe UserRepo;
    private void deleteAccount() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("Users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show());
    }
}
