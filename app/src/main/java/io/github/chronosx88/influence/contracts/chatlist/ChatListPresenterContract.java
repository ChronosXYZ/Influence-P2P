package io.github.chronosx88.influence.contracts.chatlist;

import android.view.MenuItem;

public interface ChatListPresenterContract {
    void updateChatList();
    void openChat(String chatID);
    void onContextItemSelected(MenuItem item);
}
