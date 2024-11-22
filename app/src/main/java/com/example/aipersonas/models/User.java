package com.example.aipersonas.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class User {

    @PrimaryKey @NonNull
    private String userId;  // ID shared with Firestore
    private String firstName;
    private String lastName;
    private String email;   // User's email
    private String avatarUrl; // Optional: URL to the user's avatar image

    // Empty constructor for Firestore serialization
    public User() {
    }

    // Constructor for creating a User instance
    @Ignore
    public User(@NonNull String userId, String firstName, String lastName, String email, String avatarUrl) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    // Getters and Setters
    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return  firstName;
    }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() {return lastName;}

    public void setLastName(String lastName) {this.lastName = lastName;}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }


}
