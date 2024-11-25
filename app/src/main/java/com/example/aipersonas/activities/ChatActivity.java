package com.example.aipersonas.activities;

import static android.content.ContentValues.TAG;
import static com.example.aipersonas.R.layout.activity_chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.adapters.MessageAdapter;
import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.models.Chat;
import com.example.aipersonas.models.Message;
import com.example.aipersonas.repositories.ChatRepository;
import com.example.aipersonas.repositories.GPTRepository;
import com.example.aipersonas.viewmodels.ChatViewModel;
import com.example.aipersonas.viewmodels.ChatViewModelFactory;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ChatViewModel chatViewModel;
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private ImageView sendButton;
    private EditText messageInput;
    private String chatId;
    private String personaId;
    private String userId;
    private List<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_chat);

        // Initialize views
        sendButton = findViewById(R.id.sendMessageButton);
        messageInput = findViewById(R.id.editTextMessage);
        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        TextView personaNameTextView = findViewById(R.id.personaNameTextView);

        // Retrieve personaId, chatId, and personaName from intent
        personaId = getIntent().getStringExtra("personaId");
        chatId = getIntent().getStringExtra("chatId");
        String personaName = getIntent().getStringExtra("personaName");

        if (personaId == null || chatId == null) {
            Toast.makeText(this, "Invalid Persona or Chat ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (personaName != null) {
            // Update the TextView with the persona name
            personaNameTextView.setText(personaName);
        } else {
            Log.e(TAG, "Invalid Persona Name.");
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up RecyclerView
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageRecyclerView.setAdapter(messageAdapter);

        // Initialize ViewModel
        ChatRepository chatRepository = new ChatRepository(ChatDatabase.getInstance(getApplication()).chatDao());
        ChatViewModelFactory factory = new ChatViewModelFactory(getApplication(), chatRepository, personaId, chatId);
        ChatViewModel chatViewModel = new ViewModelProvider(this, factory).get(ChatViewModel.class);
        this.chatViewModel = chatViewModel;

        // Observe messages for the current chat
        observeMessages();

        // Handle send button click
        sendButton.setOnClickListener(v -> {
            String messageContent = messageInput.getText().toString();
            if (!messageContent.isEmpty()) {
                // Create a Message object
                Message userMessage = new Message(
                        chatId,
                        personaId,
                        messageContent,
                        Timestamp.now(),
                        "sent"
                );

                // Send message through ViewModel
                chatViewModel.sendMessage(userMessage);

                // Send message to GPT
                chatViewModel.sendMessageToGPT(messageContent, new GPTRepository.ApiCallback() {
                    @Override
                    public void onSuccess(String gptResponse) {
                        // Create GPT's response as a Message object
                        Message gptMessage = new Message(
                                chatId,
                                FirebaseAuth.getInstance().getCurrentUser().getUid(), // Sender ID for GPT
                                gptResponse, // GPT's response content
                                Timestamp.now(), // Use Firebase Timestamp for consistency
                                "received" // Message status
                        );

                        // Save GPT's message through ViewModel
                        chatViewModel.sendMessage(gptMessage);

                        // Update RecyclerView with GPT's response
                        runOnUiThread(() -> messageAdapter.addMessage(gptMessage));
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "GPT Error: " + error);
                    }
                });

                // Clear the input field
                messageInput.setText("");
            }
        });
    }

    private void observeMessages() {
        chatViewModel.getMessagesForChat().observe(this, messages -> {
            messageAdapter.updateMessages(messages);
            messageRecyclerView.setAdapter(messageAdapter);
            Log.d("ChatActivity", "Messages size: " + messages.size());

            if (messages != null && !messages.isEmpty()) {
                messageRecyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        });
    }
}
