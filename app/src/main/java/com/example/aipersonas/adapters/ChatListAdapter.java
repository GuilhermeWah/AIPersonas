package com.example.aipersonas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.models.Chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private List<Chat> chatList = new ArrayList<>();
    private OnChatClickListener chatClickListener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatListAdapter(OnChatClickListener chatClickListener) {
        this.chatClickListener = chatClickListener;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list_bubble, parent, false);
        return new ChatListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        // Set the persona name and last message
        holder.personaTitleTextView.setText(chat.getPersonaName());
        holder.lastMessageTextView.setText(chat.getLastMessage());


        // Format the timestamp and set it to the timestampTextView
        // The toDate() method converts the Timestamp to a Date object
        // That's a good idea (=
        if (chat.getLastMessageTime() != null) {
            long timestampInMillis = chat.getLastMessageTime().toDate().getTime();
            String formattedTime = formatTimestamp(timestampInMillis);
            holder.timestampTextView.setText(formattedTime);
        } else {
            holder.timestampTextView.setText("");  // Set an empty string if there's no timestamp
        }


        // Handle item click
        holder.itemView.setOnClickListener(v -> chatClickListener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void setChatList(List<Chat> chats) {
        if (chats != null) {
            this.chatList.clear();
            this.chatList.addAll(chats);
            notifyDataSetChanged();
        }
    }

    // Helper method to format timestamp
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class ChatListViewHolder extends RecyclerView.ViewHolder {
        private TextView personaTitleTextView;
        private TextView lastMessageTextView;
        private TextView timestampTextView;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            personaTitleTextView = itemView.findViewById(R.id.chatTitle);
            lastMessageTextView = itemView.findViewById(R.id.lastMessage);
            timestampTextView = itemView.findViewById(R.id.timestamp);  // Find the new timestamp TextView
        }
    }
}
