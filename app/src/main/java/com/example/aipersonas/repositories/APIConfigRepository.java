package com.example.aipersonas.repositories;

import android.content.Context;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class APIConfigRepository {

    private static final String TAG = "APIConfigRepository";
    private static final String COLLECTION_NAME = "sensitive_settings";
    private static final String DOCUMENT_NAME = "apiKeys";
    private static final String GPT_KEY_FIELD = "openai_api_key";

    private final FirebaseFirestore firestore;
    private EncryptedSharedPreferences encryptedPreferences;
    private final String gptKey;

    public APIConfigRepository(Context context) {
        firestore = FirebaseFirestore.getInstance();
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            encryptedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "secure_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (IOException | GeneralSecurityException e) {
            Log.e(TAG, "Error initializing encrypted preferences", e);
            gptKey = null;
            return;
        }
        gptKey = fetchGPTKey();
    }

    /**
     * Retrieves the GPT API key from local cache or Firestore.
     *
     * @return GPT API key as a String.
     */
    private String fetchGPTKey() {
        String cachedKey = getCachedKey(GPT_KEY_FIELD);
        if (cachedKey != null) {
            return cachedKey;
        } else {
            fetchGPTKeyFromFirestore();
            return null; // Return null initially, Firestore fetch is async
        }
    }

    /**
     * Fetch the GPT API key from Firestore.
     */
    private void fetchGPTKeyFromFirestore() {
        Log.d(TAG, "Attempting to fetch GPT Key from Firestore at path: " + COLLECTION_NAME + "/" + DOCUMENT_NAME);

        firestore.collection(COLLECTION_NAME)
                .document(DOCUMENT_NAME)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String gptKey = documentSnapshot.getString(GPT_KEY_FIELD);
                        if (gptKey != null) {
                            cacheApiKey(GPT_KEY_FIELD, gptKey);
                            Log.d(TAG, "GPT API Key successfully fetched: " + gptKey);
                        } else {
                            Log.e(TAG, "GPT API key not found in Firestore document");
                        }
                    } else {
                        Log.e(TAG, "Firestore document not found at: " + COLLECTION_NAME + "/" + DOCUMENT_NAME);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch GPT API key from Firestore", e);
                });
    }

    /**
     * Cache the GPT API key locally using EncryptedSharedPreferences.
     */
    private void cacheApiKey(String key, String value) {
        if (encryptedPreferences != null) {
            encryptedPreferences.edit().putString(key, value).apply();
        }
    }

    /**
     * Retrieve a cached API key from EncryptedSharedPreferences.
     */
    private String getCachedKey(String key) {
        if (encryptedPreferences != null) {
            return encryptedPreferences.getString(key, null);
        }
        return null;
    }

    /**
     * Get the GPT API key.
     *
     * @return GPT API key as a String.
     */
    public String getGPTKey() {
        return gptKey;
    }
}
