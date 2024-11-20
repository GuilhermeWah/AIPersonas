package com.example.aipersonas.repositories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDAO;
import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChatRepository {

    private ChatDAO chatDAO;
    private LiveData<List<Chat>> allChats;
    private LiveData<List<Chat>> chatsForPersona;
    private ExecutorService executorService;
    private String userId, personaId;
    private FirebaseFirestore firebaseFirestore;


    public ChatRepository(Application application, String personaId) {
        ChatDatabase database = ChatDatabase.getInstance(application);

        // Initialize Firebase Authentication and get the current user's ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatDAO = database.chatDao();

        allChats = chatDAO.getAllChats(userId);
        executorService = Executors.newFixedThreadPool(2);
        firebaseFirestore = FirebaseFirestore.getInstance(); //initialize firestore
        this.personaId = personaId;

        /*
        * this method is necessary because what if user is logged in on another device?
        * The changes made there will be stored on firestore, but not on room.
        */

        // Fetch data from Firestore if online
        if (NetworkUtils.isNetworkAvailable(application.getApplicationContext())) {
            fetchChatsFromFirestore(personaId);
        }
    }

    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public LiveData<List<Chat>> getAllChats() {
        return allChats;
    }

    public LiveData<List<Chat>> getChatsForPersona(String personaId) {
        return chatDAO.getChatsForPersona(personaId);
    }

    public void insert(Chat chat, String personaId) {
        executorService.execute(() -> {

            // Insert into Room
            executorService.execute(() -> {
                Chat existingChat = chatDAO.getChatById(chat.getChatId());
                if (existingChat == null) {
                    chatDAO.insert(chat);
                } else {
                    chatDAO.update(chat);
                }
            });

            // Insert into Firestore with personaId
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(personaId)
                    .collection("Chats")
                    .document(String.valueOf(chat.getChatId()))
                    .set(chat)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ChatRepository", "Chat added to Firestore");// Successfully added to Firestore
                    })
                    .addOnFailureListener(e -> {
                        // Log error
                        Log.e("ChatRepository", "Failed to add chat to Firestore: " + e.getMessage());
                        e.printStackTrace();
                    });
        });
    }


    public void update(Chat chat, String personaId) {
        executorService.execute(() -> {
            // Update Room Database
            chatDAO.update(chat);

            // Update Firestore
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(personaId)
                    .collection("Chats")
                    .document(String.valueOf(chat.getChatId()))
                    .set(chat)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully updated in Firestore
                    })
                    .addOnFailureListener(e -> {
                        // Log error
                        e.printStackTrace();
                    });
        });
    }

    public void delete(Chat chat, String personaId) {
        executorService.execute(() -> {
            // Delete from Room Database
            chatDAO.delete(chat);

            // Delete from Firestore
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(personaId)
                    .collection("Chats")
                    .document(String.valueOf(chat.getChatId()))
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Successfully deleted from Firestore
                    })
                    .addOnFailureListener(e -> {
                        // Log error
                        e.printStackTrace();
                    });
        });
    }

    // Fetch chats from Firestore and store them in Room to keep data synchronized
    private void fetchChatsFromFirestore(String personaId) {
        firebaseFirestore.collection("Users")
                .document(userId)
                .collection("Personas")
                .document(personaId)
                .collection("Chats")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    executorService.execute(() -> {
                        // Clear old data to avoid duplication
                        chatDAO.deleteAllChatsForUser(userId);

                        // Insert fetched data into Room
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Chat chat = document.toObject(Chat.class);
                            chatDAO.insert(chat);
                            Log.d("ChatRepository", "Chat fetched from Firestore and inserted into Room");
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Log error if data fetching fails
                    Log.e("ChatRepository", "Failed to fetch chats from Firestore: " + e.getMessage());
                    e.printStackTrace();
                });
    }

    private void sendHttpRequest(String apiUrl, String apiKey, String requestBody, ApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                requestBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    callback.onSuccess(responseData);
                } else {
                    callback.onFailure("Failed with status code: " + response.code());
                }
            }
        });
    }

    private String buildGPTRequestBody(String message) {
        return "{"
                + "\"model\":\"text-davinci-003\","
                + "\"prompt\":\"" + message + "\","
                + "\"max_tokens\":100"
                + "}";
    }


    public void sendMessageToGPT(String message, String personaId, String chatId, String gptKey, ApiCallback callback) {
        String apiUrl = "https://api.openai.com/v1/completions";
        String requestBody = buildGPTRequestBody(message);

        sendHttpRequest(apiUrl, gptKey, requestBody, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess(response);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }
}

