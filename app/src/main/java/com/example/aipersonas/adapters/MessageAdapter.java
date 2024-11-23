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
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_TYPING = 3;

    private List<Message> messageList;
    private String currentUserId;
    private Context context;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.messageList = messageList;
        this.context = context;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);

        if (message.getStatus() != null && message.getStatus().equals("typing")) {
            return VIEW_TYPE_TYPING;
        } else if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_typing, parent, false);
            return new TypingMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SENT:
                ((SentMessageViewHolder) holder).bind(message);
                break;

            case VIEW_TYPE_RECEIVED:
                ((ReceivedMessageViewHolder) holder).bind(message);
                break;

            case VIEW_TYPE_TYPING:
                // Handle typing indicator view
                ((TypingMessageViewHolder) holder).bind();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void setMessages(List<Message> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    // ViewHolder for sent messages
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView sentMessageTextView;
        TextView timestampTextView;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessageTextView = itemView.findViewById(R.id.text_message_sent);
            timestampTextView = itemView.findViewById(R.id.text_message_timestamp);
        }

        void bind(Message message) {
            sentMessageTextView.setText(message.getMessageContent());
            timestampTextView.setText(formatTimestamp(message.getTimestamp().toDate().getTime()));
        }
    }

    // ViewHolder for received messages
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView receivedMessageTextView;
        TextView timestampTextView;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedMessageTextView = itemView.findViewById(R.id.text_message_received);
            timestampTextView = itemView.findViewById(R.id.text_message_timestamp);
        }

        void bind(Message message) {
            receivedMessageTextView.setText(message.getMessageContent());
            timestampTextView.setText(formatTimestamp(message.getTimestamp().toDate().getTime()));
        }

    }

    // ViewHolder for typing indicator
    class TypingMessageViewHolder extends RecyclerView.ViewHolder {
        TextView typingIndicatorTextView;

        TypingMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            typingIndicatorTextView = itemView.findViewById(R.id.text_typing_indicator);
        }

        void bind() {
            typingIndicatorTextView.setText(context.getString(R.string.typing));
        }
    }

    // Helper method to format timestamp
    private String formatTimestamp(long timestamp) {
        // Your logic to format the timestamp
        return String.valueOf(timestamp);
    }

    public void addMessage(Message message) {
        if (messageList != null) {
            messageList.add(message);
            notifyItemInserted(messageList.size() - 1); // Notify the adapter about the newly inserted item
        } else {
            Log.e("MessageAdapter", "Message list is null. Cannot add message.");
        }
    }

}
