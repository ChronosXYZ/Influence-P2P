package io.github.chronosx88.influence.contracts.chatactivity;

import net.tomp2p.peers.PeerAddress;

import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public interface IChatLogicContract {
    void sendMessage(PeerAddress address, MessageEntity message);
}
