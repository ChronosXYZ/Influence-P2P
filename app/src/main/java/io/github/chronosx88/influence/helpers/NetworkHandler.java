package io.github.chronosx88.influence.helpers;

import android.content.Context;
import android.content.Intent;

import com.instacart.library.truetime.TrueTime;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import io.github.chronosx88.influence.XMPPConnectionService;

public class NetworkHandler implements IncomingChatMessageListener {
    private final static String LOG_TAG = "NetworkHandler";
    private Context context;

    public NetworkHandler(Context context) {
        this.context = context;
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        if(LocalDBWrapper.getChatByChatID(from.asEntityBareJidString()) == null) {
            LocalDBWrapper.createChatEntry(chat.getXmppAddressOfChatPartner().asUnescapedString(), chat.getXmppAddressOfChatPartner().asBareJid().asUnescapedString());
        }
        long messageID = LocalDBWrapper.createMessageEntry(chat.getXmppAddressOfChatPartner().asUnescapedString(), from.asUnescapedString(), TrueTime.now().getTime(), message.getBody(), true, false);
        Intent intent = new Intent(XMPPConnectionService.INTENT_NEW_MESSAGE);
        intent.setPackage(context.getPackageName());
        intent.putExtra(XMPPConnectionService.MESSAGE_CHATID, chat.getXmppAddressOfChatPartner().toString());
        intent.putExtra(XMPPConnectionService.MESSAGE_ID, messageID);
        context.sendBroadcast(intent);
    }
}