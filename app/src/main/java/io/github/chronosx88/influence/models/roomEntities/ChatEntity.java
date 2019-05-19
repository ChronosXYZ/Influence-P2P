package io.github.chronosx88.influence.models.roomEntities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chats")
public class ChatEntity {
    @PrimaryKey @NonNull public String jid;
    @ColumnInfo public String chatName;

    public ChatEntity(@NonNull String jid, String chatName) {
        this.jid = jid;
        this.chatName = chatName;
    }
}
