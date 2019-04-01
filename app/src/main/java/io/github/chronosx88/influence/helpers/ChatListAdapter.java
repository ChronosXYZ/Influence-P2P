package io.github.chronosx88.influence.helpers;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.IItemClickListener;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {
    List<ChatEntity> chatList = new ArrayList<>();
    public int onClickPosition = -1;
    private IItemClickListener itemClickListener;

    public ChatListAdapter(IItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

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

    public ChatEntity getItem(int position) {
        return chatList.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        holder.chatName.setText(chatList.get(position).name);
        holder.onLongClick(position);
    }

    public ChatEntity getChatEntity(int position) {
        return chatList.get(position);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ChatListViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView chatName;

        public ChatListViewHolder(View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.chat_name);
            itemView.setOnCreateContextMenuListener(this);
            itemView.setOnClickListener((v) -> {
                itemClickListener.onItemClick(v, getAdapterPosition());
            });
        }

        public void onLongClick(int position) {
            itemView.setOnLongClickListener((v) -> {
                onClickPosition = position;
                itemView.showContextMenu();
                return true;
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, 0, 0, "Remove chat");
        }
    }
}
