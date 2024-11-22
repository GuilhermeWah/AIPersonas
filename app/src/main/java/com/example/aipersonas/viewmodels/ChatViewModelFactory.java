package com.example.aipersonas.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.aipersonas.repositories.ChatRepository;

public class ChatViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final String personaId;
    private final String chatId;

    public ChatViewModelFactory(Application application, String personaId, String chatId) {
        this.application = application;
        this.personaId = personaId;
        this.chatId = chatId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatViewModel.class)) {
            return (T) new ChatViewModel(application, personaId, chatId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}

