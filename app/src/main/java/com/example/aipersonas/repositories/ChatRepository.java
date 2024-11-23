package com.example.aipersonas.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aipersonas.databases.ChatDAO;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository {
    private static final String TAG = "ChatRepository";
    private final ChatDAO chatDAO;
    private final FirebaseFirestore firebaseFirestore;
    private final ExecutorService executor;
    private final String userId;

    // Firestore references
    private final CollectionReference usersCollection;

    // Real-time listener for chats
    private ListenerRegistration chatListener;
    private final MutableLiveData<List<Chat>> allChatsLiveData;

    public ChatRepository(ChatDAO chatDAO) {
        this.chatDAO = chatDAO;
        firebaseFirestore = FirebaseFirestore.getInstance();
        executor = Executors.newSingleThreadExecutor();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersCollection = firebaseFirestore.collection("Users");
        allChatsLiveData = new MutableLiveData<>();

    }
    // Insert a chat into both Firestore and Room
    public void insertChat(Chat chat) {
        executor.execute(() -> {
            chatDAO.insertChat(chat);
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Personas")
                    .document(chat.getPersonaId())
                    .collection("Chats")
                    .document(chat.getChatId())
                    .set(chat)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat added successfully in Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding chat to Firestore", e));
        });
    }

    // Update a chat in both Firestore and Room
    public void updateChat(Chat chat) {
        executor.execute(() -> {
            chatDAO.updateChat(chat);
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .collection("Chats")
                    .document(chat.getChatId())
                    .set(chat)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat updated successfully in Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating chat in Firestore", e));
        });
    }

    // Delete a chat from both Firestore and Room
    public void deleteChat(Chat chat) {
        executor.execute(() -> {
            chatDAO.deleteChat(chat.getChatId());
    });
    }

    // Get all chats for a specific persona
    public LiveData<List<Chat>> getChatsForPersona(String personaId) {
        return chatDAO.getAllChatsForPersona(personaId);
    }

    // Fetch chats from Firestore and update local Room database
    public void fetchChatsFromFirestore(String personaId) {
        firebaseFirestore.collection("Users")
                .document(userId)
                .collection("Chats")
                .whereEqualTo("personaId", personaId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> executor.execute(() -> {
                    chatDAO.deleteAllChatsForPersona(personaId);
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Chat chat = document.toObject(Chat.class);
                        chatDAO.insertChat(chat);
                    }
                }))
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching chats from Firestore", e));
    }

    // Method to listen to chats in real-time from Firestore
    public void listenToChats(String personaId) {
        CollectionReference chatsRef = firebaseFirestore
                .collection("Users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Personas")
                .document(personaId)
                .collection("Chats");

         chatListener = chatsRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            executor.execute(() -> {
                if (queryDocumentSnapshots != null) {
                    List<Chat> chatList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Chat chat = document.toObject(Chat.class);
                        if (chat != null) {
                            chatList.add(chat);
                            chatDAO.insertChat(chat);  // Update Room with the latest chat data
                        }
                    }
                    allChatsLiveData.postValue(chatList);
                }
            });
        });
    }

    public void stopListeningToChats() {
        if (chatListener != null) {
            chatListener.remove();
            chatListener = null;
        }
    }

    /**
     *
     *##################       ###############
     *          MESSAGES SECTION
     *          ###########################
     * ##################           ###########
     */

    // Insert a message into both Firestore and Room
    public void addMessage(@NonNull Message message) {
        if (userId == null || message.getPersonaId() == null || message.getChatId() == null) {
            Log.e(TAG, "Invalid IDs: userId=" + userId + ", personaId=" + message.getPersonaId() + ", chatId=" + message.getChatId());
            return;
        }

        usersCollection
                .document(userId)
                .collection("Personas")
                .document(message.getPersonaId())
                .collection("Chats")
                .document(message.getChatId())
                .collection("Messages")
                .document(message.getMessageId())
                .set(message)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Message added successfully to Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding message to Firestore", e));
    }



    // Listen to messages in real-time from Firestore
    public void listenToMessages(String personaId, String chatId) {
        CollectionReference messagesRef = firebaseFirestore
                .collection("Users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Personas")
                .document(personaId)
                .collection("Chats")
                .document(chatId)
                .collection("Messages");

        messagesRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            executor.execute(() -> {
                if (queryDocumentSnapshots != null) {
                    List<Message> messageList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Message message = document.toObject(Message.class);
                        if (message != null) {
                            messageList.add(message);
                            chatDAO.insertMessage(message);  // Update Room with the latest message data
                        }
                    }
                }
            });
        });
    }


    // Get all messages for a specific chat
    public LiveData<List<Message>> getMessagesForChat(String chatId) {
        return chatDAO.getMessagesForChat(chatId);
    }

    // Method to get all chats live data
    public LiveData<List<Chat>> getAllChats() {
        return allChatsLiveData;
    }

    // Update a message's status in both Firestore and Room
    public void updateMessageStatus(String chatId, String status) {
        // Update in Room database
        executor.execute(() -> {
            chatDAO.updateMessageStatus(chatId, status);
            Log.d(TAG, "Message status updated in Room: " + status);
        });
    }

    // Delete a specific message from both Firestore and Room
    public void deleteMessage(@NonNull Message message) {
        // Delete from Room database
        executor.execute(() -> {
            chatDAO.deleteMessage(message.getMessageId());
            Log.d(TAG, "Message deleted from Room: " + message.getMessageContent());
        });

        // Delete from Firestore database
        firebaseFirestore.collection("Users")
                .document(userId)
                .collection("Personas")
                .document(message.getPersonaId())
                .collection("Chats")
                .document(message.getChatId())
                .collection("Messages")
                .document(message.getMessageId())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Message successfully deleted from Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting message from Firestore", e));
    }
}
