package com.example.aipersonas.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "persona_table")
public class Persona {

    @PrimaryKey(autoGenerate = true)
    private int personaId;
    private String name;
    private String description;

    // Constructor
    public Persona(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public int getPersonaId() {
        return personaId;
    }

    public void setPersonaId(int personaId) {
        this.personaId = personaId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
