package io.github.chronosx88.influence.helpers;

import com.instacart.library.truetime.TrueTime;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import io.github.chronosx88.influence.models.appEvents.NewMessageEvent;

public class NetworkHandler implements IncomingChatMessageListener {
    private final static String LOG_TAG = "NetworkHandler";

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        if(LocalDBWrapper.getChatByChatID(from.asEntityBareJidString()) == null) {
            LocalDBWrapper.createChatEntry(chat.getXmppAddressOfChatPartner().asUnescapedString(), chat.getXmppAddressOfChatPartner().asBareJid().asUnescapedString());
        }
        long messageID = LocalDBWrapper.createMessageEntry(chat.getXmppAddressOfChatPartner().asUnescapedString(), from.asUnescapedString(), TrueTime.now().getTime(), message.getBody(), true, false);

        EventBus.getDefault().post(new NewMessageEvent(chat.getXmppAddressOfChatPartner().toString(), messageID));
    }
}