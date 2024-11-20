package com.example.aipersonas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.models.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messages;
    private final String currentUserId;

    // Constructor
    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 0 ? R.layout.item_message_sent : R.layout.item_message_received, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageTextView.setText(message.getContent()); // Use `getContent()` from Message
        holder.timestampTextView.setText(formatTimestamp(message.getTimestamp().toDate().getTime())); // Convert Firestore Timestamp
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Return 0 for messages sent by the user, 1 for received messages
        return messages.get(position).getSenderId().equals(currentUserId) ? 0 : 1;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timestampTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.textMessage);
            timestampTextView = itemView.findViewById(R.id.textTimestamp);
        }
    }

    // Helper method to format the timestamp
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }
}
