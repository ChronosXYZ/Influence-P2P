package io.github.chronosx88.influence.helpers;

import android.util.Log;

import net.tomp2p.peers.PeerAddress;

import java.util.Date;
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
     * @param peerAddress Companion's address
     * @return Successful chat creation (true / false)
     */
    public static boolean createChatEntry(String chatID, String name, PeerAddress peerAddress) {
        List<ChatEntity> chatEntities = AppHelper.getChatDB().chatDao().getChatByChatID(chatID);
        if (chatEntities.size() > 0) {
            Log.e(LOG_TAG, "Failed to create chat " + chatID + " because chat exists!");
            return false;
        }
        dbInstance.chatDao().addChat(new ChatEntity(chatID, name, "", Serializer.serialize(peerAddress)));
        return true;
    }

    /**
     * Creating a message entry in the local database
     * @param type Message type
     * @param chatID ID of the chat in which need to create a message
     * @param sender Message sender (username)
     * @param text Message text (or technical info if technical message type)
     * @return
     */
    public static boolean createMessageEntry(int type, String chatID, String sender, String text) {
        List<ChatEntity> chatEntities = AppHelper.getChatDB().chatDao().getChatByChatID(chatID);
        if(chatEntities.size() < 1) {
            Log.e(LOG_TAG, "Failed to create message entry because chat " + chatID + " doesn't exists!");
            return false;
        }
        dbInstance.messageDao().insertMessage(new MessageEntity(type, chatID, sender, new Date().getTime(), text));
        return true;
    }
}
