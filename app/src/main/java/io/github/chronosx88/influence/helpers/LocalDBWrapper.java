package io.github.chronosx88.influence.helpers;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class LocalDBWrapper {
    private static final String LOG_TAG = "LocalDBWrapper";
    private static RoomHelper dbInstance = AppHelper.getChatDB();

    /**
     * Create a chat entry in the local database.
     * @param chatID Chat ID
     * @param name Chat name
     * @param metadataRef Reference to general chat metadata (key in DHT)
     * @param membersRef Reference to member list
     */
    public static void createChatEntry(String chatID, String name, String metadataRef, String membersRef, int chunkID) {
        dbInstance.chatDao().addChat(new ChatEntity(chatID, name, metadataRef, membersRef, new ArrayList<>(), chunkID));
    }

    /**
     * Creating a message entry in the local database
     * @param type Message type
     * @param chatID ID of the chat in which need to create a message
     * @param username Sender username
     * @param senderID Sender peer ID
     * @param timestamp Message timestamp
     * @param text Message text
     * @return New message
     */
    public static MessageEntity createMessageEntry(int type, String messageID, String chatID, String username, String senderID, long timestamp, String text, boolean isSent, boolean isRead) {
        List<ChatEntity> chatEntities = AppHelper.getChatDB().chatDao().getChatByChatID(chatID);
        if(chatEntities.size() < 1) {
            Log.e(LOG_TAG, "Failed to create message entry because chat " + chatID + " doesn't exists!");
            return null;
        }
        MessageEntity message = new MessageEntity(type, messageID, chatID, senderID, username, timestamp, text, isSent, isRead);
        dbInstance.messageDao().insertMessage(message);
        return message;
    }

    public static MessageEntity getMessageByID(String messageID) {
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
