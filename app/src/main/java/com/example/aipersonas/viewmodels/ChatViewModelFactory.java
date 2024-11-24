package com.example.aipersonas.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.aipersonas.repositories.ChatRepository;
import com.example.aipersonas.repositories.GPTRepository;

public class ChatViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final String personaId;
    private final String chatId;
    private final ChatRepository chatRepository;

    public ChatViewModelFactory(Application application, ChatRepository chatRepository, String personaId, String chatId) {
        this.application = application;
        this.personaId = personaId;
        this.chatId = chatId;
        this.chatRepository = chatRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatViewModel.class)) {
            GPTRepository gptRepository = new GPTRepository(application); // Create GPTRepository instance
            return (T) new ChatViewModel(application, chatRepository, gptRepository, personaId, chatId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
