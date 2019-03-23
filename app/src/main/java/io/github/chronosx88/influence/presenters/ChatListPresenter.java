package io.github.chronosx88.influence.presenters;

import android.view.MenuItem;

import net.tomp2p.dht.FutureRemove;
import net.tomp2p.peers.Number160;

import io.github.chronosx88.influence.contracts.chatlist.ChatListLogicContract;
import io.github.chronosx88.influence.contracts.chatlist.ChatListPresenterContract;
import io.github.chronosx88.influence.contracts.chatlist.ChatListViewContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.ChatListAdapter;
import io.github.chronosx88.influence.logic.ChatListLogic;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class ChatListPresenter implements ChatListPresenterContract {
    private ChatListViewContract view;
    private ChatListLogicContract logic;
    private ChatListAdapter chatListAdapter;

    public ChatListPresenter(ChatListViewContract view) {
        this.view = view;
        chatListAdapter = new ChatListAdapter();
        this.logic = new ChatListLogic();
        this.view.setRecycleAdapter(chatListAdapter);
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
    public void onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case 0: {
                if(chatListAdapter.onClickPosition != -1) {
                    new Thread(() -> {
                        ChatEntity chat = chatListAdapter.getItem(chatListAdapter.onClickPosition);
                        AppHelper.getChatDB().chatDao().deleteChat(chat.chatID);
                        view.updateChatList(chatListAdapter, logic.loadAllChats());
                    }).start();
                }
            }
        }
    }
}
