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

import com.example.aipersonas.viewmodels.ChatListViewModel;
import com.example.aipersonas.viewmodels.ChatListViewModelFactory;
import com.example.aipersonas.viewmodels.PersonaViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import java.util.Date;

import com.example.aipersonas.R;
import com.example.aipersonas.adapters.ChatListAdapter;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Persona;
import com.google.firebase.auth.FirebaseAuth;

public class ChatListActivity extends AppCompatActivity implements ChatListAdapter.OnChatClickListener {

    private RecyclerView chatListRecyclerView;
    private ChatListViewModel chatListViewModel;
    private PersonaViewModel personaViewModel;
    private ChatListAdapter chatListAdapter;
    private Persona currentPersona;
    private FloatingActionButton addChatButton;
    private FirebaseAuth auth;

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

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        chatListRecyclerView = findViewById(R.id.chat_list_recyclerview);
        chatListAdapter = new ChatListAdapter(this);
        chatListRecyclerView.setAdapter(chatListAdapter);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addChatButton = findViewById(R.id.add_chat_fab);

        // Get personaId from the intent
        String personaId = null;

        if (getIntent().hasExtra("personaId")) {
            personaId = getIntent().getStringExtra("personaId");

            // Use ChatViewModelFactory to create ChatViewModel
            ChatListViewModelFactory factory = new ChatListViewModelFactory(getApplication(), personaId);
            chatListViewModel = new ViewModelProvider(this, factory).get(ChatListViewModel.class);

            personaViewModel = new ViewModelProvider(this).get(PersonaViewModel.class);
            personaViewModel.getPersonaById(personaId).observe(this, persona -> {
                if (persona != null) {
                    currentPersona = persona;
                    Log.d("ChatListActivity", "Current Persona set: " + currentPersona.getName());
                } else {
                    Toast.makeText(this, "Failed to load persona details.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Add log here to verify if personaId is received correctly
        Log.d("ChatListActivity", "Persona ID received: " + personaId);

        if (personaId != null) {
            chatListViewModel.getChatsForPersona(personaId).observe(this, chats -> {
                chatListAdapter.setChatList(chats);
            });
        } else {
            // Handle null personaId scenario
            Toast.makeText(this, "Persona not found", Toast.LENGTH_SHORT).show();
        }

        addChatButton.setOnClickListener(v -> {
            if (currentPersona != null) {
                // Create a new chat here
                Chat newChat = new Chat(
                        userId,
                        currentPersona.getPersonaId(),
                        currentPersona.getName(),
                        "New Chat",                       // lastMessage
                        Timestamp.now(), // lastMessageTime (using current time)
                        true,                             // isActive (e.g., true to indicate active chat)
                        "active"                          // status (e.g., "active" or some other appropriate status)
                );
                chatListViewModel.insert(newChat);

                Toast.makeText(ChatListActivity.this, "Chat created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChatListActivity.this, "Persona not found", Toast.LENGTH_SHORT).show();
            }
        });
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
    // We are using the interface ChatListAdapter.OnChatClickListener to handle this
    @Override
    public void onChatClick(Chat chat) {
        // logic to open the selected chat
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chat.getChatId()); // Pass the chatId to the ChatActivity
        intent.putExtra("personaId", chat.getPersonaId()); // Pass the personaId to the ChatActivity
        startActivity(intent);
    }
}
