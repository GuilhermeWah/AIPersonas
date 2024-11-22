package com.example.aipersonas.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aipersonas.models.Persona;

import java.util.List;

@Dao
public interface PersonaDAO {

    //stack overflow on conflict strategy:
    //https://stackoverflow.com/questions/56231855/room-database-onconflict-onconflictstrategy-replace-not-working
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Persona persona);

    @Update
    void update(Persona persona);

    @Delete
    void delete(Persona persona);

    @Query("DELETE FROM persona_table")
    void deleteAll();

    @Query("SELECT * FROM persona_table WHERE personaId = :personaId")
    LiveData<Persona> getPersonaById(String personaId);

    //Get persona by id syncronously
    @Query("SELECT * FROM persona_table WHERE personaId = :personaId LIMIT 1")
    Persona getPersonaByIdSync(String personaId);


    // is used to hold and observe all the personas in the Room database.
    // not sure if it is a good solution, however it will be responsible for updating
    // the UI whenever the data changes (automatically)

    @Query("SELECT * FROM persona_table ORDER BY name ASC")
    LiveData<List<Persona>> getAllPersonas();


}
