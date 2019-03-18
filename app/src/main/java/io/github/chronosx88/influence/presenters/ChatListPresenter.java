package io.github.chronosx88.influence.presenters;

import io.github.chronosx88.influence.contracts.chatlist.ChatListLogicContract;
import io.github.chronosx88.influence.contracts.chatlist.ChatListPresenterContract;
import io.github.chronosx88.influence.contracts.chatlist.ChatListViewContract;
import io.github.chronosx88.influence.helpers.ChatListAdapter;
import io.github.chronosx88.influence.logic.ChatListLogic;

public class ChatListPresenter implements ChatListPresenterContract {
    private ChatListViewContract view;
    private ChatListLogicContract logic;
    private ChatListAdapter chatListAdapter;

    public ChatListPresenter(ChatListViewContract view) {
        this.view = view;
        chatListAdapter = new ChatListAdapter();
        this.view.setRecycleAdapter(chatListAdapter);
        this.logic = new ChatListLogic();
    }

    @Override
    public void updateChatList() {
        chatListAdapter.setChatList(logic.loadAllChats());
        chatListAdapter.notifyDataSetChanged();
    }

    @Override
    public void openChat(String chatID) {
        // TODO
    }
}
