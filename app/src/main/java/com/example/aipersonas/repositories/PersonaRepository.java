package com.example.aipersonas.repositories;

import com.example.aipersonas.utils.NetworkUtils;
import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.databases.PersonaDAO;
import com.example.aipersonas.models.Persona;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersonaRepository {

    private PersonaDAO personaDAO;
    private LiveData<List<Persona>> allPersonas;
    private FirebaseFirestore firebaseFirestore;
    private String userId;

    // Executor for async database tasks
    private ExecutorService executor;

    // Constructor
    public PersonaRepository(Application application) {
        // Initialize Room database
        ChatDatabase database = ChatDatabase.getInstance(application);
        personaDAO = database.personaDao();
        allPersonas = personaDAO.getAllPersonas();

        // Initialize Firebase Firestore
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Get the authenticated user ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize executor
        executor = Executors.newSingleThreadExecutor();

        // Fetch data from Firestore if online
        if (NetworkUtils.isNetworkAvailable(application.getApplicationContext())) {
            fetchPersonasFromFirestore();
        }
    }

    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // this method returns all the personas from the roomDB
    public LiveData<List<Persona>> getAllPersonas() {
        return allPersonas; // Data is sourced from Room, which acts as the local cache
    }

    // Method to get a specific persona by ID
    public LiveData<Persona> getPersonaById(String personaId) {
        // Now use the initialized personaDAO instance
        return personaDAO.getPersonaById(personaId);
    }

// Fetch personas from Firestore and cache them in Room
private void fetchPersonasFromFirestore() {

    firebaseFirestore.collection("Users")
            .document(userId)
            .collection("Personas")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                executor.execute(() -> {
                    // Clear old data to avoid duplication
                    personaDAO.deleteAll();

                    // data we fetched is inserted on room; sync online and offline
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Persona persona = document.toObject(Persona.class);
                        personaDAO.insert(persona);
                    }
                });
            })
            .addOnFailureListener(e -> {
                // Log error if data fetching fails
                e.printStackTrace();
            });
}



    // Method to insert a Persona
    public void insert(Persona persona) {
        // Insert to Room Database asynchronously
        executor.execute(() -> {
            personaDAO.insert(persona);
            // Insert to Firestore
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(String.valueOf(persona.getPersonaId()))
                    .set(persona)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully added to Firestore
                    })
                    .addOnFailureListener(e -> {
                        // Log error
                        e.printStackTrace();
                    });
        });
    }

    // Method to update a Persona
    public void update(Persona persona) {
        // Update Room Database asynchronously
        executor.execute(() -> {
            personaDAO.update(persona);
            // Update Firestore
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(String.valueOf(persona.getPersonaId()))
                    .set(persona)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully updated in Firestore
                    })
                    .addOnFailureListener(e -> {
                        // Log error
                        e.printStackTrace();
                    });
        });
    }

    // Method to delete a Persona
    public void delete(Persona persona, FirestoreCallback callback) {
        // Delete from Room Database asynchronously
        executor.execute(() -> {
            personaDAO.delete(persona);
            // Delete from Firestore
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(String.valueOf(persona.getPersonaId()))
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    });
        });
    }
}
