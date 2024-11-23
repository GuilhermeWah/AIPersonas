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

public class ChatViewModel extends AndroidViewModel {

    private static final String TAG = "ChatViewModel";
    private final ChatRepository chatRepository;
    private final GPTRepository gptRepository;
    private final LiveData<List<Message>> messagesForChat;
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
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

        // Initialize LiveData for messages related to the current chat
        messagesForChat = chatRepository.getMessagesForChat(chatId);
    }

    public LiveData<List<Message>> getMessagesForChat() {
        return messagesForChat;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void sendMessage(String message) {
        Log.d(TAG, "[CVM] sendMessage called with message: " + message);

        // Create and insert the user message into Firestore and Room
        Message userMessage = new Message(
                chatId,
                getUserId(),
                message,
                Timestamp.now(),
                "sent"
        );
        chatRepository.insertMessage(userMessage);

        // Set "typing" status while waiting for GPT response
        setMessageStatus("typing");

        // Use GPTRepository to handle GPT API interaction
        gptRepository.sendGPTRequest(message, 150, 0.7f, new GPTRepository.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "GPT Response: " + response);
                Message responseMessage = new Message(
                        chatId,
                        "GPT",
                        response,
                        Timestamp.now(),
                        "received"
                );
                chatRepository.insertMessage(responseMessage);
                setMessageStatus("idle");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to get GPT response: " + error);
                errorLiveData.postValue(error);
                setMessageStatus("idle");
            }
        });
    }

    private void setMessageStatus(String status) {
        // Logic for setting the "typing" or "idle" status in Firestore.
        chatRepository.updateMessageStatus(chatId, status);
    }

    public void insertMessage(Message message) {
        chatRepository.insertMessage(message);
    }

    public void deleteMessage(Message message) {
        // Update to delete the message by message ID (since deleteMessage expects a String parameter)
        chatRepository.deleteMessage(message);
    }

    public String getUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public String getGptApiKey() {
        return gptRepository.getGptKey();
    }
}
