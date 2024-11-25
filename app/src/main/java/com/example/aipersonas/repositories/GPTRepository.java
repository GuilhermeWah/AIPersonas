package com.example.aipersonas.repositories;

import android.app.Application;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    private final List<JSONObject> conversationHistory = new ArrayList<>();


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
    public void sendGPTRequest(String userMessage, int maxTokens, float temperature, ApiCallback callback) {
        if (gptKey == null) {
            Log.e(TAG, "GPT Key is null, cannot proceed.");
            callback.onFailure("Missing GPT API key");
            return;
        }

        try {
            // Add the user's message to the conversation history
            JSONObject userMessageObject = new JSONObject();
            userMessageObject.put("role", "user");
            userMessageObject.put("content", userMessage);
            conversationHistory.add(userMessageObject);

            // Create the JSON array of messages
            JSONArray messagesArray = new JSONArray(conversationHistory);

            // Construct the JSON request body
            JSONObject requestBodyJson = new JSONObject();
            requestBodyJson.put("model", "gpt-3.5-turbo");
            requestBodyJson.put("messages", messagesArray);
            requestBodyJson.put("max_tokens", maxTokens);
            requestBodyJson.put("temperature", temperature);

            String requestBody = requestBodyJson.toString();
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
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
                            if (choicesArray.length() > 0) {
                                JSONObject messageObject = choicesArray.getJSONObject(0).getJSONObject("message");
                                String messageContent = messageObject.getString("content");

                                // Clean up the response to make sure it's formatted properly
                                messageContent = formatGPTResponse(messageContent);

                                // Add the assistant's response to the conversation history
                                JSONObject assistantMessageObject = new JSONObject();
                                assistantMessageObject.put("role", "assistant");
                                assistantMessageObject.put("content", messageContent);
                                conversationHistory.add(assistantMessageObject);

                                // Pass the properly formatted content to the callback
                                callback.onSuccess(messageContent);
                            } else {
                                callback.onFailure("No valid response from GPT");
                            }
                        } catch (JSONException e) {
                            callback.onFailure("Error parsing GPT response: " + e.getMessage());
                        }
                    } else {
                        String errorResponse = response.body() != null ? response.body().string() : "Unknown error";
                        Log.e(TAG, "HTTP request failed with code: " + response.code() + ", response: " + errorResponse);
                        callback.onFailure(errorResponse);
                    }
                }
            });
        } catch (JSONException e) {
            callback.onFailure("Error constructing GPT request: " + e.getMessage());
        }
    }

    // Helper method to format the GPT response properly
    private String formatGPTResponse(String response) {
        // Remove unnecessary information or formatting from the response
        return response.trim();
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
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray choicesArray = jsonResponse.getJSONArray("choices");
                        if (choicesArray.length() > 0) {
                            JSONObject messageObject = choicesArray.getJSONObject(0).getJSONObject("message");
                            String summaryContent = messageObject.getString("content");
                            callback.onSuccess(summaryContent.trim());
                        } else {
                            callback.onFailure("No valid response from GPT");
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Error parsing GPT response: " + e.getMessage());
                    }
                } else {
                    String errorResponse = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "HTTP request failed with code: " + response.code() + ", response: " + errorResponse);
                    callback.onFailure(errorResponse);
                }
            }
        });
    }

    // Method to request summarization if needed
    /**
     * Method to request summarization if certain conditions are met.
     *
     * This method observes the list of messages associated with a given chat ID and determines
     * if summarization is necessary based on the following conditions:
     * 1. **Message Count-Based Trigger**: Summarization occurs when the number of messages in the chat
     *    reaches a multiple of 10. This helps to manage context as conversations grow.
     * 2. **Inactivity-Based Trigger**: If the chat has been inactive for a specific duration (e.g., 10 minutes),
     *    a summary is generated to ensure that context is preserved in case of long pauses.
     *
     * The summarization result is saved in both the Room database and Firestore to maintain synchronization
     * between local and remote storage. The approach aims to avoid overwhelming Firestore with frequent updates
     * by limiting summarization to only when these conditions are met.
     *
     * Parameters:
     * - `chatId` (String): The ID of the chat for which summarization is being requested.
     * - `lifecycleOwner` (LifecycleOwner): Used to observe LiveData and ensure lifecycle-aware operations.
     */
    public void requestSummarizationIfNeeded(String chatId, LifecycleOwner lifecycleOwner) {
        LiveData<List<Message>> liveMessages = chatDAO.getMessagesForChat(chatId);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String personaId = chatDAO.getPersonaIdForChat(chatId);

        // Observe the liveMessages with a lifecycleOwner
        liveMessages.observe(lifecycleOwner, messages -> {
            // Check conditions for summarization
            boolean shouldSummarizeByMessageCount = messages.size() % 10 == 0 && messages.size() != 0;
            boolean shouldSummarizeByInactivity = shouldSummarizeDueToInactivity(chatId);

            if (shouldSummarizeByMessageCount || shouldSummarizeByInactivity) {
                // Trigger summarization
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

                                // Update Firestore with the summary details
                                firebaseFirestore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Personas").document(personaId)
                                        .collection("Chats").document(chatId)
                                        .update("chatSummary", summary, "lastSummaryTime", Timestamp.now())
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Summary successfully updated in Firestore."))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error updating summary in Firestore", e));
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "Failed to generate summary: " + errorMessage);
                    }
                });
            }
        });
    }

    // Helper method to determine if summarization should be triggered due to inactivity
    private boolean shouldSummarizeDueToInactivity(String chatId) {
        // Fetch the chat from Room
        Chat chat = chatDAO.getChatByIdSync(chatId);

        if (chat != null && chat.getLastSummaryTime() != null) {
            long currentTime = Timestamp.now().toDate().getTime();
            long lastSummaryTime = chat.getLastSummaryTime().toDate().getTime();
            long inactivityDuration = currentTime - lastSummaryTime;

            // Check if there has been more than 10 minutes of inactivity (600,000 ms)
            return inactivityDuration > 600000;
        }
        return false;
    }

}
