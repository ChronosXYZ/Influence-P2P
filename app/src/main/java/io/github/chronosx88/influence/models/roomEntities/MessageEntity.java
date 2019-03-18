package io.github.chronosx88.influence.models.roomEntities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class MessageEntity {
    @PrimaryKey String id;
    @ColumnInfo String chatID;
    @ColumnInfo String sender;
    @ColumnInfo String text;

    public MessageEntity(String id, String chatID, String sender, String text) {
        this.id = id;
        this.chatID = chatID;
        this.sender = sender;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getChatID() {
        return chatID;
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
        return id + "/" + chatID + "/" + sender + "/" + text;
    }
}
