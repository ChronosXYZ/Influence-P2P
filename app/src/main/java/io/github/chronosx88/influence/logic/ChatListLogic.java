package io.github.chronosx88.influence.logic;

import java.util.List;

import io.github.chronosx88.influence.contracts.chatlist.IChatListLogicContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class ChatListLogic implements IChatListLogicContract {

    @Override
    public List<ChatEntity> loadAllChats() {
        return AppHelper.getChatDB().chatDao().getAllChats();
    }
}
