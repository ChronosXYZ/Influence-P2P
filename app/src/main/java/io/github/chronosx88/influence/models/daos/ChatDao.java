package io.github.chronosx88.influence.models.daos;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

@Dao
public interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addChat(ChatEntity chatEntity);

    @Query("DELETE FROM chats WHERE jid = :jid")
    void deleteChat(String jid);

    @Query("SELECT * FROM chats")
    List<ChatEntity> getAllChats();

    @Query("SELECT * FROM chats WHERE jid = :jid")
    List<ChatEntity> getChatByChatID(String jid);

    @Update
    void updateChat(ChatEntity chat);
}
