package com.example.aipersonas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.aipersonas.R;
import com.example.aipersonas.viewmodels.UserViewModel;
import com.example.aipersonas.repositories.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class UserSettings extends AppCompatActivity {

    private TextView userName, statusText;
    private ImageView profilePicture;
    private FirebaseAuth auth;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userName = findViewById(R.id.userName);
        profilePicture = findViewById(R.id.profilePicture);

        auth = FirebaseAuth.getInstance();
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        loadUserDetails();

        findViewById(R.id.updateProfileNameButton).setOnClickListener(v -> {
            updateProfileName();
        });

        findViewById(R.id.deleteAccountButton).setOnClickListener(v -> {
            deleteAccount();
        });

        setupBottomNavigation();
    }

    private void loadUserDetails() {
        // Use LiveData to observe user data
        userViewModel.getUser().observe(this, user -> {
            if (user != null) {
                String fullName = user.getFirstName() + " " + user.getLastName();
                userName.setText(fullName);
            } else {
                Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileName() {
        //@TODO: Yet to be implemented, we gonna use the approach she taught us.
        //We ran out of time, but it will be implement. @Aryan will be working on it.

        Toast.makeText(this, "Yet to come", Toast.LENGTH_SHORT).show();
    }

    private void logOut() {
        auth.signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void deleteAccount() {
        // Use UserViewModel to delete user account
        userViewModel.deleteUser(new UserRepository.DeleteUserCallback() {
            @Override
            public void onSuccess() {
                // Step 2: Delete Firebase Authentication record
                deleteFirebaseAuthUser();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserSettings.this, "Failed to delete Firestore data", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    // Helper method to delete the Firebase Authentication account
    private void deleteFirebaseAuthUser() {
        if (auth.getCurrentUser() != null) {
            auth.getCurrentUser().delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        redirectToSignIn();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete authentication record. Please try again.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }
    }

    // Redirect to sign-in activity after deletion
    private void redirectToSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Setup bottom navigation menu
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "My Account":
                    if (!isCurrentActivity(MainActivity.class)) {
            //@TODO: Implement token usage from GPTApi, instead of using the amount using our rates;
            // Instead of using the $$ we have on the API, we can adjust accordingly to our rates.
            // Our margins and everything. Just so user knows how much they still have left on credits.
            //startActivity(new Intent(UserSettings.this, UserSettings.class));
                        Toast.makeText(UserSettings.this, "Yet to come (billing, etc..)", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case "Home":
                    if (!isCurrentActivity(MainActivity.class)) {
                        startActivity(new Intent(UserSettings.this, MainActivity.class));
                    }
                    return true;

                case "Log Out":
                    logOut();
                    return true;

                default:
                    return false;
            }
        });
    }

    // Check if the current activity is the given class
    private boolean isCurrentActivity(Class<?> activityClass) {
        return getClass().equals(activityClass);
    }

    /**
     *  Documentation of Changes:  Nov 20th, 2024
     *                *  #### We just made sure everything is aligned with our architecture.
     *                *  ### Before  we were making calls to firestore/room, inside here.
     *                *  ### The adjustments done are:
     *                checkpoint: v0.1 : https://github.com/GuilhermeWah/AIPersonas/tree/feature/GPTApiImplementationv0.1

     *    - The `UserSettings` activity now uses `UserViewModel` to interact with user data instead of directly calling Firebase services.
     *    - This ensures that all operations related to user management are delegated to the appropriate layer (ViewModel), maintaining a separation of concerns.
     *    - The `loadUserDetails()` method now observes the `LiveData` from the `UserViewModel` to get user details.
     *    - This approach automatically updates the UI when user data changes, providing a more dynamic and responsive experience.
     *    - The `deleteAccount()` method now uses the `UserViewModel.deleteUser()` method with a callback (`UserRepository.DeleteUserCallback`) to ensure that deletion occurs in sequence: Firestore first, followed by Room.
     *    - This ensures consistency, avoiding any orphaned data
     *    - Previously, `UserSettings` interacted directly with Firebase for user operations, which tightly coupled the activity with the backend service.
     *    - Now, all user-related operations are handled through the `UserViewModel` and `UserRepository`, aligning with the MVVM architecture.
     *    - This results in a cleaner separation between UI logic and data management, making the code easier to maintain and extend.
     *    - Moving logic into `UserViewModel` and `UserRepository` means that any changes to how user data is fetched, updated, or deleted can be made in the repository or ViewModel without modifying the activity.
     *    - This makes the app more scalable and simplifies future feature development.
     */
}
