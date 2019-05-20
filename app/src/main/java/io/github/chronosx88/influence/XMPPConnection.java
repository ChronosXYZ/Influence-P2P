/*
 * Copyright (C) 2019 ChronosX88
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.chronosx88.influence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.NetworkHandler;

public class XMPPConnection implements ConnectionListener {
    private final static String LOG_TAG = "XMPPConnection";
    private LoginCredentials credentials = new LoginCredentials();
    private XMPPTCPConnection connection = null;
    private SharedPreferences prefs;
    private NetworkHandler networkHandler;
    private BroadcastReceiver sendMessageReceiver = null;
    private Context context;

    public enum ConnectionState {
        CONNECTED,
        DISCONNECTED
    }

    public enum SessionState {
        LOGGED_IN,
        LOGGED_OUT
    }

    public XMPPConnection(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
        String jid = prefs.getString("jid", null);
        String password = prefs.getString("pass", null);
        if(jid != null && password != null) {
            String username = jid.split("@")[0];
            String jabberHost = jid.split("@")[1];
            credentials.username = username;
            credentials.jabberHost = jabberHost;
            credentials.password = password;
        }
        networkHandler = new NetworkHandler(context);
    }

    public void connect() throws XMPPException, SmackException, IOException {
        if(connection == null) {
            XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(credentials.jabberHost)
                    .setHost(credentials.jabberHost)
                    .setResource(AppHelper.APP_NAME)
                    .setKeystoreType(null)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                    .setCompressionEnabled(true)
                    .build();

            setupSendMessageReceiver();

            connection = new XMPPTCPConnection(conf);
            connection.addConnectionListener(this);
            if(credentials.jabberHost.equals("") && credentials.password.equals("") && credentials.username.equals("")){
                throw new IOException();
            }
            try {
                connection.connect();
                connection.login(credentials.username, credentials.password);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ChatManager.getInstanceFor(connection).addIncomingListener(networkHandler);
            ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
            ReconnectionManager.setEnabledPerDefault(true);
            reconnectionManager.enableAutomaticReconnection();
        }
    }

    public void disconnect() {
        prefs.edit().putBoolean("logged_in", false).apply();
        if(connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    @Override
    public void connected(org.jivesoftware.smack.XMPPConnection connection) {
        XMPPConnectionService.CONNECTION_STATE = ConnectionState.CONNECTED;
    }

    @Override
    public void authenticated(org.jivesoftware.smack.XMPPConnection connection, boolean resumed) {
        XMPPConnectionService.SESSION_STATE = SessionState.LOGGED_IN;
        prefs.edit().putBoolean("logged_in", true).apply();
        context.sendBroadcast(new Intent(XMPPConnectionService.INTENT_AUTHENTICATED));
        AppHelper.setJid(credentials.username + "@" + credentials.jabberHost);
    }

    @Override
    public void connectionClosed() {
        XMPPConnectionService.CONNECTION_STATE = ConnectionState.DISCONNECTED;
        XMPPConnectionService.SESSION_STATE = SessionState.LOGGED_OUT;
        prefs.edit().putBoolean("logged_in", false).apply();
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        XMPPConnectionService.CONNECTION_STATE = ConnectionState.DISCONNECTED;
        XMPPConnectionService.SESSION_STATE = SessionState.LOGGED_OUT;
        prefs.edit().putBoolean("logged_in", false).apply();
        Log.e(LOG_TAG, "Connection closed, exception occurred");
        e.printStackTrace();
    }

    private void setupSendMessageReceiver() {
        sendMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case XMPPConnectionService.INTENT_SEND_MESSAGE: {
                        String recipientJid = intent.getStringExtra(XMPPConnectionService.MESSAGE_RECIPIENT);
                        String messageText = intent.getStringExtra(XMPPConnectionService.MESSAGE_BODY);
                        sendMessage(recipientJid, messageText);
                        break;
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(XMPPConnectionService.INTENT_SEND_MESSAGE);
        context.registerReceiver(sendMessageReceiver, intentFilter);
    }

    private void sendMessage(String recipientJid, String messageText) {
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(recipientJid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        Chat chat = ChatManager.getInstanceFor(connection).chatWith(jid);
        try {
            Message message = new Message(jid, Message.Type.chat);
            message.setBody(messageText);
            chat.send(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public XMPPTCPConnection getConnection() {
        return connection;
    }
}
