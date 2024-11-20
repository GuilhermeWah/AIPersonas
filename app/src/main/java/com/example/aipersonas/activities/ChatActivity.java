package com.example.aipersonas.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.adapters.MessageAdapter;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.example.aipersonas.repositories.ChatRepository;
import com.example.aipersonas.viewmodels.ChatViewModel;
import com.example.aipersonas.viewmodels.ChatListViewModelFactory;
import com.example.aipersonas.viewmodels.ChatViewModelFactory;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ChatViewModel chatViewModel;
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private ImageButton sendButton;
    private EditText messageInput;
    private String chatId;
    private String personaId;
    private List<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        sendButton = findViewById(R.id.buttonSendMessage);
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

        // Set up RecyclerView
        // @TODO: Analyze the architecture. Should we be opening this object everytime? fbAuth
        messageAdapter = new MessageAdapter(new ArrayList<>(), FirebaseAuth.getInstance().getUid());
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageRecyclerView.setAdapter(messageAdapter);

        // Initialize ViewModel
        ChatViewModelFactory factory = new ChatViewModelFactory(getApplication(), personaId, chatId);
        ChatViewModel chatViewModel = new ViewModelProvider(this, factory).get(ChatViewModel.class);
        this.chatViewModel = chatViewModel;

        // Observe messages for the current chat
        observeMessages();

        // Handle send button click
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText(""); // Clear input after sending
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeMessages() {
        chatViewModel.getMessagesForChat(chatId).observe(this, messages -> {

            messageRecyclerView.setAdapter(messageAdapter);
            Log.d("ChatActivity", "Messages size: " + messages.size());
            //was crashing idk why

            if (messages != null && !messages.isEmpty()) {
                messageRecyclerView.smoothScrollToPosition(messages.size() - 1);
            }


        });
    }



    private void sendMessage(String messageContent) {
        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setPersonaId(personaId);
        chat.setUserId(chatViewModel.getUserId()); // Assume ViewModel provides the current user ID
        chat.setLastMessage(messageContent);
        chat.setTimestamp(Timestamp.now());

        // Save message via ViewModel
        chatViewModel.insert(chat, personaId);

        //
        // chatViewModel.sendMessageToGPT(messageContent, personaId, chatId, apiKey, callback);
    }
}
