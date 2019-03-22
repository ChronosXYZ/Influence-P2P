package io.github.chronosx88.influence.models.roomEntities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class MessageEntity {
    @NonNull @PrimaryKey String id;
    @ColumnInfo public String chatID;
    @ColumnInfo public String sender;
    @ColumnInfo public String date;
    @ColumnInfo public String text;

    public MessageEntity(String id, String chatID, String sender, String date, String text) {
        this.id = id;
        this.chatID = chatID;
        this.sender = sender;
        this.date = date;
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
