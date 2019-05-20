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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

import io.github.chronosx88.influence.helpers.AppHelper;

public class XMPPConnectionService extends Service {
    public static final String INTENT_NEW_MESSAGE = "io.github.chronosx88.intents.new_message";
    public static final String INTENT_SEND_MESSAGE = "io.github.chronosx88.intents.send_message";
    public static final String INTENT_AUTHENTICATED = "io.github.chronosx88.intents.authenticated";
    public static final String INTENT_AUTHENTICATION_FAILED = "io.github.chronosx88.intents.authentication_failed";

    public static final String MESSAGE_CHATID = "chat_jid";
    public static final String MESSAGE_ID = "message_id";
    public static final String MESSAGE_BODY = "message_body";
    public static final String MESSAGE_RECIPIENT = "message_recipient";

    public static XMPPConnection.ConnectionState CONNECTION_STATE = XMPPConnection.ConnectionState.DISCONNECTED;
    public static XMPPConnection.SessionState SESSION_STATE = XMPPConnection.SessionState.LOGGED_OUT;

    private Thread thread;
    private Handler threadHandler;
    private boolean isThreadAlive = false;
    private XMPPConnection connection;
    private Context context = AppHelper.getContext();

    public XMPPConnectionService() { }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    public void onServiceStart() {
        if(!isThreadAlive)
        {
            isThreadAlive = true;
            if(thread == null || !thread.isAlive()) {
                thread = new Thread(() -> {
                    Looper.prepare();
                    threadHandler = new Handler();
                    createConnection();
                    Looper.loop();
                });
                thread.start();
            }
        }
    }

    private void onServiceStop() {
        isThreadAlive = false;
        threadHandler.post(() -> {
            if(connection != null) {
                connection.disconnect();
            }
        });
    }

    private void createConnection() {
        if(connection == null) {
            connection = new XMPPConnection(this);
        }
        try {
            connection.connect();
        } catch (IOException | SmackException | XMPPException e) {
            Intent intent = new Intent(INTENT_AUTHENTICATION_FAILED);
            context.sendBroadcast(intent);
            e.printStackTrace();
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onServiceStart();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onServiceStop();
    }
}
