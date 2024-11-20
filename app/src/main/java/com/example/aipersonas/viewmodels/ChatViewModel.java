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
import com.example.aipersonas.repositories.APIConfigRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private static final String TAG = "ChatViewModel";
    private final ChatRepository chatRepository;
    private final LiveData<List<Chat>> chatsForCurrentChat;
    private final LiveData<String> gptKeyLiveData;
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final String personaId;
    private final String chatId;

    public ChatViewModel(@NonNull Application application, String personaId, String chatId) {
        super(application);

        this.chatRepository = new ChatRepository(application, personaId);
        this.chatId = chatId;
        this.personaId = personaId;
        /*
          it fetches the Chats Sub-Collection under the specified
          PersonaID for the currently authenticated UserID.
         */
        chatsForCurrentChat = chatRepository.getChatsForPersona(personaId);

        // Retrieve API keys from APIConfigRepository
        APIConfigRepository apiConfigRepository = new APIConfigRepository(application);
        gptKeyLiveData = apiConfigRepository.getGPTKeyLiveData();
    }

    public LiveData<List<Chat>> getChatsForCurrentChat() {
        return chatsForCurrentChat;
    }

    public LiveData<List<Message>> getMessagesForChat(String chatId) {
        return chatRepository.getMessagesForChat(chatId); // Ensure this method exists in the repository
    }

    public LiveData<String> getGPTKeyLiveData() {
        return gptKeyLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void sendMessage(String message, String personaId, String chatId) {
        gptKeyLiveData.observeForever(gptKey -> {
            if (gptKey != null) {
                chatRepository.sendMessageToGPT(message, personaId, chatId, gptKey, new ChatRepository.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(TAG, "GPT Response: " + response);

                        // Add GPT response as a new chat message
                        Chat responseChat = new Chat(
                                personaId,           // Persona ID
                                chatId,             // Chat ID
                                "GPT",             // Persona title
                                response,         // Message content
                                Timestamp.now()  // Timestamp
                        );
                        chatRepository.insert(responseChat, personaId);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to get GPT response: " + error);
                        errorLiveData.postValue(error);
                    }
                });
            } else {
                Log.e(TAG, "GPT key is null");
                errorLiveData.postValue("GPT key is null");
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

}
