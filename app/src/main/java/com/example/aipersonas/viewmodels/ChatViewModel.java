package com.example.aipersonas.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aipersonas.databases.ChatDAO;
import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.example.aipersonas.repositories.ChatRepository;
import com.example.aipersonas.repositories.GPTRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatViewModel extends AndroidViewModel {

    private static final String TAG = "ChatViewModel";
    private final ChatRepository chatRepository;
    private final GPTRepository gptRepository;
    private final LiveData<List<Message>> messagesForChat;
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final LiveData<List<Chat>> allChatsLiveData;
    private final ExecutorService executor;
    private final String chatId;

    public ChatViewModel(@NonNull Application application, String chatId) {
        super(application);

        // Get the instance of ChatDatabase and then get ChatDAO from it
        ChatDatabase chatDatabase = ChatDatabase.getInstance(application);
        ChatDAO chatDAO = chatDatabase.chatDao();

        // Pass the ChatDAO to the repository
        this.chatRepository = new ChatRepository(chatDAO);
        this.gptRepository = new GPTRepository(application);
        this.chatId = chatId;
        this.executor = Executors.newSingleThreadExecutor();

        // Initialize LiveData for messages related to the current chat
        messagesForChat = chatRepository.getMessagesForChat(chatId);
        allChatsLiveData = chatRepository.getAllChats();;

    }

    public void sendMessageToGPT(String messageContent, GPTRepository.ApiCallback callback) {
        gptRepository.sendGPTRequest(messageContent, 500, 0.7f, callback);
    }

    public void sendMessage(Message message) {
        executor.execute(() -> chatRepository.addMessage(message)); // Save to Room
        chatRepository.addMessage(message); // Save to Firestore
    }


    public LiveData<List<Chat>> getAllChats() {
        return getAllChats();
    }

    public void startListeningToChats(String personaId) {
        chatRepository.listenToChats(personaId);
    }

    public void stopListeningToChats() {
        chatRepository.stopListeningToChats();
    }

    public void startListeningToMessages(String personaId, String chatId) {
        chatRepository.listenToMessages(personaId, chatId);
    }



    public LiveData<List<Message>> getMessagesForChat() {
        return messagesForChat;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public String getUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public String getGptApiKey() {
        return gptRepository.getGptKey();
    }
}
