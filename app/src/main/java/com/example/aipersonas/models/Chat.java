package com.example.aipersonas.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.Timestamp;

import java.util.UUID;

@Entity(tableName = "chat_table")
public class Chat {

    @PrimaryKey @NonNull
    private String chatId; // Updated primary key to be compatible with autoGenerate
    private String userId;
    private String personaId;
    private String personaName;
    private String lastMessage;
    private String chatSummary;
    private Timestamp lastSummaryTime;
    private Timestamp lastMessageTime;
    private boolean isActive;
    private String status;

    // No-arg constructor required by Room
    public Chat() {
        this.chatId = UUID.randomUUID().toString();
    }



    // Parameterized constructor for other uses
    @Ignore
    public Chat(String userId, String personaId, String name, String lastMessage, Timestamp lastMessageTime, boolean isActive, String status) {
        this.chatId = UUID.randomUUID().toString(); // Manually generate ID
        this.userId = userId;
        this.personaId = personaId;
        this.personaName = name;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.isActive = isActive;
        this.status = status;
    }


    // Getters and Setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
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

    public String getPersonaName() {
        return personaName;
    }

    public void setPersonaName(String name) {
        this.personaName = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Timestamp lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChatSummary() {
        return chatSummary;
    }

    public void setChatSummary(String chatSummary) {
        this.chatSummary = chatSummary;
    }

    public Timestamp getLastSummaryTime() {
        return lastSummaryTime;
    }

    public void setLastSummaryTime(Timestamp lastSummaryTime) {
        this.lastSummaryTime = lastSummaryTime;
    }

}
