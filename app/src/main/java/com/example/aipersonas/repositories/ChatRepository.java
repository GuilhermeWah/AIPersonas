package com.example.aipersonas.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDAO;
import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository {

    private ChatDAO chatDAO;
    private LiveData<List<Chat>> allChats;
    private LiveData<List<Chat>> chatsForPersona;
    private ExecutorService executorService;
    private String userId, personaId;
    private FirebaseFirestore firebaseFirestore;


    public ChatRepository(Application application, String personaId) {
        ChatDatabase database = ChatDatabase.getInstance(application);

        // Initialize Firebase Authentication and get the current user's ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatDAO = database.chatDao();

        allChats = chatDAO.getAllChats(userId);
        executorService = Executors.newFixedThreadPool(2);
        firebaseFirestore = FirebaseFirestore.getInstance(); //initialize firestore
        this.personaId = personaId;

        /*
        * this method is necessary because what if user is logged in on another device?
        * The changes made there will be stored on firestore, but not on room.
        */

        // Fetch data from Firestore if online
        if (NetworkUtils.isNetworkAvailable(application.getApplicationContext())) {
            fetchChatsFromFirestore(personaId);
        }
    }

    public LiveData<List<Chat>> getAllChats() {
        return allChats;
    }

    public LiveData<List<Chat>> getChatsForPersona(String personaId) {
        return chatDAO.getChatsForPersona(personaId);
    }

    public void insert(Chat chat, String personaId) {
        executorService.execute(() -> {

            // Insert into Room
            executorService.execute(() -> {
                Chat existingChat = chatDAO.getChatById(chat.getChatId());
                if (existingChat == null) {
                    chatDAO.insert(chat);
                } else {
                    chatDAO.update(chat);
                }
            });

            // Insert into Firestore with personaId
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(personaId)
                    .collection("Chats")
                    .document(String.valueOf(chat.getChatId()))
                    .set(chat)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully added to Firestore
                    })
                    .addOnFailureListener(e -> {
                        // Log error
                        e.printStackTrace();
                    });
        });
    }


    public void update(Chat chat, String personaId) {
        executorService.execute(() -> {
            // Update Room Database
            chatDAO.update(chat);

            // Update Firestore
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(personaId)
                    .collection("Chats")
                    .document(String.valueOf(chat.getChatId()))
                    .set(chat)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully updated in Firestore
                    })
                    .addOnFailureListener(e -> {
                        // Log error
                        e.printStackTrace();
                    });
        });
    }

    public void delete(Chat chat, String personaId) {
        executorService.execute(() -> {
            // Delete from Room Database
            chatDAO.delete(chat);

            // Delete from Firestore
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(personaId)
                    .collection("Chats")
                    .document(String.valueOf(chat.getChatId()))
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Successfully deleted from Firestore
                    })
                    .addOnFailureListener(e -> {
                        // Log error
                        e.printStackTrace();
                    });
        });
    }

    // Fetch chats from Firestore and store them in Room to keep data synchronized
    private void fetchChatsFromFirestore(String personaId) {
        firebaseFirestore.collection("Users")
                .document(userId)
                .collection("Personas")
                .document(personaId)
                .collection("Chats")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    executorService.execute(() -> {
                        // Clear old data to avoid duplication
                        chatDAO.deleteAllChatsForUser(userId);

                        // Insert fetched data into Room
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Chat chat = document.toObject(Chat.class);
                            chatDAO.insert(chat);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Log error if data fetching fails
                    e.printStackTrace();
                });
    }


    public String getUserId() {
        return userId;
    }
}

