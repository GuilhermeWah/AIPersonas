package com.example.aipersonas.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

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

    @Query("SELECT * FROM chat_table WHERE personaId = :personaId ORDER BY timestamp ASC")
    LiveData<List<Chat>> getChatsForPersona(int personaId);

    @Query("SELECT * FROM chat_table")
    LiveData<List<Chat>> getAllChats();
}
