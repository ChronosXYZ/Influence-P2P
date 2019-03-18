package io.github.chronosx88.influence.contracts.chatlist;

import java.util.List;

import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public interface ChatListLogicContract {
    List<ChatEntity> loadAllChats();
    void createChatBySender(ChatEntity entity);
}
