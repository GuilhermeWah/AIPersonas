package com.example.aipersonas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.models.Chat;

import java.util.ArrayList;
import java.util.List;

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
        holder.personaTitleTextView.setText(chat.getPersonaTitle());
        holder.lastMessageTextView.setText(chat.getLastMessage());
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

    public static class ChatListViewHolder extends RecyclerView.ViewHolder {
        private TextView personaTitleTextView;
        private TextView lastMessageTextView;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            personaTitleTextView = itemView.findViewById(R.id.chatTitle);
            lastMessageTextView = itemView.findViewById(R.id.lastMessage);
        }
    }
}
