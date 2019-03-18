package io.github.chronosx88.influence.models.daos;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.github.chronosx88.influence.models.roomEntities.MessageModel;

@Dao
public interface MessageDao {
    @Insert
    void insertMessage(MessageModel chatModel);

    @Query("DELETE FROM messages WHERE id = :msgID")
    void deleteMessage(String msgID);

    @Query("DELETE FROM messages WHERE chatID = :chatID")
    void deleteMessagesByChatID(String chatID);

    @Query("SELECT * FROM messages WHERE chatID = :chatID")
    List<MessageModel> getMessagesByChatID(String chatID);

    @Query("SELECT * FROM messages WHERE id = :id")
    List<MessageModel> getMessageByID(String id);
}
