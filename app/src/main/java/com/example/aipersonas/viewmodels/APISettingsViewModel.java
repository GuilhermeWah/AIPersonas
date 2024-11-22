package com.example.aipersonas.viewmodels;

import androidx.lifecycle.ViewModel;
import com.example.aipersonas.repositories.APIConfigRepository;

/**
 * I just thought about it ..  There's no need of storing the
 * Firebase Auth key in the firestore. They are not server API Keys
 * they are designed to be exposed in client-side apps. Without proper authentication
 * no one can read/write to the firestore. That's why I've been setting the rules on the
 * firebase console lol
 */
public class APISettingsViewModel extends ViewModel {

    private final APIConfigRepository repository;
    private String gptKey;
    private String error;

    public APISettingsViewModel(APIConfigRepository repository) {
        this.repository = repository;
        fetchApiKeys(); // Fetch API keys during initialization
    }

    public String getGptKey() {
        return gptKey;
    }

    public String getError() {
        return error;
    }

    private void fetchApiKeys() {
        gptKey = repository.getGPTKey();
        if (gptKey == null) {
            error = "Failed to retrieve GPT API key";
        }
    }
}
