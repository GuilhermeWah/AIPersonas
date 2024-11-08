package com.example.aipersonas.adapters;

import android.annotation.SuppressLint;
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

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList = new ArrayList<>(); // this approach avoids  null issues
    /*

     */
    private OnChatClickListener chatClickListener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(OnChatClickListener chatClickListener) {
        this.chatList = chatList;
        this.chatClickListener = chatClickListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list_bubble, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.personaTitle.setText(chat.getPersonaTitle());
        holder.lastMessage.setText(chat.getLastMessage());
        holder.itemView.setOnClickListener(v -> chatClickListener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    /* new version for setChatList (I have to test it tho)
    @SuppressLint("NotifyDataSetChanged")
    public void setChatList(List<Chat> chats) {
        this.chatList = chats;
        notifyDataSetChanged();  // Notifies the adapter that the data has changed
    }
    */
    @SuppressLint("NotifyDataSetChanged")
    public void setChatList(List<Chat> chats) {
        if (chats != null) {
            this.chatList.clear();
            this.chatList.addAll(chats);
            notifyDataSetChanged();  // Notify the adapter about data change
        }
    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView personaTitle;
        private TextView lastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            personaTitle = itemView.findViewById(R.id.personaTitle);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}
