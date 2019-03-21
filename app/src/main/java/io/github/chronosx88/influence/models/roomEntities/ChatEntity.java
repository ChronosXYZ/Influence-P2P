package io.github.chronosx88.influence.models.roomEntities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chats")
public class ChatEntity {
    @NonNull @PrimaryKey String id;
    @ColumnInfo public String name;
    @ColumnInfo public String peerAddresses;
    @ColumnInfo public String keyPairID;

    public ChatEntity(String id, String name, String peerAddresses, String keyPairID) {
        this.id = id;
        this.name = name;
        this.peerAddresses = peerAddresses;
        this.keyPairID = keyPairID;
    }

    public String getId() {
        return id;
    }

    public String getKeyPairID() {
        return keyPairID;
    }

    public String getPeerAddress() { return peerAddresses; }

    public String getName() {
        return name;
    }
}
