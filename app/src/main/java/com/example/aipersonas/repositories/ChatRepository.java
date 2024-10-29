package com.example.aipersonas.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDAO;
import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.models.Chat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository {

    private ChatDAO chatDAO;
    private LiveData<List<Chat>> allChats;
    private ExecutorService executorService;

    public ChatRepository(Application application) {
        ChatDatabase database = ChatDatabase.getInstance(application);
        chatDAO = database.chatDao();
        allChats = chatDAO.getAllChats();
        executorService = Executors.newFixedThreadPool(2);
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

    public LiveData<List<Chat>> getAllChats() {
        return allChats;
    }
}
