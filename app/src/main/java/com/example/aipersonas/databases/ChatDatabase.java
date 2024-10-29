package com.example.aipersonas.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Persona;

@Database(entities = {Persona.class, Chat.class}, version = 1, exportSchema = false)
public abstract class ChatDatabase extends RoomDatabase {

    private static ChatDatabase instance;

    public abstract PersonaDAO personaDao();
    public abstract ChatDAO chatDao();

    public static synchronized ChatDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ChatDatabase.class, "chat_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
