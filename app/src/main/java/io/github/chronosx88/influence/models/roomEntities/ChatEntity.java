package io.github.chronosx88.influence.models.roomEntities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chats")
public class ChatEntity {
    @PrimaryKey String id;
    @ColumnInfo  String name;
    @ColumnInfo String keyPairID;

    public ChatEntity(String id, String name, String keyPairID) {
        this.id = id;
        this.name = name;
        this.keyPairID = keyPairID;
    }

    public String getId() {
        return id;
    }

    public String getKeyPairID() {
        return keyPairID;
    }

    public String getName() {
        return name;
    }
}
