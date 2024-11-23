package com.example.aipersonas.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "persona_table")
public class Persona {

    @PrimaryKey @NonNull
    private String personaId;  // ID shared with Firestore (String instead of int)
    private String name;
    private String personaDescription;
    private String gptDescription;

    // Empty constructor for Firestore serialization
    @Ignore
    public Persona() {
        this.personaId = UUID.randomUUID().toString(); // Manually generate ID
    }

    // Constructor
    public Persona(String name, String personaDescription) {
        this.personaId = UUID.randomUUID().toString(); // Manually generate ID
        this.name = name;
        this.personaDescription = personaDescription;
        this.gptDescription = "";
    }

    // Getters and Setters
    @NonNull
    public String getPersonaId() {
        return personaId;
    }

    public void setPersonaId(@NonNull String personaId) {
        this.personaId = personaId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPersonaDescription (String personaDescription) {this.personaDescription = personaDescription; }

    public String getName() {
        return name;
    }

    public String getPersonaDescription() {return personaDescription;}

    public String getGptDescription() {return gptDescription;}

    public void setGptDescription(String gptDescription) {this.gptDescription = gptDescription;}
}
