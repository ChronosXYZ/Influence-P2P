package io.github.chronosx88.influence.presenters;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.tomp2p.peers.PeerAddress;

import java.util.ArrayList;
import java.util.List;

import io.github.chronosx88.influence.contracts.chatactivity.ChatLogicContract;
import io.github.chronosx88.influence.contracts.chatactivity.ChatPresenterContract;
import io.github.chronosx88.influence.contracts.chatactivity.ChatViewContract;
import io.github.chronosx88.influence.contracts.observer.Observer;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.helpers.MessageTypes;
import io.github.chronosx88.influence.helpers.Serializer;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.logic.ChatLogic;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class ChatPresenter implements ChatPresenterContract, Observer {
    private ChatLogicContract logic;
    private ChatViewContract view;
    private PeerAddress receiverAddress;
    private String chatID;
    private Gson gson;

    public ChatPresenter(ChatViewContract view, String chatID) {
        this.logic = new ChatLogic();
        this.view = view;
        this.chatID = chatID;
        this.receiverAddress = (PeerAddress) Serializer.deserialize(LocalDBWrapper.getChatByChatID(chatID).getPeerAddress());
        AppHelper.getObservable().register(this);
        gson = new Gson();
    }

    @Override
    public void sendMessage(String text) {
        long messageID = LocalDBWrapper.createMessageEntry(MessageTypes.USUAL_MESSAGE, chatID, AppHelper.getPeerID(), text);
        MessageEntity message = LocalDBWrapper.getMessageByID(messageID);
        logic.sendMessage(receiverAddress, message);
        view.updateMessageList(message);
    }

    @Override
    public void handleEvent(JsonObject object) {
        switch (object.get("action").getAsInt()) {
            case UIActions.MESSAGE_RECEIVED: {
                MessageEntity messageEntity = LocalDBWrapper.getMessageByID(object.get("additional").getAsInt());
                view.updateMessageList(messageEntity);
            }
        }
    }

    @Override
    public void updateAdapter() {
        List<MessageEntity> entities = LocalDBWrapper.getMessagesByChatID(chatID);
        view.updateMessageList(entities == null ? new ArrayList<>() : entities);
    }
}
