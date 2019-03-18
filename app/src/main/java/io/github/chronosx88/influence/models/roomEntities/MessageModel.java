package io.github.chronosx88.influence.models.roomEntities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class MessageModel {
    @PrimaryKey String id;
    String chatID;
    String sender;
    String text;
}
