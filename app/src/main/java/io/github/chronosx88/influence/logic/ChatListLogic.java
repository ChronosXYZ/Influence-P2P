package io.github.chronosx88.influence.logic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.github.chronosx88.influence.contracts.chatlist.ChatListLogicContract;
import io.github.chronosx88.influence.contracts.observer.Observer;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.NetworkActions;
import io.github.chronosx88.influence.helpers.UIActions;
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
    public void handleEvent(JSONObject object) {
        try {
            switch ((int) object.get("action")) {
                case NetworkActions.START_CHAT: {
                    createChatBySender(new ChatEntity(object.getString("chatID"), object.getString("name"), ""));
                    AppHelper.getObservable().notifyObservers(new JSONObject().put("action", UIActions.NEW_CHAT), MainObservable.UI_ACTIONS_CHANNEL);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createChatBySender(ChatEntity entity) {
        AppHelper.getChatDB().chatDao().addChat(entity);
    }
}
