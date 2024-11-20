package com.example.aipersonas.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.aipersonas.repositories.APIConfigRepository;


import java.util.Map;


/**
*
*   I just thought about it ..  There's no need of storing the
 *  the Firebase  Auth  key in the firestore. They are not server API Keys
 *  they are designed to be exposed in client-side apps. Without proper authentication
 *  no one can read/write to the firestore. That's why I've been setting the rules on the
 *  firebase console lol*
*
* */
public class APISettingsViewModel extends ViewModel   {

    private final APIConfigRepository repository;
    private final MutableLiveData<String> gptKeyLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public APISettingsViewModel(APIConfigRepository repository) {
        this.repository = repository;
    }

    public LiveData<String> getApiKeysLiveData() {
        return gptKeyLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void fetchApiKeys() {
        repository.fetchGPTKey();
        repository.getGPTKeyLiveData().observeForever(apiKey -> {
            if (apiKey != null) {
                gptKeyLiveData.postValue(apiKey); // Assuming apiKeysLiveData is now a MutableLiveData<String>
            }
        });
        repository.getErrorLiveData().observeForever(error -> {
            if (error != null) {
                errorLiveData.postValue(error);
            }
        });
    }

}
