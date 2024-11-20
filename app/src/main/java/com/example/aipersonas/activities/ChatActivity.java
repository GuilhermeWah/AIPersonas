package com.example.aipersonas.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.aipersonas.R;
import com.example.aipersonas.viewmodels.ChatListViewModel;
import com.example.aipersonas.viewmodels.ChatViewModelFactory;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private ChatListViewModel chatListViewModel;
    private ImageButton sendButton;
    private EditText messageInput;
    private String chatId;
    private String personaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeViews();
        retrieveIntentData();

        if (personaId == null || chatId == null) {
            Toast.makeText(this, "Invalid Chat or Persona ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViewModel();
        observeChats();
        setupSendMessageListener();
    }

    private void initializeViews() {
        sendButton = findViewById(R.id.buttonSendMessage);
        messageInput = findViewById(R.id.editTextMessage);
    }

    private void retrieveIntentData() {
        chatId = getIntent().getStringExtra("chatId");
        personaId = getIntent().getStringExtra("personaId");

        if (personaId == null || chatId == null) {
            Log.e(TAG, "Missing personaId or chatId in intent");
        }
    }

    private void initializeViewModel() {
        ChatViewModelFactory factory = new ChatViewModelFactory(getApplication(), personaId);
        chatListViewModel = new ViewModelProvider(this, factory).get(ChatListViewModel.class);
    }

    private void observeChats() {
        chatListViewModel.getChatsForPersona(personaId).observe(this, chats -> {
            // TODO: Update UI with chat data
            Log.d(TAG, "Chats observed: " + chats.size());
        });
    }

    private void setupSendMessageListener() {
        sendButton.setOnClickListener(view -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                messageInput.setText(""); // Clear input
                sendMessage(message);
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String message) {
        chatListViewModel.sendMessageToGPT(message, personaId, chatId);
        Log.d(TAG, "Message sent: " + message);
    }
}
