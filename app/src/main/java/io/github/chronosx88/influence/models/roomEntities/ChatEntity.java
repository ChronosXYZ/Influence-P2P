package io.github.chronosx88.influence.models.roomEntities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

import io.github.chronosx88.influence.models.GenericUser;

@Entity(tableName = "chats")
public class ChatEntity {
    @PrimaryKey @NonNull public String jid;
    @ColumnInfo public String chatName;
    @ColumnInfo public ArrayList<GenericUser> users;
    @ColumnInfo public int unreadMessagesCount;

    public ChatEntity(@NonNull String jid, String chatName, ArrayList<GenericUser> users, int unreadMessagesCount) {
        this.jid = jid;
        this.chatName = chatName;
        this.users = users;
        this.unreadMessagesCount = unreadMessagesCount;
    }

    public boolean isPrivateChat() {
        return users.size() == 1;
    }
}
