package com.example.aipersonas.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aipersonas.models.Persona;

import java.util.List;

@Dao
public interface PersonaDAO {

    @Insert
    void insert(Persona persona);

    @Update
    void update(Persona persona);

    @Delete
    void delete(Persona persona);


    // is used to hold and observe all the personas in the Room database.
    // not sure if it is a good solution, however it will be responsible for updating
    // the UI whenever the data changes (automatically)
    @Query("SELECT * FROM persona_table ORDER BY name ASC")
    LiveData<List<Persona>> getAllPersonas();
}
