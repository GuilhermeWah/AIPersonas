package com.example.aipersonas.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aipersonas.models.Chat;
import java.util.List;

@Dao
public interface ChatDAO {

    @Insert
    void insert(Chat chat);

    @Update
    void update(Chat chat);

    @Delete
    void delete(Chat chat);

    /*
    @Query("SELECT * FROM chat_table WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<Chat>> getChatsForPersona(int personaId);
    */

    @Query("SELECT * FROM chat_table WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<Chat>> getAllChats(String userId);
}
