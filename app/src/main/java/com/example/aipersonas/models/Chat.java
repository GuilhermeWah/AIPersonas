package com.example.aipersonas.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.aipersonas.databases.Converters;
import com.google.firebase.Timestamp;

import java.util.UUID;

@Entity(tableName = "chat_table")
@TypeConverters(Converters.class)
public class Chat {

    @PrimaryKey
    @NonNull
    private String chatId;
    private String userId;
    private String personaId;
    private String personaTitle;
    private String lastMessage;
    private Timestamp timestamp;

    // Empty constructor for Firestore serialization
    @Ignore
    public Chat() {
    }

    // Default constructor for Room
    public Chat(@NonNull String chatId, String userId, String personaId, String personaTitle, String lastMessage, Timestamp timestamp) {
        this.chatId = chatId;
        this.userId = userId;
        this.personaId = personaId;
        this.personaTitle = personaTitle;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    // Constructor for new Chat objects
    @Ignore
    public Chat(@NonNull String userId, String personaId, String personaTitle, String lastMessage, Timestamp timestamp) {
        this.chatId = UUID.randomUUID().toString();
        this.userId = userId;
        this.personaId = personaId;
        this.personaTitle = personaTitle;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    // Constructor for preexisting Chat objects
    @Ignore
    public Chat(String chatId, String personaId, String message) {
        this.chatId = chatId;
        this.personaId = personaId;
        this.lastMessage = message;
        this.timestamp = Timestamp.now();
    }

    @NonNull
    public String getChatId() {
        return chatId;
    }

    public void setChatId(@NonNull String chatId) {
        this.chatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPersonaId() {
        return personaId;
    }

    public void setPersonaId(String personaId) {
        this.personaId = personaId;
    }

    public String getPersonaTitle() {
        return personaTitle;
    }

    public void setPersonaTitle(String personaTitle) {
        this.personaTitle = personaTitle;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
