package io.github.chronosx88.influence.logic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import io.github.chronosx88.influence.XMPPConnectionService;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;

public class MainLogic implements CoreContracts.IMainLogicContract {
    private static final String LOG_TAG = MainLogic.class.getName();

    private Context context;

    public MainLogic() {
        this.context = AppHelper.getContext();
    }

    @Override
    public void startService() {
        context.startService(new Intent(context, XMPPConnectionService.class));
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                XMPPConnectionService.XMPPServiceBinder binder = (XMPPConnectionService.XMPPServiceBinder) service;
                AppHelper.setXmppConnection(binder.getConnection());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                AppHelper.setXmppConnection(null);
            }
        };
        context.bindService(new Intent(context, XMPPConnectionService.class), connection,Context.BIND_AUTO_CREATE);
    }
}
