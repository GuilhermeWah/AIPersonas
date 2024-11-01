package com.example.aipersonas.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDAO;
import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.models.Chat;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository {

    private ChatDAO chatDAO;
    private LiveData<List<Chat>> allChats;
    private ExecutorService executorService;
    private String userId;

    public ChatRepository(Application application) {
        ChatDatabase database = ChatDatabase.getInstance(application);

        // Initialize Firebase Authentication and get the current user's ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatDAO = database.chatDao();
        allChats = chatDAO.getAllChats(userId);
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<Chat>> getAllChats() {
        return allChats;
    }

    public void insert(Chat chat) {
        executorService.execute(() -> chatDAO.insert(chat));
    }

    public void update(Chat chat) {
        executorService.execute(() -> chatDAO.update(chat));
    }

    public void delete(Chat chat) {
        executorService.execute(() -> chatDAO.delete(chat));
    }

}
