package com.example.aipersonas.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aipersonas.R;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve the chatId from the intent and use it to load chat messages
        int chatId = getIntent().getIntExtra("chatId", -1);

        // TODO: Load messages for the specific chat using the chatId
    }
}
