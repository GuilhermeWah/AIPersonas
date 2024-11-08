package com.example.aipersonas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.adapters.ChatAdapter;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Persona;
import com.example.aipersonas.viewmodels.ChatViewModel;

public class ChatListActivity extends AppCompatActivity implements ChatAdapter.OnChatClickListener {

    private RecyclerView chatListRecyclerView;
    private ChatViewModel chatViewModel;
    private ChatAdapter chatAdapter;
    private Persona currentPersona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_list_activity);

        Toolbar toolbar = findViewById(R.id.chat_list_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Persona Chats");
        }

        chatListRecyclerView = findViewById(R.id.chat_list_recyclerview);
        chatAdapter = new ChatAdapter(this);
        chatListRecyclerView.setAdapter(chatAdapter);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get personaId from the intent
        String personaId = null;
        if (getIntent().hasExtra("personaId")) {
            personaId = getIntent().getStringExtra("personaId");
        }

        if (personaId != null) {
            chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
            chatViewModel.getChatsForPersona(personaId).observe(this, chats -> {
                chatAdapter.setChatList(chats);
            });
        } else {
            // Handle null personaId scenario
            Toast.makeText(this, "Persona not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // verify if the user clicked the back button
        if (item.getItemId() == android.R.id.home) {
            finish(); // close the current activity to go back to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Implementing the logic to open the selected chat when clicked on the chat item
    // We are using the interface ChatAdapter.OnChatClickListener to handle this
    @Override
    public void onChatClick(Chat chat) {
        // logic to open the selected chat
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chat.getChatId()); // Pass the chatId to the ChatActivity
        startActivity(intent);
    }
}
