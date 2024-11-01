package com.example.aipersonas.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aipersonas.R;
import com.google.android.material.snackbar.Snackbar;

public class ChatActivity extends AppCompatActivity {

    private ImageButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sendButton = findViewById(R.id.buttonSendMessage);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve the chatId from the intent and use it to load chat messages
        int chatId = getIntent().getIntExtra("chatId", -1);
        // TODO: Load messages for the specific chat using the chatId


        sendButton.setOnClickListener(v -> {
            if (!isOnline()) {
                Snackbar.make(findViewById(R.id.chatLayout), "You need an internet connection to send messages.", Snackbar.LENGTH_LONG).show();
            } else {
                // Proceed with sending the message
                // TODO: Implement sending the messagesendMessage();
            }
        });


    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void setupConnectivityListener() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest request = new NetworkRequest.Builder().build();

        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> sendButton.setEnabled(true)); // Enable send button when online
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> sendButton.setEnabled(false)); // Disable send button when offline
            }
        });
    }
}


