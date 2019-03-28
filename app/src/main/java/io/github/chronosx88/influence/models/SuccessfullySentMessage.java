package io.github.chronosx88.influence.models;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;

import io.github.chronosx88.influence.helpers.actions.NetworkActions;

public class SuccessfullySentMessage extends BasicNetworkMessage implements Serializable {
    private long messageID;

    public SuccessfullySentMessage(String senderID, PeerAddress senderPeerAddress, long messageID) {
        super(NetworkActions.MESSAGE_SENT, senderID, senderPeerAddress);
        this.messageID = messageID;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }
}
