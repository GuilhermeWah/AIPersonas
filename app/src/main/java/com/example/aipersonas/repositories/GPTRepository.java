package com.example.aipersonas.repositories;

import android.app.Application;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDAO;
import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.example.aipersonas.utils.SummaryUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GPTRepository {

    private static final String TAG = "GPTRepository";
    private final OkHttpClient client;
    private final APIConfigRepository apiConfigRepository; // Reference to get the API key
    private final String gptKey;
    private final FirebaseFirestore firebaseFirestore;
    private final Executor executor;
    private final ChatDAO chatDAO;



    public GPTRepository(Application application) {
        this.client = new OkHttpClient();
        this.apiConfigRepository = new APIConfigRepository(application);
        this.gptKey = fetchGPTKey();
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        this.chatDAO = ChatDatabase.getInstance(application).chatDao();


    }



    // Define ApiCallback interface
    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    private String fetchGPTKey() {
        String apiKey = apiConfigRepository.getGPTKey();
        if (apiKey != null) {
            return apiKey;
        } else {
            Log.e(TAG, "Failed to fetch GPT API key");
            return null;
        }
    }

    public String getGptKey() {
        return gptKey;
    }

    // Function to handle GPT requests
    public void sendGPTRequest(String prompt, int maxTokens, float temperature, ApiCallback callback) {

        if (gptKey == null) {
            Log.e(TAG, "GPT Key is null, cannot proceed.");
            callback.onFailure("Missing GPT API key");
            return;
        }

        String requestBody = buildGPTRequestBody(prompt, maxTokens, temperature);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + gptKey)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "HTTP request failed: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().string());
                } else {
                    String errorResponse = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "HTTP request failed with code: " + response.code() + ", response: " + errorResponse);
                    callback.onFailure(errorResponse);
                }
            }
        });
    }

    //@TODO: Documenting this function
    public String buildGPTRequestBody(String description, int maxTokens, float temperature) {
        String instruction = "You are tasked with refining the persona description provided for internal use only. "
                + "This refined description is intended to optimize GPT's understanding and simulation of the persona during user interactions. "
                + "The purpose is to make the persona more effective in responding to user queries by capturing key skills, expertise, personality traits, motivations, and communication style. "
                + "This internal version should help GPT maintain context, exhibit consistent personality traits, and respond in a manner that aligns with the persona’s defined characteristics throughout all interactions. "
                + "Make sure to emphasize the persona's role, capabilities, motivations, and approach to communication, ensuring the resulting description is insightful, comprehensive, and well-structured for internal use by GPT. "
                + "The refined description will not be displayed to the user but will be used solely to guide GPT's behavior in conversations.";

        // Constructing the JSON request body
        return "{"
                + "\"model\":\"gpt-3.5-turbo\","
                + "\"messages\":["
                + "{\"role\":\"system\", \"content\":\"" + instruction + "\"},"
                + "{\"role\":\"user\", \"content\":\"" + description + "\"}"
                + "],"
                + "\"max_tokens\":" + maxTokens + ","
                + "\"temperature\":" + temperature
                + "}";
    }

    // Method to summarize messages
    public void summarizeMessages(List<Message> messages, ApiCallback callback) {
        if (gptKey == null) {
            Log.e(TAG, "GPT Key is null, cannot proceed.");
            callback.onFailure("Missing GPT API key");
            return;
        }

        StringBuilder conversation = new StringBuilder();
        for (Message message : messages) {
            conversation.append(message.getSenderId()).append(": ").append(message.getMessageContent()).append("\n");
        }

        String requestBody = "{"
                + "\"model\":\"gpt-3.5-turbo\","
                + "\"messages\":["
                + "{\"role\":\"system\", \"content\":\"Please summarize the following conversation:\"},"
                + "{\"role\":\"user\", \"content\":\"" + conversation.toString() + "\"}"
                + "],"
                + "\"max_tokens\":500,"
                + "\"temperature\":0.5"
                + "}";

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + gptKey)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "HTTP request failed: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().string());
                } else {
                    String errorResponse = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "HTTP request failed with code: " + response.code() + ", response: " + errorResponse);
                    callback.onFailure(errorResponse);
                }
            }
        });
    }

    /**Method to request summarization if needed (ask her in class)
    // BUG 1:  Attempting to perform a database operation
    // (getPersonaIdForChat) on the main thread. Android prevents such operations to avoid UI freezing.
    // The reason for this happening is: getPersonaIdForChat in the chatDAO is being called synchronously
    // --> (getPersonaIdForChat(chatId)) <--- inside the summarization function
    //  Calling this synchronously locks  the main thread which leads to  IllegalStateException
    // To fix this we gotta make sure  the getPersonaIdForChat is called asynchronously (on a background thread)
    // We can use ExecutorService  to run the query on the bg and handle it in the mainthread;
     // Or we can simply change the return  type on our DAO to LiveData<String>. Ended up going with the latter*/

    public void requestSummarizationIfNeeded(String chatId, LifecycleOwner lifecycleOwner) {
        LiveData<List<Message>> liveMessages = chatDAO.getMessagesForChat(chatId);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LiveData<String> personaId = chatDAO.getPersonaIdForChat(chatId); // This line is causing the crash we gotta run on the bg thread
        String personaIdValue = personaId.getValue(); // Extract String from LiveData
        if (personaId == null || chatId == null) {
            Log.e(TAG, "PersonaId or ChatId is null. Cannot generate Firestore path.");
            return;
        }


        // Observe the liveMessages with a lifecycleOwner
        liveMessages.observe(lifecycleOwner,  messages -> {
            if (messages.size() % 10 == 0 && messages.size() >= 10) {
                // Trigger summarization after every 10 messages
                summarizeMessages(messages, new ApiCallback() {
                    @Override
                    public void onSuccess(String summary) {
                        Log.d(TAG, "Summary generated: " + summary);

                        // Save the summary to Room and Firestore
                        executor.execute(() -> {
                            Chat chat = chatDAO.getChatByIdSync(chatId);
                            if (chat != null) {
                                chat.setChatSummary(summary);
                                chat.setLastSummaryTime(Timestamp.now());
                                chatDAO.updateChat(chat);
                            }
                        });

                        // Also store the summary in Firestore
                        firebaseFirestore.collection("Users").document(currentUserId)
                                .collection("Personas").document(personaIdValue)
                                .collection("Chats").document(chatId)
                                .update("chatSummary", summary, "lastSummaryTime", Timestamp.now())
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Summary successfully updated in Firestore."))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating summary in Firestore", e));
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "Failed to generate summary: " + errorMessage);
                    }
                });
            }
        });
    }







}
