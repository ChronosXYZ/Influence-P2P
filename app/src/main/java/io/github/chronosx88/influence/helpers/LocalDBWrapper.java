package io.github.chronosx88.influence.helpers;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class LocalDBWrapper {
    private static final String LOG_TAG = "LocalDBWrapper";
    private static RoomHelper dbInstance = AppHelper.getChatDB();

    public static void createChatEntry(String jid, String chatName) {
        dbInstance.chatDao().addChat(new ChatEntity(jid, chatName));
    }

    public static long createMessageEntry(String jid, String senderJid, long timestamp, String text, boolean isSent, boolean isRead) {
        List<ChatEntity> chatEntities = AppHelper.getChatDB().chatDao().getChatByChatID(jid);
        if(chatEntities.size() < 1) {
            Log.e(LOG_TAG, "Failed to create message entry because chat " + jid + " doesn't exists!");
            return -1;
        }
        MessageEntity message = new MessageEntity(jid, senderJid, timestamp, text, isSent, isRead);
        long index = dbInstance.messageDao().insertMessage(message);
        return index;
    }

    public static MessageEntity getMessageByID(long messageID) {
        List<MessageEntity> messages = dbInstance.messageDao().getMessageByID(messageID);
        if(messages.isEmpty()) {
            return null;
        }
        return messages.get(0);
    }

    public static List<MessageEntity> getMessagesByChatID(String chatID) {
        List<MessageEntity> messages = dbInstance.messageDao().getMessagesByChatID(chatID);
        if(messages.isEmpty()) {
            return null;
        }
        return messages;
    }

    public static ChatEntity getChatByChatID(String chatID) {
        List<ChatEntity> chats = dbInstance.chatDao().getChatByChatID(chatID);
        if(chats.isEmpty()) {
            return null;
        }
        return chats.get(0);
    }

    public static void updateChatEntity(ChatEntity chatEntity) {
        dbInstance.chatDao().updateChat(chatEntity);
    }

    public static void updateMessage(MessageEntity messageEntity) {
        dbInstance.messageDao().updateMessage(messageEntity);
    }
}
