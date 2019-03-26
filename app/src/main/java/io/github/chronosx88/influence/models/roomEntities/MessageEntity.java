package io.github.chronosx88.influence.models.roomEntities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class MessageEntity {
    @PrimaryKey(autoGenerate = true) String id;
    @ColumnInfo public int type;
    @ColumnInfo public String chatID;
    @ColumnInfo public String sender;
    @ColumnInfo public long timestamp;
    @ColumnInfo public String text;

    public MessageEntity(int type, String chatID, String sender, long timestamp, String text) {
        this.type = type;
        this.chatID = chatID;
        this.sender = sender;
        this.timestamp = timestamp;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public int getType() { return type; }

    public String getChatID() {
        return chatID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    @NonNull
    @Override
    public String toString() {
        return id + "/" + chatID + "/" + type + "/" + sender + "/" + text;
    }
}
