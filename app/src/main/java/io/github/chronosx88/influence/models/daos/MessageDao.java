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
    long insertMessage(MessageEntity chatModel);

    @Query("DELETE FROM messages WHERE messageID = :messageID")
    void deleteMessage(String messageID);

    @Query("DELETE FROM messages WHERE jid = :jid")
    void deleteMessagesByChatID(String jid);

    @Query("SELECT * FROM messages WHERE jid = :jid")
    List<MessageEntity> getMessagesByChatID(String jid);

    @Query("SELECT * FROM messages WHERE messageID = :messageID")
    List<MessageEntity> getMessageByID(long messageID);

    @Update
    void updateMessage(MessageEntity message);
}
