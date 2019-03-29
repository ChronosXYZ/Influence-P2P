package io.github.chronosx88.influence.contracts.chatactivity;

import java.util.List;

import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public interface IChatViewContract {
    void updateMessageList(MessageEntity message);
    void updateMessageList(List<MessageEntity> messages);
}
