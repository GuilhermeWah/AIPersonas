package com.example.aipersonas.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.databases.UserDAO;
import com.example.aipersonas.models.User;
import com.example.aipersonas.utils.NetworkUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public interface DeleteUserCallback {
        void onSuccess();
        void onFailure(Exception e);
    }


    public LiveData<User> getUserById() {
        return userDAO.getUserById(userId);
    }

    public void fetchUserFromFirestore() {
        firestore.collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);

                        // Explicitly set the userId in case it's not automatically mapped
                        if (user != null) {
                            user.setUserId(userId);
                        } else {
                            user = new User(userId, "Default First Name", "Default Last Name", "default@example.com", "default_avatar_url");
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
                        e.printStackTrace(); // Log error
                    });
        });
    }

    // Delete the whole collection + subcollections; --> it will disappear from the firestore.
    // By doing it, our subcollections wont be empty. They will be gone too.
    // It was bugging too! Lots of logs, needed.


    public void deleteUser(DeleteUserCallback callback) {
        // Step 1: Fetch the user document to confirm existence
        firestore.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Step 2: Delete subcollections (Personas, Chats, and Messages)
                        deleteSubcollections(userId, () -> {
                            // Step 3: Delete the main user document after deleting subcollections
                            firestore.collection("Users").document(userId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Step 4: Delete local Room data only after Firestore deletion is successful
                                        executor.execute(() -> {
                                            userDAO.deleteById(userId);
                                            if (callback != null) {
                                                callback.onSuccess();
                                            }
                                        });
                                        Log.d("UserRepository", "User and all related data deleted successfully from Firestore and Room");
                                    })
                                    .addOnFailureListener(e -> {
                                        if (callback != null) {
                                            callback.onFailure(e);
                                        }
                                        Log.e("UserRepository", "Failed to delete user document from Firestore", e);
                                    });
                        }, e -> {
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                            Log.e("UserRepository", "Failed to delete subcollections from Firestore", e);
                        });
                    } else {
                        if (callback != null) {
                            callback.onFailure(new Exception("User document does not exist in Firestore."));
                        }
                        Log.e("UserRepository", "User document does not exist in Firestore.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                    Log.e("UserRepository", "Failed to fetch user document from Firestore.", e);
                });
    }

    /**
     * Deletes all subcollections (Personas, Chats, and Messages) of a user.
     *
     * @param userId    The user ID whose subcollections are to be deleted.
     * @param onSuccess Callback to be executed when deletion is successful.
     * @param onFailure Callback to be executed when deletion fails.
     */
    private void deleteSubcollections(String userId, Runnable onSuccess, OnFailureListener onFailure) {
        // Delete Personas and their subcollections
        firestore.collection("Users").document(userId).collection("Personas")
                .get()
                .addOnSuccessListener(personaSnapshots -> {
                    for (DocumentSnapshot persona : personaSnapshots) {
                        String personaId = persona.getId();

                        // Delete Chats and Messages under each Persona
                        firestore.collection("Users").document(userId)
                                .collection("Personas").document(personaId)
                                .collection("Chats").get()
                                .addOnSuccessListener(chatSnapshots -> {
                                    for (DocumentSnapshot chat : chatSnapshots) {
                                        String chatId = chat.getId();

                                        // Delete Messages under each Chat
                                        firestore.collection("Users").document(userId)
                                                .collection("Personas").document(personaId)
                                                .collection("Chats").document(chatId)
                                                .collection("Messages").get()
                                                .addOnSuccessListener(messageSnapshots -> {
                                                    for (DocumentSnapshot message : messageSnapshots) {
                                                        message.getReference().delete();
                                                    }
                                                    // After deleting Messages, delete the Chat itself
                                                    chat.getReference().delete();
                                                })
                                                .addOnFailureListener(onFailure);
                                    }
                                    // After deleting Chats, delete the Persona itself
                                    persona.getReference().delete();
                                })
                                .addOnFailureListener(onFailure);
                    }
                    // After deleting all Personas, call the success callback
                    onSuccess.run();
                })
                .addOnFailureListener(onFailure);
    }


}
