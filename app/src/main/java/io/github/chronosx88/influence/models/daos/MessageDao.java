package io.github.chronosx88.influence.models.daos;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

@Dao
public interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMessage(MessageEntity chatModel);

    @Query("DELETE FROM messages WHERE messageID = :messageID")
    void deleteMessage(String messageID);

    @Query("DELETE FROM messages WHERE chatID = :chatID")
    void deleteMessagesByChatID(String chatID);

    @Query("SELECT * FROM messages WHERE chatID = :chatID")
    List<MessageEntity> getMessagesByChatID(String chatID);

    @Query("SELECT * FROM messages WHERE messageID = :messageID")
    List<MessageEntity> getMessageByID(String messageID);

    @Update
    void updateMessage(MessageEntity message);
}
