package io.github.chronosx88.influence.presenters;

import android.content.Intent;
import android.view.MenuItem;

import io.github.chronosx88.influence.contracts.chatlist.IChatListLogicContract;
import io.github.chronosx88.influence.contracts.chatlist.IChatListPresenterContract;
import io.github.chronosx88.influence.contracts.chatlist.IChatListViewContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.ChatListAdapter;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.logic.ChatListLogic;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.views.ChatActivity;

public class ChatListPresenter implements IChatListPresenterContract {
    private IChatListViewContract view;
    private IChatListLogicContract logic;
    private ChatListAdapter chatListAdapter;

    public ChatListPresenter(IChatListViewContract view) {
        this.view = view;
        chatListAdapter = new ChatListAdapter((v, p)-> {
            openChat(chatListAdapter.getChatEntity(p).chatID);
        });
        this.logic = new ChatListLogic();
        this.view.setRecycleAdapter(chatListAdapter);
    }

    @Override
    public void updateChatList() {
        view.updateChatList(chatListAdapter, logic.loadAllChats());
    }

    @Override
    public void openChat(String chatID) {
        Intent intent = new Intent(AppHelper.getContext(), ChatActivity.class);
        intent.putExtra("chatID", chatID);
        intent.putExtra("contactUsername", LocalDBWrapper.getChatByChatID(chatID).name);
        view.startActivity(intent);
    }

    @Override
    public void onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case 0: {
                if(chatListAdapter.onClickPosition != -1) {
                    new Thread(() -> {
                        ChatEntity chat = chatListAdapter.getItem(chatListAdapter.onClickPosition);
                        AppHelper.getChatDB().chatDao().deleteChat(chat.chatID);
                        AppHelper.getChatDB().messageDao().deleteMessagesByChatID(chat.chatID);
                        view.updateChatList(chatListAdapter, logic.loadAllChats());
                    }).start();
                }
            }
        }
    }
}
