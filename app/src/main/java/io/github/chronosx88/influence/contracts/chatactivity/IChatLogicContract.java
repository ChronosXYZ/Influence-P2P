package io.github.chronosx88.influence.contracts.chatactivity;

import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public interface IChatLogicContract {
    void sendMessage(MessageEntity message);
    void stopTrackingForNewMsgs();
}
