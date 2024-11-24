package com.example.aipersonas.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.example.aipersonas.repositories.ChatRepository;
import com.example.aipersonas.repositories.GPTRepository;
import com.example.aipersonas.repositories.GPTRepository.ApiCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatViewModel extends AndroidViewModel {
    private static final String TAG = "ChatViewModel";
    private final ChatRepository chatRepository;
    private final GPTRepository gptRepository;
    private final LiveData<List<Message>> messagesForChat;
    private final LiveData<List<Chat>> allChats;
    private final ExecutorService executor;
    private final String userId;
    private final String chatId;

    public ChatViewModel(@NonNull Application application, ChatRepository chatRepository, GPTRepository gptRepository, String personaId, String chatId) {
        super(application);
        this.chatRepository = chatRepository;
        this.gptRepository = gptRepository;
        this.chatId = chatId;
        this.executor = Executors.newCachedThreadPool();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.allChats = chatRepository.getAllChats();
        this.messagesForChat = chatRepository.getMessagesForChat(chatId);
    }

    public LiveData<List<Message>> getMessagesForChat() {
        return messagesForChat;
    }

    public LiveData<List<Chat>> getAllChats() {
        return allChats;
    }

    // Insert or update a chat through repository
    public void saveChat(Chat chat) {
        chatRepository.insertOrUpdateChat(chat);
    }

    // Add a new message to the chat
    public void sendMessage(Message message) {
        chatRepository.addMessage(message);
    }

    // Send a message to GPT and handle the response
    public void sendMessageToGPT(String prompt, ApiCallback callback) {
        gptRepository.sendGPTRequest(prompt, 100, 0.7f, callback);
    }

    // Summarize messages if needed
    public void requestSummarizationIfNeeded() {
        gptRepository.requestSummarizationIfNeeded(chatId, (LifecycleOwner) this);
    }

    // Update message status
    public void updateMessageStatus(String messageId, String status) {
        chatRepository.updateMessageStatus(messageId, status);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        chatRepository.stopListeningToChats();
    }
}
