package com.example.aipersonas.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_table")
public class Chat {

    @PrimaryKey(autoGenerate = true)
    private int chatId;
    private String userId; // Foreign key to link to a User
    private String personaId; // Foreign key to link to a Persona
    private String personaTitle; // Optional field to store the name of the associated persona;
    private String lastMessage;
    private long timestamp;

    // Constructor
    public Chat(String userId,String personaId, String personaTitle, String lastMessage, long timestamp) {
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
    public void setLastMessage (String lastMessage){
        this.lastMessage = lastMessage;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPersonaId(String personaId){
        this.personaId = personaId;
    }
    public void setPersonaTitle(String personaTitle) {
        this.personaTitle = personaTitle;
    }

    public String getPersonaId() {
        return personaId;
    }
    public String getPersonaTitle(){
        return personaTitle;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
