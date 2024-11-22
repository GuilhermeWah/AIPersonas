package com.example.aipersonas.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.example.aipersonas.models.Persona;
import com.example.aipersonas.models.User;

@Database(entities = {Persona.class, Chat.class, User.class, Message.class}, version = 18, exportSchema = false)
@TypeConverters({Converters.class}) //we created this, because Room doesn't support Timestamp from firebase
public abstract class ChatDatabase extends RoomDatabase {

    private static ChatDatabase instance;

    public abstract PersonaDAO personaDao();
    public abstract ChatDAO chatDao();
    public abstract UserDAO userDao();

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
