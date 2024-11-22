package com.example.aipersonas.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aipersonas.models.Chat;
import com.example.aipersonas.repositories.ChatRepository;
import com.example.aipersonas.repositories.APIConfigRepository;

import java.util.List;

public class ChatListViewModel extends AndroidViewModel {

    private static final String TAG = "ChatViewModel";
    private final ChatRepository repository;
    private final LiveData<List<Chat>> allChats;
    private final String gptApiKey;
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();


    public ChatListViewModel(@NonNull Application application, String personaId) {
        super(application);
        repository = new ChatRepository(application, personaId);
        allChats = repository.getChatsForPersona(personaId);

        // Retrieve API keys from APIConfigRepository
        APIConfigRepository apiConfigRepository;
        apiConfigRepository = new APIConfigRepository(application);
        gptApiKey = apiConfigRepository.getGPTKey();
    }

    // Chat CRUD operations
    public void insert(Chat chat, String personaId) {
        repository.insert(chat, personaId);
    }

    public void update(Chat chat, String personaId) {
        repository.update(chat, personaId);
    }

    public void delete(Chat chat, String personaId) {
        repository.delete(chat, personaId);
    }

    public LiveData<List<Chat>> getChatsForPersona(String personaId) {
        return repository.getChatsForPersona(personaId);
    }

    public String getGPTKey() {
        return gptApiKey;
    }

    // Handle sending a message to GPT
    public void sendMessageToGPT(String message, String personaId, String chatId) {

            if (gptApiKey != null) {
                // Call the repository method
                repository.sendMessageToGPT(message, personaId, chatId, gptApiKey, new ChatRepository.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        // Insert GPT's response into the chat repository
                        repository.insert(new Chat(chatId, personaId, response), personaId);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to get response from GPT: " + error);
                        errorLiveData.postValue(error);
                    }
                });
            } else {
                Log.e(TAG, "GPT key is null");
                errorLiveData.postValue("GPT key is null");
            }
    }

}
