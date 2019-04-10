package io.github.chronosx88.influence.contracts.chatactivity;

public interface IChatPresenterContract {
    void sendMessage(String text);
    void updateAdapter();
    void onDestroy();
}
