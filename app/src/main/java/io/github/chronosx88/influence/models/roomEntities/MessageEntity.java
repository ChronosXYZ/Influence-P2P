package io.github.chronosx88.influence.models.roomEntities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class MessageEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    @ColumnInfo public int type;
    @ColumnInfo public String chatID;
    @ColumnInfo public String sender;
    @ColumnInfo public long timestamp;
    @ColumnInfo public String text;
    @ColumnInfo public boolean isSent;
    @ColumnInfo public boolean isRead;

    public MessageEntity(int type, String chatID, String sender, long timestamp, String text, boolean isSent, boolean isRead) {
        this.type = type;
        this.chatID = chatID;
        this.sender = sender;
        this.timestamp = timestamp;
        this.text = text;
        this.isSent = isSent;
        this.isRead = isRead;
    }

    @NonNull
    @Override
    public String toString() {
        return id + "/" + chatID + "/" + type + "/" + sender + "/" + text;
    }
}
