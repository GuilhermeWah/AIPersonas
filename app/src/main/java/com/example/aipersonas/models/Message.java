package com.example.aipersonas.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.aipersonas.databases.Converters;
import com.google.firebase.Timestamp;

import java.util.UUID;

@Entity(tableName = "message_table")
@TypeConverters(Converters.class) // Handle complex types like Timestamp
public class Message {

    @PrimaryKey
    @NonNull
    private String messageId;
    private String chatId;
    private String senderId; // Could be "user" or "GPT"
    private String messageContent; // User's message
    private String gptResponse; // GPT's response
    private Timestamp timestamp; // User's message timestamp
    private Timestamp responseTimestamp; // When GPT responded
    private String status; // e.g., "sent", "typing", "received", "completed"

    // non-arg constructor required by Firestore
    public Message() {
        this.messageId = UUID.randomUUID().toString();
    }



    // Parameterized constructor for creating a Message instance
    @Ignore
    public Message(String chatId, String senderId, String messageContent, Timestamp timestamp, String status) {
        this.messageId = UUID.randomUUID().toString();
        this.chatId = chatId;
        this.senderId = senderId;
        this.messageContent = messageContent;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and Setters
    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Additional getter for personaId if necessary
    public String getPersonaId() {
        return this.senderId;  // Assuming the persona ID is stored in the senderId field
    }

    public String getGptResponse() {
        return gptResponse;
    }

    public void setGptResponse(String gptResponse) {
        this.gptResponse = gptResponse;
    }

    public Timestamp getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(Timestamp responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", chatId='" + chatId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", messageContent='" + messageContent + '\'' +
                ", gptResponse='" + gptResponse + '\'' +
                ", timestamp=" + timestamp +
                ", responseTimestamp=" + responseTimestamp +
                ", status='" + status + '\'' +
                '}';
    }


}
