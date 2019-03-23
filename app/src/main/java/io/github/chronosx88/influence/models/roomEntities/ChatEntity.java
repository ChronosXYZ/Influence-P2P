package io.github.chronosx88.influence.models.roomEntities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chats")
public class ChatEntity {
    @PrimaryKey(autoGenerate = true) public int id;
    @ColumnInfo public String chatID;
    @ColumnInfo public String name;
    @ColumnInfo public byte[] peerAddresses;
    @ColumnInfo public String keyPairID;

    public ChatEntity(String chatID, String name, String keyPairID, byte[] peerAddresses) {
        this.chatID = chatID;
        this.name = name;
        this.peerAddresses = peerAddresses;
        this.keyPairID = keyPairID;
    }

    public int getId() {
        return id;
    }

    public String getKeyPairID() {
        return keyPairID;
    }

    public byte[] getPeerAddress() { return peerAddresses; }

    public String getName() {
        return name;
    }

    public String getChatID() {
        return chatID;
    }
}
