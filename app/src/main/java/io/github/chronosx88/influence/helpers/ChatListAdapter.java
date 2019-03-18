package io.github.chronosx88.influence.helpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {
    List<ChatEntity> chatList = new ArrayList<>();

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);
        return new ChatListViewHolder(view);
    }

    public void setChatList(List<ChatEntity> entities) {
        chatList.clear();
        chatList.addAll(entities);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        holder.chatName.setText(chatList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ChatListViewHolder extends RecyclerView.ViewHolder {
        TextView chatName;

        public ChatListViewHolder(View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.chat_name);
        }
    }
}
