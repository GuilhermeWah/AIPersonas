package com.example.aipersonas.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.aipersonas.models.User;

@Dao
public interface UserDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Query("SELECT * FROM user_table WHERE userId = :userId LIMIT 1")
    LiveData<User> getUserById(String userId);

    @Query("DELETE FROM user_table WHERE userId = :userId")
    void deleteById(String userId);
}
