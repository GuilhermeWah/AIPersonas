package com.example.aipersonas.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
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

import org.json.JSONException;
import org.json.JSONObject;

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
    //Making it MutableLiveData so we can update it from the ViewModel, and not from the UI
     private final MutableLiveData<List<Message>> messagesLiveData;



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

        // Initialize MutableLiveData
        this.messagesLiveData = new MutableLiveData<>();

        // Observe Room's LiveData from the repository and update the MutableLiveData
        chatRepository.getMessagesForChat(chatId).observeForever(messages -> {
            if (messages != null) {
                messagesLiveData.postValue(messages); // Update MutableLiveData
            }
        });

        // Link other LiveData from repository
        this.messagesForChat = chatRepository.getMessagesForChat(chatId);
        this.allChatsLiveData = chatRepository.getAllChats();
    }



    public void sendMessageToGPT(String messageContent, Message currentMessage) {
        // Call GPTRepository to send the request
        gptRepository.sendGPTRequest(messageContent, 500, 0.7f, new GPTRepository.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                // Handle GPT's response
                handleGPTResponse(response, currentMessage);
            }

            @Override
            public void onFailure(String error) {
                // Log the error and update the message status
                Log.e(TAG, "Error in GPT response: " + error);
                currentMessage.setStatus("failed");
                updateLiveDataWithFailedMessage(currentMessage);
            }
        });
    }

    // Method to update LiveData with the failed message status
    private void updateLiveDataWithFailedMessage(Message currentMessage) {
        List<Message> currentMessages = messagesLiveData.getValue();
        if (currentMessages != null) {
            for (Message message : currentMessages) {
                if (message.getMessageId().equals(currentMessage.getMessageId())) {
                    message.setStatus("failed");
                }
            }
            // Post the updated list back to LiveData
            ((MutableLiveData<List<Message>>) messagesLiveData).postValue(currentMessages);
        }
    }


    public void handleGPTResponse(String responseJson, Message currentMessage) {
        try {
            JSONObject responseObject = new JSONObject(responseJson);
            String gptContent = responseObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // Update the currentMessage with GPT's response
            currentMessage.setGptResponse(responseJson); // Store the full GPT response
            currentMessage.setMessageContent(gptContent); // Set the actual GPT message
            currentMessage.setStatus("received"); // Update the status
            currentMessage.setResponseTimestamp(Timestamp.now()); // Set the response timestamp

            // Save the updated message via ChatRepository
            chatRepository.addMessage(currentMessage); // Save to Room and Firestore

            // Update LiveData to refresh the UI
            List<Message> currentMessages = messagesLiveData.getValue();
            if (currentMessages != null) {
                for (Message message : currentMessages) {
                    if (message.getMessageId().equals(currentMessage.getMessageId())) {
                        message.setGptResponse(responseJson);
                        message.setMessageContent(gptContent);
                        message.setResponseTimestamp(Timestamp.now());
                    }
                }
                // Post the updated list back to LiveData
                messagesLiveData.postValue(currentMessages);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing GPT response", e);
        }
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

    // call summarization if needed (every 10 messages)
    public void requestSummarizationIfNeeded(String chatId, LifecycleOwner lifecycleOwner) {
        gptRepository.requestSummarizationIfNeeded(chatId, lifecycleOwner);
    }

    // public method to expose messagesLiveData to other classes
    public LiveData<List<Message>> getMessagesLiveData() {
        return messagesLiveData;
    }


    public ChatRepository getChatRepository() {
        return chatRepository;
    }

    public LiveData<List<Message>> getMessagesForChat() {
        return messagesForChat;
    }

    public void observeMessages(String chatId) {
        Log.d(TAG, "observeMessages called for chatId: " + chatId);
        chatRepository.getMessagesForChat(chatId).observeForever(messages -> {
            if (messages == null || messages.isEmpty()) {
                Log.d(TAG, "observeMessages: Messages size = 0");
            } else {
                Log.d(TAG, "observeMessages: Messages size = " + messages.size());
            }
        });
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
