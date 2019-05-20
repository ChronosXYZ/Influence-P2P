package io.github.chronosx88.influence.logic;

import android.content.Intent;

import com.instacart.library.truetime.TrueTime;

import io.github.chronosx88.influence.XMPPConnection;
import io.github.chronosx88.influence.XMPPConnectionService;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class ChatLogic implements CoreContracts.IChatLogicContract {
    private String chatID;
    private ChatEntity chatEntity;
    //private KeyPairManager keyPairManager;

    public ChatLogic(ChatEntity chatEntity) {
        this.chatEntity = chatEntity;
        this.chatID = chatEntity.jid;
        //this.keyPairManager = new KeyPairManager();
    }

    @Override
    public MessageEntity sendMessage(String text) {
        if (XMPPConnectionService.CONNECTION_STATE.equals(XMPPConnection.ConnectionState.CONNECTED)) {
            Intent intent = new Intent(XMPPConnectionService.INTENT_SEND_MESSAGE);
            intent.putExtra(XMPPConnectionService.MESSAGE_BODY, text);
            intent.putExtra(XMPPConnectionService.MESSAGE_RECIPIENT, chatEntity.jid);
            AppHelper.getContext().sendBroadcast(intent);
            long messageID = LocalDBWrapper.createMessageEntry(chatID, AppHelper.getJid(), TrueTime.now().getTime(), text, false, false);
            return LocalDBWrapper.getMessageByID(messageID);
        } else {
            return null;
        }
    }
}
