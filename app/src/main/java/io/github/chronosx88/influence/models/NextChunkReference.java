package io.github.chronosx88.influence.models;

import java.io.Serializable;

import io.github.chronosx88.influence.helpers.actions.NetworkActions;

public class NextChunkReference extends BasicNetworkMessage implements Serializable {
    private int nextChunkID;

    public NextChunkReference(String messageID, String senderID, String username, long timestamp, int nextChunkID) {
        super(NetworkActions.NEXT_CHUNK_REF, messageID, senderID, username, timestamp);
        this.nextChunkID = nextChunkID;
    }

    public int getNextChunkID() {
        return nextChunkID;
    }
}
