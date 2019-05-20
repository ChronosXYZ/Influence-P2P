package io.github.chronosx88.influence.logic;

import java.util.List;

import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class ChatListLogic implements CoreContracts.IDialogListLogicContract {

    @Override
    public List<ChatEntity> loadAllChats() {
        return AppHelper.getChatDB().chatDao().getAllChats();
    }
}
