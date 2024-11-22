package com.example.aipersonas.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.example.aipersonas.repositories.ChatRepository;
import com.example.aipersonas.repositories.GPTRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private static final String TAG = "ChatViewModel";
    private final ChatRepository chatRepository;
    private final GPTRepository gptRepository;
    private final LiveData<List<Chat>> chatsForCurrentChat;
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final String personaId;
    private final String chatId;

    public ChatViewModel(@NonNull Application application, String personaId, String chatId) {
        super(application);

        this.chatRepository = new ChatRepository(application, personaId);
        this.gptRepository = new GPTRepository(application); // Initialize GPTRepository
        this.chatId = chatId;
        this.personaId = personaId;

        // Initialize LiveData objects
        chatsForCurrentChat = chatRepository.getChatsForPersona(personaId);
    }

    public LiveData<List<Chat>> getChatsForCurrentChat() {
        return chatsForCurrentChat;
    }

    public LiveData<List<Message>> getMessagesForChat(String chatId) {
        return chatRepository.getMessagesForChat(chatId); // Ensure this method exists in the repository
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void sendMessage(String message, String personaId, String chatId) {
        Log.d(TAG, "[CVM] sendMessage called with message: " + message);

        // Use GPTRepository to handle GPT API interaction
        gptRepository.sendGPTRequest(message, 150, 0.7f, new GPTRepository.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "GPT Response: " + response);
                Chat responseChat = new Chat(
                        personaId,
                        chatId,
                        "GPT",
                        response,
                        Timestamp.now()
                );
                chatRepository.insert(responseChat, personaId);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to get GPT response: " + error);
                errorLiveData.postValue(error);
            }
        });
    }


    public void insert(Chat chat, String personaId) {
        chatRepository.insert(chat, personaId);
    }

    public void update(Chat chat, String personaId) {
        chatRepository.update(chat, personaId);
    }

    public void delete(Chat chat, String personaId) {
        chatRepository.delete(chat, personaId);
    }

    public String getUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public String getGptApiKey() {
        return gptRepository.getGptKey();
    }
}
