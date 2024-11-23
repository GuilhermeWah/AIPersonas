package com.example.aipersonas.utils;

import com.example.aipersonas.models.Message;

import java.util.List;

public class SummaryUtils {

    // Generates a summary from a list of messages in a chat
    public static String generateSummaryFromMessages(List<Message> messages) {
        StringBuilder conversationHistory = new StringBuilder();

        for (Message message : messages) {
            conversationHistory.append(message.getSenderId()).append(": ")
                    .append(message.getMessageContent()).append("\n");
        }

        return generateSummaryFromConversation(conversationHistory.toString());
    }

    // Summarizes a given conversation history string
    public static String generateSummaryFromConversation(String conversationHistory) {
        // Placeholder for a more advanced summarization logic
        // Can integrate with GPT-3.5 or other models to create a summary
        return "Summarized conversation text based on: \n" + conversationHistory;
    }

    // Formats the summary if needed (e.g., adding headers or trimming content)
    public static String formatSummary(String rawSummary) {
        return "Summary: " + rawSummary;
    }

    // Updates an existing summary with new messages
    public static String updateSummary(String currentSummary, List<Message> newMessages) {
        StringBuilder updatedHistory = new StringBuilder(currentSummary);
        updatedHistory.append("\nNew Messages:\n");

        for (Message message : newMessages) {
            updatedHistory.append(message.getSenderId()).append(": ")
                    .append(message.getMessageContent()).append("\n");
        }

        return generateSummaryFromConversation(updatedHistory.toString());
    }

    // Utility to check if summarization is required based on message count or conditions
    public static boolean isSummarizationRequired(int messageCount, long lastSummaryTimestamp, long currentTimestamp) {
        // Example condition: Summarize after every 10 messages or after a certain time has passed
        return messageCount >= 10 || (currentTimestamp - lastSummaryTimestamp) > 600000; // 10 minutes
    }
}
