package io.github.chronosx88.influence.helpers;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import io.github.chronosx88.influence.models.daos.ChatDao;
import io.github.chronosx88.influence.models.daos.MessageDao;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

@Database(entities = { MessageEntity.class, ChatEntity.class }, version = 1)
public abstract class RoomHelper extends RoomDatabase {
    public abstract ChatDao chatDao();
    public abstract MessageDao messageDao();
}
