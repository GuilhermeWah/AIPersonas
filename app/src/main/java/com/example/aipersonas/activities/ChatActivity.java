package com.example.aipersonas.activities;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.adapters.MessageAdapter;
import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.models.Message;
import com.example.aipersonas.repositories.ChatRepository;
import com.example.aipersonas.viewmodels.ChatViewModel;
import com.example.aipersonas.viewmodels.ChatViewModelFactory;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private ChatViewModel chatViewModel;
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private ImageView sendButton;
    private EditText messageInput;
    private String chatId;
    private String personaId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        sendButton = findViewById(R.id.sendMessageButton);
        messageInput = findViewById(R.id.editTextMessage);
        messageRecyclerView = findViewById(R.id.messageRecyclerView);

        // Retrieve personaId and chatId from intent
        personaId = getIntent().getStringExtra("personaId");
        chatId = getIntent().getStringExtra("chatId");

        if (personaId == null || chatId == null) {
            Toast.makeText(this, "Invalid Persona or Chat ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up RecyclerView
        messageAdapter = new MessageAdapter(this, new ArrayList<>());
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageRecyclerView.setAdapter(messageAdapter);

        // Initialize Repository and ViewModel using Factory
        ChatRepository chatRepository = new ChatRepository(ChatDatabase.getInstance(getApplication()).chatDao());
        ChatViewModelFactory factory = new ChatViewModelFactory(getApplication(), chatRepository, personaId, chatId);
        chatViewModel = new ViewModelProvider(this, factory).get(ChatViewModel.class);

        // Observe messages for the current chat
        observeMessages();

        // Handle send button click
        sendButton.setOnClickListener(v -> {
            String messageContent = messageInput.getText().toString();
            if (!messageContent.isEmpty()) {
                // Create a Message object
                Message userMessage = new Message(chatId, personaId, messageContent, Timestamp.now(), "sent");

                // Send message through ViewModel
                chatViewModel.sendMessage(userMessage);

                // Clear the input field
                messageInput.setText("");
            }
        });
    }

    private void observeMessages() {
        chatViewModel.getMessagesForChat().observe(this, messages -> {
            messageAdapter.updateMessages(messages);
            if (!messages.isEmpty()) {
                messageRecyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        });
    }
}
