package io.github.chronosx88.influence.logic;

import com.google.gson.Gson;

import net.tomp2p.peers.PeerAddress;

import io.github.chronosx88.influence.contracts.chatactivity.IChatLogicContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.P2PUtils;
import io.github.chronosx88.influence.models.SendMessage;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class ChatLogic implements IChatLogicContract {
    private static Gson gson = new Gson();
    @Override
    public void sendMessage(PeerAddress address, MessageEntity message) {
        new Thread(() -> {
            P2PUtils
                    .send(address, gson.toJson(
                            new SendMessage(
                                    AppHelper.getPeerID(),
                                    AppHelper.getPeerDHT().peerAddress(),
                                    message.id,
                                    message.timestamp,
                                    message.type,
                                    message.chatID,
                                    message.text
                            )
                    ));
        }).start();
        // TODO: put message into DHT if user is offline
    }
}
