package com.example.aipersonas.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;

import java.util.List;

@Dao
public interface ChatDAO {

    // Insert a new chat into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChat(Chat chat);

    // Insert a message into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(Message message);

    // Update an existing chat in the database
    @Update
    void updateChat(Chat chat);

    // Update an existing message in the database
    @Update
    void updateMessage(Message message);

    // Delete a message in the database
    @Query("DELETE FROM message_table WHERE messageId = :messageId")
    void deleteMessage(String messageId);

    // Delete all chats in the database
    @Query("DELETE FROM chat_table")
    void deleteAllChats();

    // Get all chats
    @Query("SELECT * FROM chat_table ORDER BY lastMessageTime DESC")
    LiveData<List<Chat>> getAllChats();

    // Get chat by chatId
    @Query("SELECT * FROM chat_table WHERE chatId = :chatId LIMIT 1")
    Chat getChatByIdSync(String chatId);

    // Update the last message and last message time for a specific chat
    @Query("UPDATE chat_table SET lastMessage = :lastMessage, lastMessageTime = :lastMessageTime WHERE chatId = :chatId")
    void updateChatLastMessage(String chatId, String lastMessage, long lastMessageTime);


    // Update the status of a specific message in the chat
    @Query("UPDATE message_table SET status = :status WHERE messageId = :messageId")
    void updateMessageStatus(String messageId, String status);

    // Get all messages for a specific chat
    @Query("SELECT * FROM message_table WHERE chatId = :chatId ORDER BY timestamp ASC")
    LiveData<List<Message>> getMessagesForChat(String chatId);

    // Get new messages for a specific chat since the last known timestamp
    @Query("SELECT * FROM message_table WHERE chatId = :chatId AND timestamp > :lastKnownTimestamp ORDER BY timestamp ASC")
    LiveData<List<Message>> getNewMessages(String chatId, long lastKnownTimestamp);

    // Get all chats for a specific persona
    @Query("SELECT * FROM chat_table WHERE personaId = :personaId")
    LiveData<List<Chat>> getAllChatsForPersona(String personaId);

    // Update chat status (e.g., typing, idle)
    @Query("UPDATE chat_table SET status = :status WHERE chatId = :chatId")
    void updateChatStatus(String chatId, String status);

    // Delete a chat (including all associated messages)
    @Query("DELETE FROM chat_table WHERE chatId = :chatId")
    void deleteChat(String chatId);

    // Delete all chats for a specific persona
    @Query("DELETE FROM chat_table WHERE personaId = :personaId")
    void deleteAllChatsForPersona(String personaId);

    // Get all messages with a specific status (e.g., "waiting_for_response")
    @Query("SELECT * FROM message_table WHERE chatId = :chatId AND status = :status ORDER BY timestamp ASC")
    LiveData<List<Message>> getMessagesByStatus(String chatId, String status);

    // Get the latest message for a specific chat
    @Query("SELECT * FROM message_table WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    Message getLastMessageForChat(String chatId);
}
