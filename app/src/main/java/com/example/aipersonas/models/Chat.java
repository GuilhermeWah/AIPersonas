package com.example.aipersonas.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_table")
public class Chat {

    @PrimaryKey(autoGenerate = true)
    private int chatId;
    private int personaId; // Foreign key to link to a Persona
    private String personaTitle; // Optional field to store the name of the associated persona;
    private String lastMessage;
    private String timestamp;

    // Constructor
    public Chat(int personaId, String personaTitle, String lastMessage, String timestamp) {
        this.personaId = personaId;
        this.personaTitle = personaTitle;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getPersonaId() {
        return personaId;
    }
    public String getPersonaTitle(){
        return personaTitle;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
