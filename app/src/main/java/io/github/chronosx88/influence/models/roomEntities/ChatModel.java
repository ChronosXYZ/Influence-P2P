package io.github.chronosx88.influence.models.roomEntities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chats")
public class ChatModel {
    @PrimaryKey String id;
    String name;
    String keyPairID;
}
