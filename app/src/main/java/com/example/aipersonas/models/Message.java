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
    private String senderId;
    private String content;
    private Timestamp timestamp;
    private boolean isSent;


    // non arg constructor required by Firestore
    public Message() {
        this.messageId = UUID.randomUUID().toString();
    }

    // Parameterized constructor for creating a Message instance
    @Ignore
    public Message(String chatId, String senderId, String content, Timestamp timestamp, boolean isSent) {
        this.messageId = UUID.randomUUID().toString();
        this.chatId = chatId;
        this.senderId = senderId; // Fixed parameter name to match the field
        this.content = content;
        this.timestamp = timestamp;
        this.isSent = isSent;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", chatId='" + chatId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", isSent=" + isSent +
                '}';
    }
}