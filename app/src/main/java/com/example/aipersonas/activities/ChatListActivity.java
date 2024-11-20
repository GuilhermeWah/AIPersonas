package com.example.aipersonas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.adapters.ChatListAdapter;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Persona;
import com.example.aipersonas.viewmodels.ChatViewModel;
import com.example.aipersonas.viewmodels.ChatViewModelFactory;
import com.example.aipersonas.viewmodels.PersonaViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

public class ChatListActivity extends AppCompatActivity implements ChatListAdapter.OnChatClickListener {

    private static final String TAG = "ChatListActivity";
    private RecyclerView chatListRecyclerView;
    private ChatViewModel chatViewModel;
    private PersonaViewModel personaViewModel;
    private ChatListAdapter chatListAdapter;
    private Persona currentPersona;
    private FloatingActionButton addChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_list_activity);

        setupToolbar();

        String personaId = getIntent().getStringExtra("personaId");
        if (personaId == null) {
            Toast.makeText(this, "Persona not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();
        setupViewModels(personaId);
        setupAddChatButton(personaId);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.chat_list_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Persona Chats");
        }
    }

    private void setupRecyclerView() {
        chatListRecyclerView = findViewById(R.id.chat_list_recyclerview);
        chatListAdapter = new ChatListAdapter(this);
        chatListRecyclerView.setAdapter(chatListAdapter);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupViewModels(String personaId) {
        ChatViewModelFactory factory = new ChatViewModelFactory(getApplication(), personaId);
        chatViewModel = new ViewModelProvider(this, factory).get(ChatViewModel.class);

        personaViewModel = new ViewModelProvider(this).get(PersonaViewModel.class);
        personaViewModel.getPersonaById(personaId).observe(this, persona -> {
            if (persona != null) {
                currentPersona = persona;
                Log.d(TAG, "Current Persona set: " + currentPersona.getName());
            } else {
                Toast.makeText(this, "Failed to load persona details.", Toast.LENGTH_SHORT).show();
            }
        });

        chatViewModel.getChatsForPersona(personaId).observe(this, chats -> {
            chatListAdapter.setChatList(chats);
        });
    }

    private void setupAddChatButton(String personaId) {
        addChatButton = findViewById(R.id.add_chat_fab);
        addChatButton.setOnClickListener(v -> {
            if (currentPersona != null) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String userId = auth.getCurrentUser().getUid();

                Chat newChat = new Chat(userId, currentPersona.getPersonaId(), currentPersona.getName(), "New Chat", new Timestamp(new Date()));
                chatViewModel.insert(newChat, personaId);

                Toast.makeText(ChatListActivity.this, "Chat added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChatListActivity.this, "No persona selected. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChatClick(Chat chat) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chat.getChatId());
        intent.putExtra("personaId", chat.getPersonaId());
        startActivity(intent);
    }
}
