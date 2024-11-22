package com.example.aipersonas.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.io.IOException;

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

    public GPTRepository(Application application) {
        this.client = new OkHttpClient();
        this.apiConfigRepository = new APIConfigRepository(application);
        this.gptKey = fetchGPTKey(); // Initialize gptKey with null
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


    // Helper to build request body for chat models
    private String buildGPTRequestBody(String description, int maxTokens, float temperature) {
        String instruction = "You are an AI language model tasked with tailoring persona descriptions for user-facing virtual assistants. " +
                "The purpose of tailoring this persona description is to help GPT effectively maintain context throughout interactions with the user, " +
                "and to ensure that the virtual assistant provides responses that are highly relevant, personalized, and context-aware. " +
                "Your goal is to refine the given persona description by making it more engaging, precise, and informative. " +
                "Highlight the persona's role, key expertise, personality traits, motivation, and interaction style. " +
                "Make sure the refined description supports consistency, context retention, and user engagement, while maintaining a balance between professionalism and approachability.";

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






}


