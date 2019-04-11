package io.github.chronosx88.influence.logic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.chatactivity.IChatLogicContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.KeyPairManager;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.helpers.ObservableUtils;
import io.github.chronosx88.influence.helpers.P2PUtils;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.JoinChatMessage;
import io.github.chronosx88.influence.models.NextChunkReference;
import io.github.chronosx88.influence.models.TextMessage;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class ChatLogic implements IChatLogicContract {
    private static Gson gson = new Gson();
    private String chatID;
    private String newMessage = "";
    private ChatEntity chatEntity;
    private Thread checkNewMessagesThread = null;
    private KeyPairManager keyPairManager;
    private Timer timer;

    public ChatLogic(ChatEntity chatEntity) {
        this.chatEntity = chatEntity;
        this.chatID = chatEntity.chatID;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                checkForNewMessages();
            }
        };
        this.timer = new Timer();
        if(AppHelper.getPeerDHT() != null) {
            timer.schedule(timerTask, 1, 1000);
        }
        this.keyPairManager = new KeyPairManager();
    }

    @Override
    public void sendMessage(MessageEntity message) {
        if(AppHelper.getPeerDHT() == null) {
            ObservableUtils.notifyUI(UIActions.NODE_IS_OFFLINE);
            return;
        }
        new Thread(() -> {
            Data data = null;
            try {
                data = new Data(gson.toJson(new TextMessage(message.senderID, message.messageID, message.chatID, message.username, message.timestamp, message.text, false)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            data.protectEntry(keyPairManager.getKeyPair("mainKeyPair"));
            P2PUtils.put(chatID + "_messages" + chatEntity.chunkCursor, message.messageID, data);
            try {
                P2PUtils.put(chatID + "_newMessage", null, new Data(message.messageID));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void checkForNewMessages() {
        if(checkNewMessagesThread == null) {
            checkNewMessagesThread = new Thread(() -> {
                Map<Number640, Data> data = P2PUtils.get(chatID + "_newMessage");
                if(data != null) {
                    for(Map.Entry<Number640, Data> entry : data.entrySet()) {
                        String newMessage = null;
                        try {
                            newMessage = (String) entry.getValue().object();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!newMessage.equals(this.newMessage)) {
                            handleNewMessages(chatEntity.chunkCursor);
                            this.newMessage = newMessage;
                        }
                    }
                }
            });
        }

        if(!checkNewMessagesThread.isAlive()) {
            checkNewMessagesThread = new Thread(() -> {
                Map<Number640, Data> data = P2PUtils.get(chatID + "_newMessage");
                if(data != null) {
                    for(Map.Entry<Number640, Data> entry : data.entrySet()) {
                        String newMessage = null;
                        try {
                            newMessage = (String) entry.getValue().object();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!newMessage.equals(this.newMessage)) {
                            handleNewMessages(chatEntity.chunkCursor);
                            this.newMessage = newMessage;
                        }
                    }
                }
            });
            checkNewMessagesThread.start();
        }
    }

    private void handleNewMessages(int chunkID) {
        new Thread(() -> {
            Map<Number640, Data> messages = P2PUtils.get(chatEntity.chatID + "_messages" + chunkID);
            if (messages != null) {
                for (Map.Entry<Number640, Data> message : messages.entrySet()) {
                    String json = null;
                    try {
                        json = (String) message.getValue().object();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    switch (getMessageAction(json)) {
                        case NetworkActions.TEXT_MESSAGE: {
                            TextMessage textMessage = gson.fromJson(json, TextMessage.class);
                            LocalDBWrapper.createMessageEntry(NetworkActions.TEXT_MESSAGE, textMessage.getMessageID(), textMessage.getChatID(), textMessage.getUsername(), textMessage.getSenderID(), textMessage.getTimestamp(), textMessage.getText(), true, false);
                            ObservableUtils.notifyUI(UIActions.MESSAGE_RECEIVED, chatID, textMessage.getMessageID());
                            break;
                        }
                        case NetworkActions.JOIN_CHAT: {
                            JoinChatMessage joinChatMessage = gson.fromJson(json, JoinChatMessage.class);
                            LocalDBWrapper.createMessageEntry(NetworkActions.JOIN_CHAT, joinChatMessage.getMessageID(), joinChatMessage.getChatID(), joinChatMessage.getUsername(), joinChatMessage.getSenderID(), joinChatMessage.getTimestamp(), null, true, false);
                            ObservableUtils.notifyUI(UIActions.MESSAGE_RECEIVED, chatID, joinChatMessage.getMessageID());
                            break;
                        }
                        case NetworkActions.NEXT_CHUNK_REF: {
                            NextChunkReference nextChunkReference = gson.fromJson(json, NextChunkReference.class);
                            chatEntity.chunkCursor = nextChunkReference.getNextChunkID();
                            LocalDBWrapper.updateChatEntity(chatEntity);
                            break;
                        }
                    }
                }
                if(messages.size() > 10) {
                    String messageID = UUID.randomUUID().toString();
                    try {
                        P2PUtils.put(chatEntity.chatID + "_messages" + chunkID, messageID, new Data(gson.toJson(new NextChunkReference(messageID, AppHelper.getPeerID(), AppHelper.getPeerID(), System.currentTimeMillis(), chatEntity.chunkCursor+1))));
                        P2PUtils.put(chatEntity.chatID + "_newMessage", null, new Data(messageID));
                        LocalDBWrapper.updateChatEntity(chatEntity);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private int getMessageAction(String json) {
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        return jsonObject.get("action").getAsInt();
    }

    @Override
    public void stopTrackingForNewMsgs() {
        timer.cancel();
        timer.purge();
    }
}
