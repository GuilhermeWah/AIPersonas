package com.example.aipersonas.activities;

import static android.content.ContentValues.TAG;
import static com.example.aipersonas.R.layout.activity_chat;

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
import com.example.aipersonas.models.Message;
import com.example.aipersonas.viewmodels.ChatViewModel;
import com.example.aipersonas.viewmodels.ChatViewModelFactory;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {

    private ChatViewModel chatViewModel;
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private ImageView sendButton;
    private EditText messageInput;
    private String chatId;
    private String personaId;
    private String userId;
    private final Set<String> messageIds = new HashSet<>(); // Track added message IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_chat);

        // Initialize views
        sendButton = findViewById(R.id.sendMessageButton);
        messageInput = findViewById(R.id.editTextMessage);
        messageRecyclerView = findViewById(R.id.messageRecyclerView);

        // Retrieve personaId and chatId from intent
        personaId = getIntent().getStringExtra("personaId");
        chatId = getIntent().getStringExtra("chatId");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (personaId == null || chatId == null || userId == null) {
            Log.e(TAG, "Invalid Persona or Chat ID.");
            Toast.makeText(this, "Invalid Persona or Chat ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up RecyclerView
        messageAdapter = new MessageAdapter(this, new ArrayList<>());
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageRecyclerView.setAdapter(messageAdapter);

        // Initialize ViewModel
        ChatViewModelFactory factory = new ChatViewModelFactory(getApplication(), personaId, chatId);
        chatViewModel = new ViewModelProvider(this, factory).get(ChatViewModel.class);

        // Observe messages
        observeMessages();

        // Handle send button click
        sendButton.setOnClickListener(v -> {
            String messageContent = messageInput.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessage(messageContent);
            }
        });
    }

    private void observeMessages() {
        chatViewModel.observeMessages(chatId);
        chatViewModel.getMessagesLiveData().observe(this, messages -> {
            if (messages == null || messages.isEmpty()) {
                Log.d(TAG, "UI observer: Messages size = 0");
            } else {
                Log.d(TAG, "UI observer: Displaying " + messages.size() + " messages.");
                // Update UI here
            }
        });
    }




    private void sendMessage(String messageContent) {
        Log.d(TAG, "sendMessage called for userMessage: " + messageContent);

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
        Log.d(TAG, "sendMessage called for userMessage: " + userMessage.getMessageId());

        // Send message to GPT
        chatViewModel.sendMessageToGPT(messageContent, userMessage);

        // Clear the input field
        messageInput.setText("");
    }
}

//    private void observeMessages() {
//        chatViewModel.getMessagesForChat().observe(this, messages -> {
//            Log.d(TAG, "Observe triggered, Messages size = " + messages.size());
//            for (Message message : messages) {
//                if (!messageIds.contains(message.getMessageId())) {
//                    messageAdapter.addMessage(message);
//                    messageIds.add(message.getMessageId());
//                    Log.d(TAG, "Adding message to adapter: " + message.getMessageId());
//                }
//            }
//            // Summarization logic (unchanged)
//            if (messages.size() % 10 == 0) {
//                chatViewModel.requestSummarizationIfNeeded(chatId, this);
//            }
//        });
//    }
/**
 *  FOR OBSERVEMESSAGES, BEFORE WE WERE RESETTING THE ADAPTER EVERYTHING.
 *  TOO MUCH RESOURCES WERE BEING USED! INSTEAD OF :
 *  1-  Too many visible glitches were happening
 *  2 - Perfomance was being slow (Was triggering a full refresh every time)
 *  3 - The typing animation was disappearing (because of the binding .. )
 *  I found two different approaches:
 *  _______________
 *  notifyItemInserted(); --: triggers the adapter about the newly inserted item
 *  or creating the setMessages on the Adapter; --: triggers the adapter about the data change
 *  setMessages() --> triggers the adapter about the data change (full datachange)
 * it leads to perfomance overhead, it agains forces the RV to refresh the whole list,
 * causes scroll position loss, may disrupt the user's current scroll because of the entire thing
 * is refreshed
 *______________
 *  However, the best approach is the notifyItemInserted() because it notifies the adapter
 *  about a item-at-time, which is good for our case, since our app works with real-time data.
 *
 * ## ANOTHER BIG BUG -----  TOOK TOO MUCH TIME TO FIX :
 * LiveData Over-Triggering: When a new message is added to Room or Firestore, it triggers
 * observeMessages, which re-updates the adapter and re-adds messages.  It was causing multiple
 * responses from the GPT, the same.
 * @ToDo: Analyze, apparently it's passing the context manytimes; responseBody twice.
 *
 */

