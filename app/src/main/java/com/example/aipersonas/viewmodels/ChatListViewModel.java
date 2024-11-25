package com.example.aipersonas.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aipersonas.databases.ChatDatabase;
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

        // Get instance of ChatDatabase and pass ChatDAO to the repository
        ChatDatabase db = ChatDatabase.getInstance(application);
        repository = new ChatRepository(db.chatDao());

        // Assuming this method requires a personaId argument, provide it as needed
        allChats = repository.getChatsForPersona(personaId);

        // Retrieve API keys from APIConfigRepository
        APIConfigRepository apiConfigRepository = new APIConfigRepository(application);
        gptApiKey = apiConfigRepository.getGPTKey();
    }

    // Chat CRUD operations
    public void insert(Chat chat) {
        repository.insertOrUpdateChat(chat);
    }

    public void update(Chat chat) {
        repository.updateChat(chat);
    }

    public void delete(Chat chat) {
        repository.deleteChat(chat);
    }

    public LiveData<List<Chat>> getChatsForPersona(String personaId) {
        return repository.getChatsForPersona(personaId);
    }

    public String getGPTKey() {
        return gptApiKey;
    }


}
