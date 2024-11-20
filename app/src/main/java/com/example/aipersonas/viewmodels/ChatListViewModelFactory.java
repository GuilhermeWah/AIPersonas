package com.example.aipersonas.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ChatListViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final String personaId;

    public ChatListViewModelFactory(Application application, String personaId) {
        this.application = application;
        this.personaId = personaId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatListViewModel.class)) {
            return (T) new ChatListViewModel(application, personaId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
