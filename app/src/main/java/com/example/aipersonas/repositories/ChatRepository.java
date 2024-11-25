package com.example.aipersonas.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aipersonas.databases.ChatDAO;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
        executor = Executors.newCachedThreadPool();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersCollection = firebaseFirestore.collection("Users");
        allChatsLiveData = new MutableLiveData<>();
    }

    // Utility method to get Firestore path for chats
    private DocumentReference getChatDocumentRef(String personaId, String chatId) {
        return usersCollection
                .document(userId)
                .collection("Personas")
                .document(personaId)
                .collection("Chats")
                .document(chatId);
    }


    // Insert or update a chat in both Firestore and Room
    public void insertOrUpdateChat(@NonNull Chat chat) {
        if (chat.getUserId() == null || chat.getPersonaId() == null || chat.getChatId() == null) {
            Log.e(TAG, "Invalid IDs: userId=" + chat.getUserId() + ", personaId=" + chat.getPersonaId() + ", chatId=" + chat.getChatId());
            return;
        }

        executor.execute(() -> {
            // Insert chat into Room database using DAO
            chatDAO.insertChat(chat);

            // Insert or update the chat in Firestore
            usersCollection
                    .document(chat.getUserId())
                    .collection("Personas")
                    .document(chat.getPersonaId())
                    .collection("Chats")
                    .document(chat.getChatId())
                    .set(chat)  // This stores the entire Chat object in Firestore
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat added/updated successfully in Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding/updating chat in Firestore", e));
        });
    }





    // Delete a chat from both Firestore and Room
    public void deleteChat(Chat chat) {
        executor.execute(() -> {
            chatDAO.deleteChat(chat.getChatId());
            getChatDocumentRef(chat.getPersonaId(), chat.getChatId()).delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat deleted successfully from Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting chat from Firestore", e));
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
                .collection("Personas")
                .document(personaId)
                .collection("Chats")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> executor.execute(() -> {
                    chatDAO.deleteAllChatsForPersona(personaId);
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Chat chat = document.toObject(Chat.class);
                        if (chat != null) {
                            chatDAO.insertChat(chat);
                        }
                    }
                }))
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching chats from Firestore", e));
    }

    // Method to listen to chats in real-time from Firestore
    public void listenToChats(String personaId) {
        CollectionReference chatsRef = usersCollection
                .document(userId)
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

    // Insert a message into both Firestore and Room
    public void addMessage(@NonNull Message message) {
        if (userId == null || message.getPersonaId() == null || message.getChatId() == null) {
            Log.e(TAG, "Invalid IDs: userId=" + userId + ", personaId=" + message.getPersonaId() + ", chatId=" + message.getChatId());
            return;
        }

        executor.execute(() -> {
            // Insert the message into Room
            chatDAO.insertMessage(message);

            // Insert the message into Firestore
            usersCollection
                    .document(userId)
                    .collection("Personas")
                    .document(message.getPersonaId())
                    .collection("Chats")
                    .document(message.getChatId())
                    .collection("Messages")
                    .document(message.getMessageId())
                    .set(message)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Message added successfully to Firestore");

                        // After successfully adding the message, update the chat's last message info
                        updateChatWithLastMessage(message);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding message to Firestore", e));
        });
    }


    private void updateChatWithLastMessage(@NonNull Message message) {
        executor.execute(() -> {
            // Get the chat from Room and update it
            Chat chat = chatDAO.getChatByIdSync(message.getChatId());
            if (chat != null) {
                chat.setLastMessage(message.getMessageContent());
                chat.setLastMessageTime(message.getTimestamp());
                chatDAO.updateChat(chat);

                // Update the chat in Firestore
                getChatDocumentRef(chat.getPersonaId(), chat.getChatId())
                        .update("lastMessage", message.getMessageContent(), "lastMessageTime", message.getTimestamp())
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat updated with last message successfully in Firestore"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating chat with last message in Firestore", e));
            }
        });
    }


    // Listen to messages in real-time from Firestore
    public void listenToMessages(String personaId, String chatId) {
        CollectionReference messagesRef = usersCollection
                .document(userId)
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
    public void updateMessageStatus(String messageId, String status) {
        executor.execute(() -> {
            chatDAO.updateMessageStatus(messageId, status);
            usersCollection
                    .document(userId)
                    .collection("Personas")
                    .document(chatDAO.getPersonaIdForChat(chatDAO.getChatByIdSync(messageId).getChatId()))
                    .collection("Chats")
                    .document(chatDAO.getChatByIdSync(messageId).getChatId())
                    .collection("Messages")
                    .document(messageId)
                    .update("status", status)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Message status updated in Firestore: " + status))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating message status in Firestore", e));
        });
    }

    // Delete a specific message from both Firestore and Room
    public void deleteMessage(@NonNull Message message) {
        executor.execute(() -> {
            chatDAO.deleteMessage(message.getMessageId());
            Log.d(TAG, "Message deleted from Room: " + message.getMessageContent());

            usersCollection
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
        });
    }

    public void updateChat(Chat chat) {
        executor.execute(() -> {
            chatDAO.updateChat(chat);
        });}

    public void insertChat(Chat chat) {
        executor.execute(() -> {
            chatDAO.insertChat(chat);
        });
    }


    public String getgptDescriptionForChat(String personaId) {
        return chatDAO.getgptDescriptionForPersona(personaId);

    }
}