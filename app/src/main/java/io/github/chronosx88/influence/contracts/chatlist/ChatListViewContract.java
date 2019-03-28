package io.github.chronosx88.influence.contracts.chatlist;

import android.content.Intent;

import java.util.List;

import io.github.chronosx88.influence.helpers.ChatListAdapter;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public interface ChatListViewContract {
    void setRecycleAdapter(ChatListAdapter adapter);
    void startActivity(Intent intent);
    void updateChatList(ChatListAdapter adapter, List<ChatEntity> chats);
}
