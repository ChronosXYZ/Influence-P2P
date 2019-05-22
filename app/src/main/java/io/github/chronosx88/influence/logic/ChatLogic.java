package io.github.chronosx88.influence.logic;

import com.instacart.library.truetime.TrueTime;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

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
        if (AppHelper.getXmppConnection().isConnectionAlive()) {
            EntityBareJid jid;
            try {
                jid = JidCreate.entityBareFrom(chatEntity.jid);
            } catch (XmppStringprepException e) {
                return null;
            }
            AppHelper.getXmppConnection().sendMessage(jid, text);
            long messageID = LocalDBWrapper.createMessageEntry(chatID, AppHelper.getJid(), TrueTime.now().getTime(), text, false, false);
            return LocalDBWrapper.getMessageByID(messageID);
        } else {
            return null;
        }
    }
}
