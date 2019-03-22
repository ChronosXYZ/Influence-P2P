package io.github.chronosx88.influence.models.daos;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

@Dao
public interface ChatDao {
    @Insert
    void addChat(ChatEntity chatEntity);

    @Query("DELETE FROM chats WHERE id = :chatID")
    void deleteChat(String chatID);

    @Query("SELECT * FROM chats")
    List<ChatEntity> getAllChats();

    @Query("SELECT * FROM chats WHERE chatID = :chatID")
    List<ChatEntity> getChatByChatID(String chatID);

    @Query("SELECT * FROM chats WHERE id = :id")
    List<ChatEntity> getChatByID(String id);
}
