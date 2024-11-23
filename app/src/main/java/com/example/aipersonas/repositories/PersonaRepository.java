package com.example.aipersonas.repositories;

import static android.content.ContentValues.TAG;

import com.example.aipersonas.utils.NetworkUtils;
import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.databases.PersonaDAO;
import com.example.aipersonas.models.Persona;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersonaRepository {

    private PersonaDAO personaDAO;
    private LiveData<List<Persona>> allPersonas;
    private FirebaseFirestore firebaseFirestore;
    private GPTRepository gptRepository;
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

        // Initialize GPTRepository
        gptRepository = new GPTRepository(application);

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

    public Persona getPersonaByIdSync(String personaId) {
        return personaDAO.getPersonaByIdSync(personaId);
    }

// Fetch personas from Firestore and cache them in Room
private void fetchPersonasFromFirestore() {

    firebaseFirestore.collection("Users")
            .document(userId)
            .collection("Personas")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                executor.execute(() -> {
                    // Clear old data to avoid duplication --
                    // not sure if it is smart tho!  seems to overwhelm the db
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

    public void updatePersonaDescription(String personaId, String tailoredDescription, String description) {
        Log.d(TAG, "Updating persona description in Firestore and Room: " + tailoredDescription);

        // Update the descriptions in Firestore
        firebaseFirestore.collection("Users")
                .document(userId)
                .collection("Personas")
                .document(personaId)
                .update(
                        "personaDescription", description,
                        "gptDescription", tailoredDescription
                )
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Persona descriptions updated successfully in Firestore."))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating persona descriptions in Firestore: ", e));

        executor.execute(() -> {
            Persona persona = personaDAO.getPersonaByIdSync(personaId);
            if (persona != null) {
                persona.setPersonaDescription(description);
                persona.setGptDescription(tailoredDescription);
                personaDAO.update(persona);
                Log.d(TAG, "Persona descriptions updated successfully in Room.");
            } else {
                Log.e(TAG, "Persona not found in Room with ID: " + personaId);
            }
        });
    }



    public void tailorPersonaDescription(String personaId, String description, TailoringCallback callback) {
        // Parameters
        int maxTokens = 750;
        float temperature = 0.7F;

        // Call GPT Repository method
        gptRepository.sendGPTRequest(description, maxTokens, temperature, new GPTRepository.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        String tailoredDescription = message.getString("content");

                        Log.d(TAG, "GPT tailored description received: " + tailoredDescription);

                        // Store tailored description in Firestore and Room
                        updatePersonaDescription(personaId, tailoredDescription, description);
                        callback.onSuccess(tailoredDescription);
                    } else {
                        callback.onFailure("No choices found in GPT response.");
                    }
                } catch (JSONException e) {
                    callback.onFailure("Failed to parse GPT response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to tailor persona description: " + error);
                callback.onFailure(error);
            }
        });
    }



    public interface TailoringCallback {
        void onSuccess(String tailoredDescription);
        void onFailure(String error);
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
