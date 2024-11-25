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

/**
 * Adapter for displaying messages in a chat.
 *
 * This adapter handles different types of messages, such as sent, received, and typing indicators.
 * It ensures efficient updates by using granular methods to notify the RecyclerView about changes,
 * thereby improving performance compared to the previous approach of using notifyDataSetChanged() for all updates.
 */

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

    /**
     * Previous Approach:
     * - Initially, we used notifyDataSetChanged() for all types of updates, including adding, updating, or deleting messages.
     * - This approach led to inefficiencies, as notifyDataSetChanged() refreshes the entire RecyclerView, even for small changes.
     *
     * Current Changes:
     * - We introduced more granular methods like notifyItemInserted(), notifyItemChanged(), and notifyItemRemoved().
     * - These changes aim to optimize UI updates by only notifying about the specific parts of the list that have changed.
     * - This reduces the overhead of refreshing the entire RecyclerView and results in a smoother user experience.
     */

    /**
     * Updates the entire message list in the adapter.
     *
     * This method replaces the entire dataset with a new list of messages and then notifies the RecyclerView to refresh all items.
     * It is useful when a complete refresh is necessary, but it can be inefficient for small changes.
     *
     * @param messages List of updated messages.
     */
    public void updateMessages(List<Message> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    /**
     * Adds a new message to the adapter.
     *
     * This method adds a single new message to the dataset and notifies the adapter to refresh only the newly added item.
     * This is more efficient compared to refreshing the entire list.
     *
     * @param message The message to be added.
     */
    public void addMessage(Message message) {
        if (messageList != null) {
            messageList.add(message);
            notifyItemInserted(messageList.size() - 1);
        } else {
            Log.e("MessageAdapter", "Message list is null. Cannot add message.");
        }
    }

    /**
     * Updates a specific message in the adapter.
     *
     * This method finds the specific message in the list and updates it, notifying the adapter to refresh only the changed item.
     * This allows for more efficient updates by targeting only the affected item.
     *
     * @param message The message to be updated.
     */
    public void updateMessage(Message message) {
        int position = findMessagePosition(message);
        if (position != -1) {
            messageList.set(position, message);
            notifyItemChanged(position);
        }
    }

    /**
     * Removes a specific message from the adapter.
     *
     * This method removes a message from the dataset and notifies the adapter to refresh only the removed item.
     * This improves performance by avoiding a complete refresh of the list.
     *
     * @param message The message to be removed.
     */
    public void removeMessage(Message message) {
        int position = findMessagePosition(message);
        if (position != -1) {
            messageList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Helper method to find the position of a message in the list.
     *
     * This method iterates through the message list to find the position of the given message.
     *
     * @param message The message whose position needs to be found.
     * @return The position of the message in the list, or -1 if not found.
     */
    private int findMessagePosition(Message message) {
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getMessageId().equals(message.getMessageId())) {
                return i;
            }
        }
        return -1;
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

        /**
         * Binds a sent message to the ViewHolder.
         *
         * @param message The message to be bound to the view.
         */
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

        /**
         * Binds a received message to the ViewHolder.
         *
         * @param message The message to be bound to the view.
         */
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

        /**
         * Binds the typing indicator to the ViewHolder.
         * Displays a "typing..." message to indicate that the other user is typing.
         */
        void bind() {
            typingIndicatorTextView.setText(context.getString(R.string.typing));
        }
    }

    /**
     * Helper method to format the timestamp for messages.
     *
     * This method formats the timestamp into a human-readable string.
     *
     * @param timestamp The timestamp to be formatted.
     * @return The formatted timestamp as a string.
     */
    private String formatTimestamp(long timestamp) {
        // Your logic to format the timestamp
        return String.valueOf(timestamp);
    }
}
