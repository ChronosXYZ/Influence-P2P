package io.github.chronosx88.influence.presenters;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.chatactivity.IChatLogicContract;
import io.github.chronosx88.influence.contracts.chatactivity.IChatPresenterContract;
import io.github.chronosx88.influence.contracts.chatactivity.IChatViewContract;
import io.github.chronosx88.influence.contracts.observer.IObserver;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.logic.ChatLogic;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class ChatPresenter implements IChatPresenterContract, IObserver {
    private IChatLogicContract logic;
    private IChatViewContract view;
    private ChatEntity chatEntity;
    private String chatID;
    private Gson gson;

    public ChatPresenter(IChatViewContract view, String chatID) {
        this.logic = new ChatLogic(LocalDBWrapper.getChatByChatID(chatID));
        this.view = view;
        this.chatID = chatID;
        this.chatEntity = LocalDBWrapper.getChatByChatID(chatID);

        AppHelper.getObservable().register(this);
        gson = new Gson();
    }

    @Override
    public void sendMessage(String text) {
        MessageEntity message = LocalDBWrapper.createMessageEntry(NetworkActions.TEXT_MESSAGE, UUID.randomUUID().toString(), chatID, AppHelper.getPeerID(), AppHelper.getPeerID(), System.currentTimeMillis(), text, false, false);
        logic.sendMessage(message);
        view.updateMessageList(message);
    }

    @Override
    public void handleEvent(JsonObject object) {
        switch (object.get("action").getAsInt()) {
            case UIActions.MESSAGE_RECEIVED: {
                MessageEntity messageEntity = LocalDBWrapper.getMessageByID(object.get("additional").getAsString());
                view.updateMessageList(messageEntity);
            }
        }
    }

    @Override
    public void updateAdapter() {
        List<MessageEntity> entities = LocalDBWrapper.getMessagesByChatID(chatID);
        view.updateMessageList(entities == null ? new ArrayList<>() : entities);
    }

    @Override
    public void onDestroy() {
        logic.stopTrackingForNewMsgs();
    }
}
