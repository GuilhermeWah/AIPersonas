package com.example.aipersonas.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.aipersonas.models.Chat;
import com.example.aipersonas.repositories.ChatRepository;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private ChatRepository repository;
    private LiveData<List<Chat>> allChats;


    public ChatViewModel(@NonNull Application application, String personaId) {
        super(application);
        repository = new ChatRepository(application, personaId);
        allChats = repository.getChatsForPersona(personaId);

    }

    public void insert(Chat chat, String personaId) {
        repository.insert(chat, personaId);
    }

    public void update(Chat chat, String personaId) {

        repository.update(chat, personaId);
    }

    public void delete(Chat chat, String personaId) {

        repository.delete(chat, personaId);
    }

    public LiveData<List<Chat>> getAllChats() {
        return allChats;
    }

    public LiveData<List<Chat>> getChatsForPersona(String personaId) {
        return repository.getChatsForPersona(personaId);
    }
}
