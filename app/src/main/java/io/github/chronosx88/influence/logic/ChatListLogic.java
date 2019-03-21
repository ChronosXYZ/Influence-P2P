package io.github.chronosx88.influence.logic;

import com.google.gson.JsonObject;

import java.util.List;

import io.github.chronosx88.influence.contracts.chatlist.ChatListLogicContract;
import io.github.chronosx88.influence.contracts.observer.Observer;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.observable.MainObservable;

public class ChatListLogic implements ChatListLogicContract, Observer {
    public ChatListLogic() {
        AppHelper.getObservable().register(this, MainObservable.OTHER_ACTIONS_CHANNEL);
    }

    @Override
    public List<ChatEntity> loadAllChats() {
        return AppHelper.getChatDB().chatDao().getAllChats();
    }

    @Override
    public void handleEvent(JsonObject object) {
        switch (object.get("action").getAsInt()) {
            case NetworkActions.START_CHAT: {
                createChatBySender(new ChatEntity(object.get("chatID").getAsString(), object.get("name").getAsString(), ""));
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", UIActions.NEW_CHAT);
                AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                break;
            }
        }
    }

    @Override
    public void createChatBySender(ChatEntity entity) {
        AppHelper.getChatDB().chatDao().addChat(entity);
    }
}
