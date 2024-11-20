package com.example.aipersonas.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;

import java.util.List;

@Dao
public interface ChatDAO {

    /**
     *   CHAT RELATED OPERATIONS
     *
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Chat chat);

    @Update
    void update(Chat chat);

    @Delete
    void delete(Chat chat);

    // Deletes all chats for a specific user to avoid duplication
    @Query("DELETE FROM chat_table WHERE userId = :userId")
    void deleteAllChatsForUser(String userId);

    @Query("SELECT * FROM chat_table WHERE chatId = :chatId")
    Chat getChatById(String chatId);

    // Retrieves all chats for a specific user ordered by timestamp in descending order
    @Query("SELECT * FROM chat_table WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<Chat>> getAllChats(String userId);

    // Retrieves all chats for a specific persona ordered by timestamp in descending order
    @Query("SELECT * FROM chat_table WHERE personaId = :personaId ORDER BY timestamp DESC")
    LiveData<List<Chat>> getChatsForPersona(String personaId);
    /**
     *
     *   MESSAGE RELATED OPERATIONS
     *
     */
    // Retrieves all messages for a specific chat ordered by timestamp in ascending order
    @Query("SELECT * FROM message_table WHERE chatId = :chatId ORDER BY timestamp ASC")
    LiveData<List<Message>> getMessagesForChat(String chatId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(Message message);





}
