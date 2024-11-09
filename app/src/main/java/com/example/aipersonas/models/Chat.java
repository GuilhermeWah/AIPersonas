package com.example.aipersonas.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.aipersonas.databases.Converters;
import com.google.firebase.Timestamp;

import java.util.UUID;

@Entity(tableName = "chat_table")
@TypeConverters(Converters.class) // Attach the TypeConverter to handle the timestamp field
public class Chat {

    @PrimaryKey
    @NonNull
    private String chatId;  // Manually generate ID (UUID) for consistency across Room and Firestore
    private String userId;  // Foreign key to link to a User
    private String personaId;  // Foreign key to link to a Persona
    private String personaTitle;  // Optional field to store the name of the associated persona

    private String lastMessage;
    private Timestamp timestamp;

    // Empty Constructor for Firestore serialization
    public Chat() {
        this.chatId = UUID.randomUUID().toString(); // Generate unique ID
    }

    // Constructor for creating a Chat instance
    public Chat(String userId, String personaId, String personaTitle, String lastMessage, Timestamp timestamp) {
        this.chatId = UUID.randomUUID().toString(); // Generate unique ID
        this.userId = userId;
        this.personaId = personaId;
        this.personaTitle = personaTitle;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    // Getters and Setters
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
