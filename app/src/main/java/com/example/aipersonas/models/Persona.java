package com.example.aipersonas.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "persona_table")
public class Persona {

    @PrimaryKey
    @NonNull
    private String personaId;  // ID shared with Firestore (String instead of int)
    private String name;
    private String description;

    // Empty constructor for Firestore serialization
    public Persona(String _tmpName, String _tmpDescription) {}

    // Constructor
    public Persona(String personaId, String name, String description) {
        this.personaId = personaId;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    @NonNull
    public String getPersonaId() {
        return personaId;
    }

    public void setPersonaId(@NonNull String personaId) {
        this.personaId = personaId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
