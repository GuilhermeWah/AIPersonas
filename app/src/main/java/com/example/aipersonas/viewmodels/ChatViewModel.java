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

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository(application);
        allChats = repository.getAllChats();
    }

    public void insert(Chat chat) {
        repository.insert(chat);
    }

    public void update(Chat chat) {
        repository.update(chat);
    }

    public void delete(Chat chat) {
        repository.delete(chat);
    }

    public LiveData<List<Chat>> getAllChats() {
        return allChats;
    }
}
