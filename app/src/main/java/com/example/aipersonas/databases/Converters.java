package com.example.aipersonas.databases;

import androidx.room.TypeConverter;
import com.google.firebase.Timestamp;

public class Converters {

    @TypeConverter
    public static Timestamp fromLong(Long value) {
        return value == null ? null : new Timestamp(new java.util.Date(value));
    }

    @TypeConverter
    public static Long toLong(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toDate().getTime();
    }
}
