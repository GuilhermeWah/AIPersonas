package com.example.aipersonas.repositories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDAO;
import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.example.aipersonas.utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public LiveData<List<Message>> getMessagesForChat(String chatId) {
        return chatDAO.getMessagesForChat(chatId); // Ensure this DAO method exists
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

    public void fetchMessagesFromFirestore(String chatId) {
        firebaseFirestore.collection("Users")
                .document(userId)
                .collection("Personas")
                .document(personaId)
                .collection("Chats")
                .document(chatId)
                .collection("Messages")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    executorService.execute(() -> {
                        // Insert messages into Room
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Message message = document.toObject(Message.class);
                            chatDAO.insertMessage(message);
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e("ChatRepository", "Failed to fetch messages: " + e.getMessage()));
    }


    private void sendHttpRequest(String apiUrl, String apiKey, String requestBody, ApiCallback callback) {
        Log.d("ChatRepository", "Sending HTTP request to URL: " + apiUrl);
        Log.d("ChatRepository", "Request Body: " + requestBody);
        Log.d("ChatRepository", "Authorization Header: Bearer " + apiKey);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ChatRepository", "HTTP request failed: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "Unknown error";
                if (response.isSuccessful()) {
                    Log.d("ChatRepository", "HTTP request successful, response: " + responseBody);
                    callback.onSuccess(responseBody);
                } else {
                    Log.e("ChatRepository", "HTTP request failed with code: " + response.code() + ", message: " + response.message() + ", response body: " + responseBody);
                    callback.onFailure("HTTP request failed: " + response.message());
                }
            }
        });
    }




    private String buildGPTRequestBody(String message) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-3.5-turbo");

            // Construct the messages array
            JSONArray messagesArray = new JSONArray();

            // Add persona information as system prompt
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            //systemMessage.put("content", personaDescription);
            messagesArray.put(systemMessage);

            // Add the user message
            JSONObject userMessageObject = new JSONObject();
            userMessageObject.put("role", "user");
            userMessageObject.put("content", message);
            messagesArray.put(userMessageObject);

            // Add the messages array to the request body
            jsonBody.put("messages", messagesArray);
            jsonBody.put("max_tokens", 150); // Set appropriate max tokens
            jsonBody.put("temperature", 0.6); // Adjust temperature for creativity

            return jsonBody.toString();
        } catch (JSONException e) {
            Log.e("ChatRepository", "Failed to build JSON request body: " + e.getMessage());
            return "{}"; // Return an empty JSON object if building the request body fails
        }
    }


    public void sendMessageToGPT(String message, String personaId, String chatId, String gptKey, ApiCallback callback) {
        Log.d("ChatRepository", "sendMessageToGPT called with message: " + message);
        String apiUrl = "https://api.openai.com/v1/completions";
        String requestBody = buildGPTRequestBody(message);
        Log.d("ChatRepository", "Request Body: " + requestBody);

        sendHttpRequest(apiUrl, gptKey, requestBody, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("ChatRepository", "HTTP request successful, response: " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onFailure(String error) {
                Log.e("ChatRepository", "HTTP request failed, error: " + error);
                callback.onFailure(error);
            }


        });}
}


