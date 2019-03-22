package io.github.chronosx88.influence.presenters;

import com.google.gson.JsonObject;

import io.github.chronosx88.influence.contracts.chatlist.ChatListLogicContract;
import io.github.chronosx88.influence.contracts.chatlist.ChatListPresenterContract;
import io.github.chronosx88.influence.contracts.chatlist.ChatListViewContract;
import io.github.chronosx88.influence.contracts.observer.Observer;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.ChatListAdapter;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.logic.ChatListLogic;
import io.github.chronosx88.influence.observable.MainObservable;

public class ChatListPresenter implements ChatListPresenterContract, Observer {
    private ChatListViewContract view;
    private ChatListLogicContract logic;
    private ChatListAdapter chatListAdapter;

    public ChatListPresenter(ChatListViewContract view) {
        this.view = view;
        chatListAdapter = new ChatListAdapter();
        this.logic = new ChatListLogic();
        this.view.setRecycleAdapter(chatListAdapter);
        AppHelper.getObservable().register(this);
    }

    @Override
    public void updateChatList() {
        view.updateChatList(chatListAdapter, logic.loadAllChats());
    }

    @Override
    public void openChat(String chatID) {
        // TODO
    }

    @Override
    public void handleEvent(JsonObject object) {
        switch (object.get("action").getAsInt()) {
            case UIActions.NEW_CHAT: {
                updateChatList();
            }
        }
    }
}
