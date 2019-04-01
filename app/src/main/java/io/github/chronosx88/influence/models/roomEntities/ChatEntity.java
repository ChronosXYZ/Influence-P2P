package io.github.chronosx88.influence.models.roomEntities;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chats")
public class ChatEntity {
    @PrimaryKey @NonNull public String chatID;
    @ColumnInfo public String name;
    @ColumnInfo public String metadataRef;
    @ColumnInfo public String membersRef;
    @ColumnInfo public ArrayList<String> bannedUsers;
    @ColumnInfo public int chunkCursor;

    public ChatEntity(@NonNull String chatID, String name, String metadataRef, String membersRef, ArrayList<String> bannedUsers, int chunkCursor) {
        this.chatID = chatID;
        this.name = name;
        this.metadataRef = metadataRef;
        this.membersRef = membersRef;
        this.bannedUsers = bannedUsers;
        this.chunkCursor = chunkCursor;
    }
}
