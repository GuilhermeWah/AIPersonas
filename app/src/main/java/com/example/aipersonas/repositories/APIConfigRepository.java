package com.example.aipersonas.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class APIConfigRepository {

    private static final String TAG = "APIConfigRepository";
    private static final String COLLECTION_NAME = "apiKeys";
    private static final String DOCUMENT_NAME = "sensitive_settings";
    private static final String GPT_KEY_FIELD = "openai_api_key";

    private final FirebaseFirestore firestore;
    private EncryptedSharedPreferences encryptedPreferences;

    // LiveData for GPT API Key
    private final MutableLiveData<String> gptKeyLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

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
            errorLiveData.postValue("Failed to initialize secure storage");
        }
    }

    /**
     * Retrieves the GPT API key & Any API keys from Firestore.
     * If cached locally, fetches from local storage. Otherwise, retrieves from Firestore.
     * It was a good idea to be honest ...  By caching it we don't have to make requests to the
     * cloud everytime we need to access it. For this reason we are using  the MasterKeys,
     * to encrypt it. Not sure yet if it's a safe choice. What if user access their device
     * data files, and get to know it ? What are the odds of it being decrypted? Would be such a
     * big problem having both api keys exposed. @TODO: ask jaspret
     */
    public void fetchGPTKey() {
        String cachedKey = getCachedKey(GPT_KEY_FIELD);
        if (cachedKey != null) {
            gptKeyLiveData.postValue(cachedKey);
        } else {
            fetchGPTKeyFromFirestore();
        }
    }

    /**
     * LiveData for observing the GPT API key.
     */
    public LiveData<String> getGPTKeyLiveData() {
        return gptKeyLiveData;
    }

    /**
     * LiveData for observing errors.
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    /**
     * Fetch the GPT API key from Firestore.
     */
    private void fetchGPTKeyFromFirestore() {
        firestore.collection(COLLECTION_NAME)
                .document(DOCUMENT_NAME)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String gptKey = documentSnapshot.getString(GPT_KEY_FIELD);
                        if (gptKey != null) {
                            cacheApiKey(GPT_KEY_FIELD, gptKey);
                            gptKeyLiveData.postValue(gptKey);
                        } else {
                            errorLiveData.postValue("GPT API key not found in Firestore");
                        }
                    } else {
                        errorLiveData.postValue("Firestore document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch GPT API key from Firestore", e);
                    errorLiveData.postValue("Failed to fetch GPT API key: " + e.getMessage());
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
}
