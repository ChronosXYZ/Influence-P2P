package io.github.chronosx88.influence.models.roomEntities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class MessageEntity {
    @PrimaryKey @NonNull public String messageID; // Global message ID
    @ColumnInfo public int type; // Message type
    @ColumnInfo public String chatID; // Chat ID
    @ColumnInfo public String senderID; // PeerID
    @ColumnInfo public String username; // Username
    @ColumnInfo public long timestamp; // Timestamp
    @ColumnInfo public String text; // Message text
    @ColumnInfo public boolean isSent; // Send status indicator
    @ColumnInfo public boolean isRead; // Message Read Indicator

    public MessageEntity(int type, String messageID, String chatID, String senderID, String username, long timestamp, String text, boolean isSent, boolean isRead) {
        this.type = type;
        this.messageID = messageID;
        this.chatID = chatID;
        this.senderID = senderID;
        this.username = username;
        this.timestamp = timestamp;
        this.text = text;
        this.isSent = isSent;
        this.isRead = isRead;
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
