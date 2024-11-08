package com.example.aipersonas.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aipersonas.models.Chat;
import java.util.List;

@Dao
public interface ChatDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Chat chat);

    @Update
    void update(Chat chat);

    @Delete
    void delete(Chat chat);


    // Deletes all chats for a specific user to avoid duplication
    @Query("DELETE FROM chat_table WHERE userId = :userId")
    void deleteAllChatsForUser(String userId);


    /*
    @Query("SELECT * FROM chat_table WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<Chat>> getChatsForPersona(int personaId);
    */

    @Query("SELECT * FROM chat_table WHERE chatId = :chatId")
    Chat getChatById(String chatId);

    // Retrieves all chats for a specific user ordered by timestamp in descending order
    @Query("SELECT * FROM chat_table WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<Chat>> getAllChats(String userId);


    @Query("SELECT * FROM chat_table WHERE personaId = :personaId ORDER BY timestamp DESC")
    LiveData<List<Chat>> getChatsForPersona(String personaId);

}
