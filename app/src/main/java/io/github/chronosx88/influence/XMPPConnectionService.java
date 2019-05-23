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
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.models.appEvents.AuthenticationStatusEvent;

public class XMPPConnectionService extends Service {
    public static XMPPConnection.ConnectionState CONNECTION_STATE = XMPPConnection.ConnectionState.DISCONNECTED;
    public static XMPPConnection.SessionState SESSION_STATE = XMPPConnection.SessionState.LOGGED_OUT;

    private Thread thread;
    private Handler threadHandler;
    private boolean isThreadAlive = false;
    private XMPPConnection connection;
    private Context context = AppHelper.getContext();
    private XMPPServiceBinder binder = new XMPPServiceBinder();

    public XMPPConnectionService() { }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

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
        if(threadHandler != null) {
            threadHandler.post(() -> {
                if(connection != null) {
                    thread.interrupt();
                    thread = null;
                    connection.disconnect();
                    connection = null;
                }
            });
        }
    }

    private void createConnection() {
        if(connection == null) {
            connection = new XMPPConnection(this);
        }
        try {
            connection.connect();
        } catch (IOException | SmackException e) {
            EventBus.getDefault().post(new AuthenticationStatusEvent(AuthenticationStatusEvent.NETWORK_ERROR));
            e.printStackTrace();
            onServiceStop();
            stopSelf();
        } catch (XMPPException | EmptyLoginCredentialsException e) {
            EventBus.getDefault().post(new AuthenticationStatusEvent(AuthenticationStatusEvent.INCORRECT_LOGIN_OR_PASSWORD));
            e.printStackTrace();
            onServiceStop();
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

    public class XMPPServiceBinder extends Binder {
        public XMPPConnection getConnection() {
            return connection;
        }
    }
}
