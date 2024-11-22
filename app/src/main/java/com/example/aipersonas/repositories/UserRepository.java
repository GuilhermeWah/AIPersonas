package com.example.aipersonas.repositories;
import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.databases.UserDAO;
import com.example.aipersonas.models.User;
import com.example.aipersonas.utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
decided to create this repo, just because we are planning to extend the user-related
functionalities such as changing the avatar, updating the username, deleting accounts etc
--> this keep the usermanagement isolated and scalable as the app grows
 */
public class UserRepository {

    private UserDAO userDAO;
    private FirebaseFirestore firestore;
    private String userId;
    private ExecutorService executor;

    public UserRepository(Application application) {
        ChatDatabase database = ChatDatabase.getInstance(application);
        userDAO = database.userDao();
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        executor = Executors.newSingleThreadExecutor();

        // Sync user data from Firestore
        if (NetworkUtils.isNetworkAvailable(application.getApplicationContext())) {
            fetchUserFromFirestore();
        }
    }

    public LiveData<User> getUserById() {
        return userDAO.getUserById(userId);
    }

    public void fetchUserFromFirestore() {
        firestore.collection("Users")
                .document(userId) // Get document using the authenticated userId
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);

                        // Explicitly set the userId in case it's not automatically mapped --> don't think we gonn use it tho
                        if (user != null) {
                            user.setUserId(userId); // Ensure userId is set
                        } else {
                            user = new User(userId,"Default First Name", "Default Last Name", "default@example.com", "default_avatar_url");
                        }
                        final User finalUser = user;
                        // Insert or update the user in Room database
                        executor.execute(() -> userDAO.insert(finalUser));
                    } else {
                        Log.e("UserRepository", "User document does not exist in Firestore.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Failed to fetch user from Firestore.", e);
                });
    }


    public void updateUser(User user) {
        executor.execute(() -> {
            userDAO.insert(user); // Update local cache
            firestore.collection("Users")
                    .document(userId)
                    .set(user) // Update Firestore
                    .addOnFailureListener(e -> {
                        e.printStackTrace(); // Log errorg
                    });
        });
    }

    public void deleteUser() {
        executor.execute(() -> {
            userDAO.deleteById(userId); // Remove from Room
            firestore.collection("Users")
                    .document(userId)
                    .delete() // Remove from Firestore
                    .addOnFailureListener(e -> {
                        e.printStackTrace(); // Log errors
                    });
        });
    }
}