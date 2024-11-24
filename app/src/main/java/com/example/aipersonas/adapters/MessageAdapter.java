package com.example.aipersonas.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messageList;
    private Context context;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList != null ? messageList : new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if ("sent".equals(message.getStatus())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        Log.d("MessageAdapter", "Binding message: " + message.getMessageContent());
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
            Log.d("MessageAdapter", "Binding Sent Message: " + message.getMessageContent());

        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
            Log.d("MessageAdapter", "Binding Received Message: " + message.getMessageContent());

        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ** Add a New Message and Notify the Adapter **
    // as I mentioned in the chatActivity, this is the best approach because it avoids the full
    // refresh. This way we avoid unnecessary calls to setMessages() and notifyDataSetChanged()
    public void addMessage(Message message) {
        if (message != null) {
            Log.d("MessageAdapter", "Adding message: " + message.getMessageContent());
            messageList.add(message);
            notifyItemInserted(messageList.size() - 1); // Notify RecyclerView of the new item
        }
    }


    // ViewHolder for Sent Messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageContent;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.text_message_sent);
        }

        void bind(Message message) {
            messageContent.setText(message.getMessageContent());
        }
    }

    // ViewHolder for Received Messages
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageContent;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.text_message_received);
        }

        void bind(Message message) {
            messageContent.setText(message.getMessageContent());
        }
    }
}
