package io.github.chronosx88.influence.models.daos;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.github.chronosx88.influence.models.roomEntities.ChatModel;

@Dao
public interface ChatDao {
    @Insert
    void addChat(ChatModel chatModel);

    @Query("DELETE FROM chats WHERE id = :chatID")
    void deleteChat(String chatID);

    @Query("SELECT * FROM chats")
    List<ChatModel> getAllChats();

    @Query("SELECT * FROM chats WHERE id = :chatID")
    List<ChatModel> getChatByID(String chatID);
}
